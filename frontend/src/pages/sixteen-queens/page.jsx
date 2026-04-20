import { useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
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

const BOARD_SIZE = 16;
const SAMPLE_LIMIT = 8;

const defaultSolveForm = {
  threadCount: 4,
  solutionSampleLimit: 8,
  persistSolutionLimit: 100
};

const defaultSubmitForm = {
  playerName: '',
  gameRoundId: ''
};

const emptyBoard = () => Array(BOARD_SIZE).fill(-1);

const computeLocalConflicts = (queenColumns) => {
  const pairs = [];
  for (let rowA = 0; rowA < BOARD_SIZE; rowA += 1) {
    for (let rowB = rowA + 1; rowB < BOARD_SIZE; rowB += 1) {
      const colA = queenColumns[rowA];
      const colB = queenColumns[rowB];
      if (colA < 0 || colB < 0) {
        continue;
      }

      if (colA === colB || Math.abs(rowA - rowB) === Math.abs(colA - colB)) {
        pairs.push({ rowA, colA, rowB, colB });
      }
    }
  }
  return pairs;
};

function SixteenQueensPage() {
  const navigate = useNavigate();
  const [viewerRole, setViewerRole] = useState('PLAYER');
  const [solveForm, setSolveForm] = useState(defaultSolveForm);
  const [submitForm, setSubmitForm] = useState(defaultSubmitForm);
  const [queenColumns, setQueenColumns] = useState(emptyBoard);
  const [solveResult, setSolveResult] = useState(null);
  const [submitResult, setSubmitResult] = useState(null);
  const [historyResult, setHistoryResult] = useState(null);
  const [leaderboardResult, setLeaderboardResult] = useState(null);
  const [reportResult, setReportResult] = useState(null);
  const [isRoundClosed, setIsRoundClosed] = useState(false);
  const [status, setStatus] = useState('Ready');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const answerFromGrid = useMemo(() => {
    if (queenColumns.some((col) => col < 0)) {
      return '';
    }
    return queenColumns.join(',');
  }, [queenColumns]);

  const conflicts = useMemo(() => computeLocalConflicts(queenColumns), [queenColumns]);
  const canSubmit = useMemo(
    () => submitForm.playerName.trim() && answerFromGrid && conflicts.length === 0,
    [submitForm, answerFromGrid, conflicts.length]
  );

  const toggleQueen = (row, col) => {
    setQueenColumns((current) => {
      const next = [...current];
      next[row] = current[row] === col ? -1 : col;
      return next;
    });
  };

  const clearGrid = () => {
    setQueenColumns(emptyBoard());
  };

  const loadSampleToGrid = async () => {
    const selectedRoundId = submitForm.gameRoundId !== ''
      ? Number(submitForm.gameRoundId)
      : solveResult?.gameRoundId;

    if (viewerRole === 'PLAYER' && !isRoundClosed) {
      setError('Samples are hidden for players while the round is active. Ask admin to close the round first.');
      return;
    }

    setLoading(true);
    setError('');
    setStatus('Loading sample solutions...');

    try {
      let firstSample = solveResult?.sampleSolutions?.[0];
      if (!firstSample && selectedRoundId) {
        const data = await fetchSixteenQueensSamples(selectedRoundId, SAMPLE_LIMIT, viewerRole);
        firstSample = data?.sampleSolutions?.[0];
        if (data?.samplesVisible === false) {
          setError(data?.message || 'Samples are currently hidden for players.');
          setStatus('Sample load blocked');
          return;
        }
      }

      if (!firstSample) {
        setError('Run solver first to get sample solutions.');
        setStatus('Sample load failed');
        return;
      }

      const parsed = firstSample.split(',').map((value) => Number(value));
      if (parsed.length !== BOARD_SIZE || parsed.some((value) => Number.isNaN(value) || value < 0 || value >= BOARD_SIZE)) {
        setError('Unable to load sample solution into grid.');
        return;
      }

      setQueenColumns(parsed);
      setError('');
      setStatus('Loaded first sample solution into the grid.');
    } catch (requestError) {
      const message = requestError?.response?.data?.message || requestError.message || 'Sample load failed';
      setError(message);
      setStatus('Sample load failed');
    } finally {
      setLoading(false);
    }
  };

  const handleCloseRound = async () => {
    const selectedRoundId = submitForm.gameRoundId !== ''
      ? Number(submitForm.gameRoundId)
      : (solveResult?.gameRoundId || undefined);

    if (!selectedRoundId) {
      setError('No round selected to close. Run solve first or provide round ID.');
      return;
    }

    if (viewerRole === 'PLAYER') {
      setError('Only admin can close rounds.');
      return;
    }

    if (!window.confirm(`Close round ${selectedRoundId}? Players will be able to view sample solutions after this.`)) {
      return;
    }

    setLoading(true);
    setError('');
    setStatus('Closing round...');
    try {
      const data = await closeSixteenQueensRound(selectedRoundId);
      setIsRoundClosed(true);
      setStatus(data?.message || `Round ${selectedRoundId} closed.`);
    } catch (requestError) {
      const message = requestError?.response?.data?.message || requestError.message || 'Close round failed';
      setError(message);
      setStatus('Close round failed');
    } finally {
      setLoading(false);
    }
  };

  const handleSolve = async (event) => {
    event.preventDefault();
    setLoading(true);
    setError('');
    setStatus('Running solve round...');
    try {
      const data = await solveSixteenQueens({
        boardSize: BOARD_SIZE,
        threadCount: Number(solveForm.threadCount),
        solutionSampleLimit: Number(solveForm.solutionSampleLimit),
        persistSolutionLimit: Number(solveForm.persistSolutionLimit),
        viewerRole
      });
      setSolveResult(data);
      setIsRoundClosed(false);
      setSubmitForm((current) => ({
        ...current,
        gameRoundId: data.gameRoundId || current.gameRoundId
      }));
      setStatus('Solve completed and saved to SQLite.');
    } catch (requestError) {
      const message = requestError?.code === 'ECONNABORTED'
        ? 'Solve timed out on client side. Keep backend running and try again; the request timeout is now set to 300 seconds.'
        : (requestError?.response?.data?.message || requestError.message || 'Solve failed');
      setError(message);
      setStatus('Solve failed');
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setLoading(true);
    setError('');
    setStatus('Submitting answer...');
    try {
      const payload = {
        playerName: submitForm.playerName.trim(),
        boardSize: BOARD_SIZE,
        answer: answerFromGrid
      };

      if (submitForm.gameRoundId !== '') {
        payload.gameRoundId = Number(submitForm.gameRoundId);
      }

      const data = await submitSixteenQueens(payload);
      setSubmitResult(data);
      setStatus('Submission stored in SQLite.');
    } catch (requestError) {
      const message = requestError?.response?.data?.message || requestError.message || 'Submit failed';
      setError(message);
      setStatus('Submit failed');
    } finally {
      setLoading(false);
    }
  };

  const loadHistory = async () => {
    setLoading(true);
    setError('');
    setStatus('Loading history from SQLite...');
    try {
      const data = await fetchSixteenQueensHistory(10);
      setHistoryResult(data);
      setStatus('History loaded.');
    } catch (requestError) {
      const message = requestError?.response?.data?.message || requestError.message || 'History load failed';
      setError(message);
      setStatus('History failed');
    } finally {
      setLoading(false);
    }
  };

  const loadLeaderboard = async () => {
    setLoading(true);
    setError('');
    setStatus('Loading leaderboard...');
    try {
      const selectedRoundId = submitForm.gameRoundId !== ''
        ? Number(submitForm.gameRoundId)
        : (solveResult?.gameRoundId || undefined);
      const data = await fetchSixteenQueensLeaderboard(10, selectedRoundId);
      setLeaderboardResult(data);
      setStatus(`Leaderboard loaded${data?.roundId ? ` for round ${data.roundId}` : ''}.`);
    } catch (requestError) {
      const message = requestError?.response?.data?.message || requestError.message || 'Leaderboard load failed';
      setError(message);
      setStatus('Leaderboard failed');
    } finally {
      setLoading(false);
    }
  };

  const handleResetRecognized = async () => {
    const selectedRoundId = submitForm.gameRoundId !== ''
      ? Number(submitForm.gameRoundId)
      : (solveResult?.gameRoundId || undefined);

    if (!window.confirm(`Reset recognized solutions${selectedRoundId ? ` for round ${selectedRoundId}` : ''}?`)) {
      return;
    }

    setLoading(true);
    setError('');
    setStatus('Resetting recognized solutions...');
    try {
      const data = await resetSixteenQueensRecognized(selectedRoundId);
      setStatus(data?.message || 'Recognized solutions reset.');
      setSubmitResult(null);
      setLeaderboardResult(null);
    } catch (requestError) {
      const message = requestError?.response?.data?.message || requestError.message || 'Reset failed';
      setError(message);
      setStatus('Reset failed');
    } finally {
      setLoading(false);
    }
  };

  const loadReport = async () => {
    setLoading(true);
    setError('');
    setStatus('Loading report summary...');
    try {
      const data = await fetchSixteenQueensReport();
      setReportResult(data);
      setStatus('Report loaded.');
    } catch (requestError) {
      const message = requestError?.response?.data?.message || requestError.message || 'Report load failed';
      setError(message);
      setStatus('Report failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <main className="shell">
      <section className="hero">
        <div>
          <button
            className="back-button"
            onClick={() => navigate('/')}
            title="Back to dashboard"
          >
            ← Dashboard
          </button>
          <p className="eyebrow">Sixteen Queens Challenge</p>
          <h1>16x16 Queen Placement</h1>
          <p className="subtitle">
            Place 16 queens on a 16x16 board so no two queens attack each other.
            Solve uses bitmask backtracking algorithm with optional parallelization.
          </p>
        </div>
        <div className="status-card">
          <span>Status</span>
          <strong>{status}</strong>
        </div>
      </section>

      <section className="grid-layout">
        <article className="panel">
          <h2>Run Solver</h2>
          <p className="info-chip">Board Size: 16 x 16 (fixed)</p>
          <form onSubmit={handleSolve} className="form-grid">
            <label>
              Viewer Role
              <select
                value={viewerRole}
                onChange={(e) => setViewerRole(e.target.value)}
              >
                <option value="PLAYER">Player</option>
                <option value="ADMIN">Admin</option>
              </select>
            </label>
            <label>
              Thread Count
              <input
                type="number"
                min="1"
                max="64"
                value={solveForm.threadCount}
                onChange={(e) => setSolveForm({ ...solveForm, threadCount: e.target.value })}
              />
            </label>
            <label>
              Sample Limit
              <input
                type="number"
                min="1"
                max="500"
                value={solveForm.solutionSampleLimit}
                onChange={(e) => setSolveForm({ ...solveForm, solutionSampleLimit: e.target.value })}
              />
            </label>
            <label>
              Persist Top N
              <input
                type="number"
                min="0"
                max="5000"
                value={solveForm.persistSolutionLimit}
                onChange={(e) => setSolveForm({ ...solveForm, persistSolutionLimit: e.target.value })}
              />
            </label>
            <button type="submit" disabled={loading}>Solve & Save</button>
          </form>
          {solveResult && <ResultBox title="Solve Result" data={solveResult} />}
        </article>

        <article className="panel">
          <h2>Submit Answer</h2>
          <form onSubmit={handleSubmit} className="form-grid">
            <label>
              Player Name
              <input
                value={submitForm.playerName}
                onChange={(e) => setSubmitForm({ ...submitForm, playerName: e.target.value })}
                placeholder="Sandamini"
              />
            </label>
            <label>
              Game Round ID
              <input
                type="number"
                value={submitForm.gameRoundId}
                onChange={(e) => setSubmitForm({ ...submitForm, gameRoundId: e.target.value })}
                placeholder="optional"
              />
            </label>
            <label>
              Board Size
              <input value="16 x 16 (fixed)" readOnly />
            </label>
            <label className="full-width">
              Answer from Grid
              <input value={answerFromGrid || 'Place one queen in each row'} readOnly />
            </label>
            <button type="submit" disabled={loading || !canSubmit}>Submit</button>
          </form>

          <div className="board-actions">
            <button type="button" onClick={clearGrid} disabled={loading}>Clear Grid</button>
            <button type="button" onClick={loadSampleToGrid} disabled={loading}>Load Sample to Grid</button>
          </div>

          <p className="grid-hint">
            Sample visibility policy: Admin can always load samples. Players can load samples only after round close.
            Current round status: {isRoundClosed ? 'Closed' : 'Active'}.
          </p>

          <div className="grid-board" role="grid" aria-label="Sixteen queens board">
            {Array.from({ length: BOARD_SIZE }).map((_, row) => (
              <div key={`row-${row}`} className="grid-row" role="row">
                {Array.from({ length: BOARD_SIZE }).map((__, col) => {
                  const selected = queenColumns[row] === col;
                  const dark = (row + col) % 2 === 1;
                  return (
                    <button
                      type="button"
                      key={`cell-${row}-${col}`}
                      className={`grid-cell ${dark ? 'dark' : 'light'} ${selected ? 'selected' : ''}`}
                      onClick={() => toggleQueen(row, col)}
                      title={`Row ${row + 1}, Column ${col + 1}`}
                      aria-label={`row ${row + 1}, column ${col + 1}${selected ? ', queen selected' : ''}`}
                    >
                      {selected ? 'Q' : ''}
                    </button>
                  );
                })}
              </div>
            ))}
          </div>

          <p className={`grid-hint ${conflicts.length > 0 ? 'warn' : ''}`}>
            {conflicts.length > 0
              ? `Grid has ${conflicts.length} conflict(s). Adjust queen positions before submit.`
              : answerFromGrid
                ? 'Grid is conflict-free and ready to submit.'
                : 'Place exactly one queen per row to build your answer.'}
          </p>

          {submitResult && <ResultBox title="Submission Result" data={submitResult} />}
        </article>
      </section>

      <section className="actions-row">
        <button onClick={loadHistory} disabled={loading}>Load History</button>
        <button onClick={loadLeaderboard} disabled={loading}>Load Leaderboard</button>
        <button onClick={loadReport} disabled={loading}>Load Report</button>
        <button onClick={handleCloseRound} disabled={loading || viewerRole === 'PLAYER'}>Close Round</button>
        <button onClick={handleResetRecognized} disabled={loading}>Reset Recognized</button>
      </section>

      <section className="grid-layout three-cols">
        <article className="panel">
          <h2>History</h2>
          <DataList data={historyResult} emptyLabel="No history loaded yet." />
        </article>
        <article className="panel">
          <h2>Leaderboard</h2>
          <DataList data={leaderboardResult} emptyLabel="No leaderboard loaded yet." />
        </article>
        <article className="panel">
          <h2>Report</h2>
          <DataList data={reportResult} emptyLabel="No report loaded yet." />
        </article>
      </section>

      {error && <p className="error-banner">{error}</p>}
    </main>
  );
}

function ResultBox({ title, data }) {
  return (
    <div className="result-box">
      <h3>{title}</h3>
      <pre>{JSON.stringify(data, null, 2)}</pre>
    </div>
  );
}

function DataList({ data, emptyLabel }) {
  if (!data) {
    return <p className="empty-state">{emptyLabel}</p>;
  }

  return <pre className="data-block">{JSON.stringify(data, null, 2)}</pre>;
}

export default SixteenQueensPage;
