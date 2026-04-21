import { useEffect, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import { AnimatePresence, motion } from 'framer-motion';
import {
  closeSixteenQueensRound,
  fetchSixteenQueensHistory,
  fetchSixteenQueensLeaderboard,
  fetchSixteenQueensReport,
  fetchSixteenQueensSamples,
  resetSixteenQueensRecognized,
  solveSixteenQueens,
  submitSixteenQueens
} from '../../api/sixteenQueensApi';
import { ChessBoard } from '../../features/sixteen-queens/components/ChessBoard';
import { HistoryTimeline } from '../../features/sixteen-queens/components/HistoryTimeline';
import { LeaderboardTable } from '../../features/sixteen-queens/components/LeaderboardTable';
import { ReportsDashboard } from '../../features/sixteen-queens/components/ReportsDashboard';
import { ToastStack } from '../../features/sixteen-queens/components/ToastStack';
import { useQueensGameStore } from '../../features/sixteen-queens/useQueensGameStore';
import { useSoundEffects } from '../../features/sixteen-queens/useSoundEffects';
import { getBoardStatus, parseAnswerString, toAnswerString } from '../../features/sixteen-queens/validation';

const SAMPLE_LIMIT = 8;
const LEADERBOARD_LIMIT = 5000;

function SixteenQueensPage() {
  const navigate = useNavigate();
  const sounds = useSoundEffects();

  const {
    size,
    board,
    analysis,
    hoverCell,
    lastMoveCellKey,
    invalidMoveCellKey,
    gameState,
    viewerRole,
    solveConfig,
    submitForm,
    solveResult,
    sampleSolutions,
    selectedSampleIndex,
    submitResult,
    historyResult,
    leaderboardResult,
    reportResult,
    roundClosed,
    loading,
    statusMessage,
    errorMessage,
    toasts,
    celebration,
    setLoading,
    setStatusMessage,
    setErrorMessage,
    setViewerRole,
    setRoundClosed,
    updateSolveConfig,
    updateSubmitForm,
    setApiData,
    pushToast,
    removeToast,
    setCelebration,
    clearBoard,
    setHoverCell,
    loadBoard,
    toggleQueen
  } = useQueensGameStore();

  const boardStatus = getBoardStatus(analysis);
  const answerFromGrid = useMemo(() => toAnswerString(board), [board]);
  const canSubmit = submitForm.playerName.trim().length > 0 && boardStatus === 'valid';
  const showSamplesInControls = viewerRole === 'ADMIN' && Boolean(solveResult);

  const handleSelectSample = (index) => {
    const selected = sampleSolutions[index];
    const parsed = parseAnswerString(selected, size);

    if (!parsed) {
      pushToast({ tone: 'warning', title: 'Invalid sample', message: 'This sample could not be parsed for board preview.' });
      return;
    }

    setApiData('selectedSampleIndex', index);
    loadBoard(parsed);
    setStatusMessage(`Previewing sample pattern ${index + 1}.`);
  };

  useEffect(() => {
    const preload = async () => {
      try {
        const [historyData, leaderboardData, reportData] = await Promise.all([
          fetchSixteenQueensHistory(12),
          fetchSixteenQueensLeaderboard(LEADERBOARD_LIMIT),
          fetchSixteenQueensReport()
        ]);
        setApiData('historyResult', historyData);
        setApiData('leaderboardResult', leaderboardData);
        setApiData('reportResult', reportData);
      } catch {
        // Dashboard is still usable without initial data.
      }
    };

    preload();
  }, [setApiData]);

  const checkAllCompleted = async () => {
    const latestReport = await fetchSixteenQueensReport();
    setApiData('reportResult', latestReport);
    if (
      latestReport?.totalKnownSolutionsPersisted > 0 &&
      latestReport?.activeRecognizedSolutions >= latestReport?.totalKnownSolutionsPersisted
    ) {
      sounds.playVictory();
      setApiData('gameState', 'all_solutions_completed');
      setCelebration('fireworks');
      pushToast({
        tone: 'success',
        title: 'All solutions completed',
        message: 'Every persisted solution has been discovered. Great coordination!'
      });
    }
  };

  const withLoading = async (label, work) => {
    setLoading(true);
    setErrorMessage('');
    setStatusMessage(label);

    try {
      await work();
    } catch (requestError) {
      const message =
        requestError?.response?.data?.message ||
        requestError?.message ||
        'Request failed. Please retry.';
      setErrorMessage(message);
      pushToast({
        tone: 'error',
        title: 'Request failed',
        message
      });
    } finally {
      setLoading(false);
    }
  };

  const handleCellClick = (row, col) => {
    const outcome = toggleQueen(row, col);

    if (!outcome.placingQueen) {
      sounds.playPlace();
      return;
    }

    if (!outcome.isValidMove) {
      sounds.playInvalid();
      pushToast({
        tone: 'error',
        title: 'Invalid move',
        message: 'This queen is attacked by another queen.'
      });
      return;
    }

    sounds.playPlace();
  };

  const handleSolve = async (event) => {
    event.preventDefault();
    await withLoading('Running solver comparison...', async () => {
      const data = await solveSixteenQueens({
        boardSize: size,
        threadCount: Number(solveConfig.threadCount),
        solutionSampleLimit: Number(solveConfig.solutionSampleLimit),
        persistSolutionLimit: Number(solveConfig.persistSolutionLimit),
        viewerRole
      });

      setApiData('solveResult', data);
      setApiData('sampleSolutions', data?.sampleSolutions || []);
      setApiData('selectedSampleIndex', 0);
      updateSubmitForm({ gameRoundId: data.gameRoundId });
      setRoundClosed(false);

      const firstSample = parseAnswerString(data?.sampleSolutions?.[0], size);
      if (firstSample) {
        loadBoard(firstSample);
      }

      setStatusMessage('Solver completed. Puzzle round stored successfully.');
      pushToast({
        tone: 'success',
        title: 'Solver complete',
        message: `Round ${data.gameRoundId} finished with speedup ${Number(data.speedup || 0).toFixed(2)}x.`
      });
    });
  };

  const handleSubmit = async (event) => {
    event.preventDefault();

    if (!canSubmit) {
      pushToast({
        tone: 'warning',
        title: 'Board not ready',
        message: 'Place 16 queens and remove conflicts before submitting.'
      });
      return;
    }

    await withLoading('Submitting answer...', async () => {
      const payload = {
        playerName: submitForm.playerName.trim(),
        boardSize: size,
        answer: answerFromGrid
      };

      if (submitForm.gameRoundId) {
        payload.gameRoundId = Number(submitForm.gameRoundId);
      }

      const data = await submitSixteenQueens(payload);
      setApiData('submitResult', data);

      if (!data.correct) {
        setApiData('gameState', 'invalid_move');
        sounds.playInvalid();
        pushToast({ tone: 'error', title: 'Incorrect solution', message: data.message || 'Try another arrangement.' });
        return;
      }

      if (data.alreadyRecognized) {
        setApiData('gameState', 'duplicate_solution');
        pushToast({
          tone: 'warning',
          title: 'Already discovered',
          message: 'This valid solution was already recognized. Try another arrangement!'
        });
        return;
      }

      setApiData('gameState', 'correct_solution_found');
      setCelebration('confetti');
      sounds.playSuccess();
      pushToast({ tone: 'success', title: 'Unique solution found', message: 'Your answer is now recognized globally.' });
      await checkAllCompleted();
      await Promise.all([loadHistory(), loadLeaderboard()]);
    });
  };

  const loadHistory = async () => {
    const data = await fetchSixteenQueensHistory(20);
    setApiData('historyResult', data);
    setStatusMessage('History synced.');
    return data;
  };

  const loadLeaderboard = async () => {
    const data = await fetchSixteenQueensLeaderboard(LEADERBOARD_LIMIT);
    setApiData('leaderboardResult', data);
    setStatusMessage('Leaderboard synced.');
    return data;
  };

  const loadReport = async () => {
    const data = await fetchSixteenQueensReport();
    setApiData('reportResult', data);
    setStatusMessage('Report synced.');
    return data;
  };

  const handleLoadSample = async () => {
    await withLoading('Loading sample solution...', async () => {
      const selectedRoundId = submitForm.gameRoundId ? Number(submitForm.gameRoundId) : solveResult?.gameRoundId;

      let nextSamples = sampleSolutions;
      if (!nextSamples?.length && selectedRoundId) {
        const response = await fetchSixteenQueensSamples(selectedRoundId, SAMPLE_LIMIT, viewerRole);
        if (response?.samplesVisible === false) {
          pushToast({
            tone: 'warning',
            title: 'Sample hidden',
            message: response?.message || 'Players can access samples only after round close.'
          });
          return;
        }
        nextSamples = response?.sampleSolutions || [];
        setApiData('sampleSolutions', nextSamples);
      }

      setApiData('selectedSampleIndex', 0);

      const parsed = parseAnswerString(nextSamples?.[0], size);
      if (!parsed) {
        pushToast({ tone: 'warning', title: 'No sample', message: 'Run solver first to generate sample solutions.' });
        return;
      }

      loadBoard(parsed);
      setStatusMessage('Sample loaded to board.');
      pushToast({ tone: 'info', title: 'Sample loaded', message: 'A known valid arrangement is now on the board.' });
    });
  };

  const handleCloseRound = async () => {
    if (viewerRole === 'PLAYER') {
      pushToast({ tone: 'warning', title: 'Permission denied', message: 'Only admin can close puzzle rounds.' });
      return;
    }

    await withLoading('Closing round...', async () => {
      const selectedRoundId = submitForm.gameRoundId ? Number(submitForm.gameRoundId) : solveResult?.gameRoundId;
      if (!selectedRoundId) {
        throw new Error('Select a round or run solver before closing a round.');
      }
      await closeSixteenQueensRound(selectedRoundId);
      setRoundClosed(true);
      pushToast({ tone: 'success', title: 'Round closed', message: `Round ${selectedRoundId} is now closed for play.` });
    });
  };

  const handleRefreshDashboards = async () => {
    await withLoading('Refreshing dashboards...', async () => {
      await Promise.all([loadHistory(), loadLeaderboard(), loadReport()]);
      pushToast({ tone: 'info', title: 'Dashboard refreshed', message: 'History, leaderboard, and report are up to date.' });
    });
  };

  const handleResetRecognized = async () => {
    await withLoading('Resetting recognized solutions...', async () => {
      const selectedRoundId = submitForm.gameRoundId ? Number(submitForm.gameRoundId) : solveResult?.gameRoundId;
      await resetSixteenQueensRecognized(selectedRoundId);
      setApiData('gameState', 'playing');
      setCelebration(null);
      pushToast({ tone: 'success', title: 'Recognized reset', message: 'Recognition state reset for the selected round.' });
      await Promise.all([loadLeaderboard(), loadReport()]);
    });
  };

  const sampleSolutionsPanel = sampleSolutions?.length > 0 && (
    <section className="sample-solutions-panel">
      <h3>Sample Solution Types ({sampleSolutions.length})</h3>
      <p>Admin solve generated these patterns. Click a pattern to display it on the board.</p>
      <div className="sample-solutions-grid">
        {sampleSolutions.map((sample, index) => {
          const previewColumns = parseAnswerString(sample, size);

          return (
            <button
              type="button"
              key={`sample-${index}`}
              className={`sample-chip ${selectedSampleIndex === index ? 'active' : ''}`}
              onClick={() => handleSelectSample(index)}
              title={sample}
            >
              <div className="sample-chip-content">
                <div className="sample-mini-board" aria-hidden="true">
                  {Array.from({ length: size }).map((_, row) => (
                    Array.from({ length: size }).map((__, col) => {
                      const hasQueen = previewColumns?.[row] === col;
                      const isDark = (row + col) % 2 === 1;

                      return (
                        <span
                          key={`mini-${index}-${row}-${col}`}
                          className={`mini-cell ${isDark ? 'dark' : 'light'} ${hasQueen ? 'queen' : ''}`}
                        />
                      );
                    })
                  ))}
                </div>

                <div className="sample-chip-text">
                  <span>Pattern {index + 1}</span>
                  <small>{sample}</small>
                </div>
              </div>
            </button>
          );
        })}
      </div>
    </section>
  );

  return (
    <main className={`queens-shell ${gameState}`}>
      <ToastStack toasts={toasts} onDismiss={removeToast} />

      <header className="queens-hero">
        <div>
          <button className="ghost-action" onClick={() => navigate('/')}>Back to dashboard</button>
          <p className="eyebrow">Sixteen Queens Experience</p>
          <h1>Interactive Sixteen Queens' Puzzle</h1>
          <p className="hero-copy">
            Build a conflict-free queen layout with instant visual validation, then race the global leaderboard.
          </p>
        </div>

        <div className="status-card">
          <span>Live State</span>
          <strong>{statusMessage}</strong>
          <small>
            {boardStatus === 'valid' ? '✔ Board ready' : boardStatus === 'conflict' ? '✖ Conflict detected' : '... Place 16 queens'}
          </small>
        </div>
      </header>

      <section className="game-instructions" aria-label="How to play Sixteen Queens">
        <h3>How to Play</h3>
        <ol>
          <li>Place one queen in each row.</li>
          <li>Make sure no queens attack each other.</li>
          <li>Use Clear board if you want to restart quickly.</li>
          <li>Submit when all 16 queens are safe.</li>
        </ol>
      </section>

      <section className="queens-grid">
        <article className="panel board-panel">
          <div className="panel-header-inline">
            <h2>Chessboard</h2>
            <div className="chip-row">
              <span className="info-chip">Queens: {analysis.queensPlaced}/{size}</span>
              <span className={`info-chip ${roundClosed ? 'ok' : 'warn'}`}>Round: {roundClosed ? 'Closed' : 'Active'}</span>
            </div>
          </div>

          <ChessBoard
            size={size}
            board={board}
            analysis={analysis}
            hoverCell={hoverCell}
            onHoverCell={setHoverCell}
            onCellClick={handleCellClick}
            invalidMoveCellKey={invalidMoveCellKey}
            lastMoveCellKey={lastMoveCellKey}
          />

          <p className={`board-state-text ${boardStatus === 'conflict' ? 'danger' : boardStatus === 'valid' ? 'good' : ''}`}>
            {boardStatus === 'incomplete' && 'Place one queen in each row. Hover any tile to preview conflicts.'}
            {boardStatus === 'conflict' && 'Conflicts highlighted in red lanes and diagonal danger glow.'}
            {boardStatus === 'valid' && 'Perfect board. Submit to verify whether this is a new unique solution.'}
          </p>

          <div className="board-actions">
            <button className="secondary" type="button" onClick={clearBoard} disabled={loading}>Clear board</button>
            <button className="secondary" type="button" onClick={handleLoadSample} disabled={loading}>Load sample patterns</button>
            <button type="button" onClick={handleRefreshDashboards} disabled={loading}>Refresh dashboards</button>
          </div>

          {!showSamplesInControls && sampleSolutionsPanel}
        </article>

        <article className="panel control-panel">
          <h2>Round Controls</h2>
          {showSamplesInControls && sampleSolutionsPanel}
          <form className="form-grid" onSubmit={handleSolve}>
            <label>
              Viewer role
              <select value={viewerRole} onChange={(event) => setViewerRole(event.target.value)}>
                <option value="PLAYER">Player</option>
                <option value="ADMIN">Admin</option>
              </select>
            </label>

            <label>
              Thread count
              <input
                type="number"
                min="1"
                max="64"
                value={solveConfig.threadCount}
                onChange={(event) => updateSolveConfig({ threadCount: event.target.value })}
              />
            </label>

            <label>
              Sample limit
              <input
                type="number"
                min="1"
                max="500"
                value={solveConfig.solutionSampleLimit}
                onChange={(event) => updateSolveConfig({ solutionSampleLimit: event.target.value })}
              />
            </label>

            <label>
              Persist top N
              <input
                type="number"
                min="1"
                max="5000"
                value={solveConfig.persistSolutionLimit}
                onChange={(event) => updateSolveConfig({ persistSolutionLimit: event.target.value })}
              />
            </label>

            <button type="submit" className="full-width" disabled={loading}>Run solver</button>
          </form>

          <form className="form-grid" onSubmit={handleSubmit}>
            <label>
              Player name
              <input
                value={submitForm.playerName}
                onChange={(event) => updateSubmitForm({ playerName: event.target.value })}
                placeholder="Player alias"
              />
            </label>

            <label>
              Round ID
              <input
                type="number"
                value={submitForm.gameRoundId}
                onChange={(event) => updateSubmitForm({ gameRoundId: event.target.value })}
                placeholder="optional"
              />
            </label>

            <label className="full-width">
              Board signature
              <input readOnly value={answerFromGrid || 'Incomplete board'} />
            </label>

            <button className="full-width" type="submit" disabled={loading || !canSubmit}>Submit arrangement</button>
          </form>

          <div className="control-actions">
            <button type="button" className="secondary" onClick={handleCloseRound} disabled={loading}>Close round</button>
            <button type="button" className="secondary" onClick={handleResetRecognized} disabled={loading}>Reset recognized</button>
          </div>

          {solveResult && (
            <div className="solve-summary">
              <h3>Latest solve run</h3>
              <ul>
                <li>Round ID: {solveResult.gameRoundId}</li>
                <li>Sequential time: {solveResult.sequentialTimeMs} ms</li>
                <li>Threaded time: {solveResult.parallelTimeMs} ms</li>
                <li>Speedup: {Number(solveResult.speedup || 0).toFixed(2)}x</li>
              </ul>
            </div>
          )}

          {submitResult && (
            <div className={`submit-summary ${submitResult.correct ? (submitResult.alreadyRecognized ? 'duplicate' : 'success') : 'error'}`}>
              <h3>Submission result</h3>
              <p>{submitResult.message}</p>
            </div>
          )}

          {errorMessage && <p className="error-banner">{errorMessage}</p>}
        </article>
      </section>

      <section className="dashboard-layout">
        <article className="panel">
          <h2>Leaderboard</h2>
          <LeaderboardTable leaderboardResult={leaderboardResult} />
        </article>

        <article className="panel">
          <h2>History Timeline</h2>
          <HistoryTimeline historyResult={historyResult} />
        </article>

        <article className="panel">
          <h2>Reports Dashboard</h2>
          <ReportsDashboard
            reportResult={reportResult}
            historyResult={historyResult}
            leaderboardResult={leaderboardResult}
          />
        </article>
      </section>

      <AnimatePresence>
        {celebration === 'confetti' && (
          <motion.div className="celebration-overlay" initial={{ opacity: 0 }} animate={{ opacity: 1 }} exit={{ opacity: 0 }}>
            <div className="confetti-field" aria-hidden="true">
              {Array.from({ length: 42 }).map((_, index) => (
                <span key={`confetti-${index}`} style={{ left: `${(index * 9) % 100}%`, animationDelay: `${(index % 12) * 0.1}s` }} />
              ))}
            </div>
            <div className="celebration-modal">
              <h3>You found a unique solution!</h3>
              <p>The board is valid and newly recognized in this round.</p>
              <button type="button" onClick={() => setCelebration(null)}>Continue</button>
            </div>
          </motion.div>
        )}
      </AnimatePresence>

      <AnimatePresence>
        {celebration === 'fireworks' && (
          <motion.div className="celebration-overlay" initial={{ opacity: 0 }} animate={{ opacity: 1 }} exit={{ opacity: 0 }}>
            <div className="fireworks-field" aria-hidden="true">
              {Array.from({ length: 5 }).map((_, idx) => (
                <span key={`fireworks-${idx}`} style={{ left: `${16 + idx * 17}%`, animationDelay: `${idx * 0.25}s` }} />
              ))}
            </div>
            <div className="celebration-modal">
              <h3>All solutions completed</h3>
              <p>Every persisted solution has been recognized. Reset when you want a new challenge cycle.</p>
              <button type="button" onClick={() => setCelebration(null)}>Close</button>
            </div>
          </motion.div>
        )}
      </AnimatePresence>
    </main>
  );
}

export default SixteenQueensPage;
