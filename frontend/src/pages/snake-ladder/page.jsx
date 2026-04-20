import { useState, useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { solveSnakeLadder, submitSnakeLadder, getSnakeLadderLeaderboard } from '../../api/snakeLadderApi';

const defaultSolveForm = {
  boardSize: 8
};

const defaultSubmitForm = {
  playerName: '',
  answer: null
};

function SnakeLadderPage() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const [solveForm, setSolveForm] = useState(defaultSolveForm);
  const [submitForm, setSubmitForm] = useState(defaultSubmitForm);
  const [solveResult, setSolveResult] = useState(null);
  const [submitResult, setSubmitResult] = useState(null);
  const [status, setStatus] = useState('Ready');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [leaderboardOpen, setLeaderboardOpen] = useState(false);
  const [leaderboard, setLeaderboard] = useState([]);
  const [leaderboardLoading, setLeaderboardLoading] = useState(false);
  const [leaderboardError, setLeaderboardError] = useState('');

  useEffect(() => {
    const playerName = searchParams.get('player');
    if (playerName) {
      setSubmitForm(prev => ({ ...prev, playerName }));
    }
  }, [searchParams]);

  const handleSolve = async () => {
    if (solveForm.boardSize < 6 || solveForm.boardSize > 12) {
      setError('Board size must be between 6 and 12');
      return;
    }
    setLoading(true);
    setError('');
    setStatus('Solving...');
    try {
      const result = await solveSnakeLadder(solveForm);
      setSolveResult(result);
      setSubmitForm(prev => ({ ...prev, gameRoundId: result.gameRoundId }));
      setStatus('Board generated. Choose the minimum throws.');
    } catch (err) {
      setError(err.response?.data?.message || err.message);
      setStatus('Error occurred.');
    } finally {
      setLoading(false);
    }
  };

  const fetchLeaderboard = async () => {
    setLeaderboardError('');
    setLeaderboardLoading(true);
    try {
      const data = await getSnakeLadderLeaderboard(50);
      setLeaderboard(data.leaderboard || []);
    } catch (err) {
      setLeaderboardError(err.response?.data?.message || err.message || 'Failed to load leaderboard');
      setLeaderboard([]);
    } finally {
      setLeaderboardLoading(false);
    }
  };

  const openLeaderboard = async () => {
    setLeaderboardOpen(true);
    await fetchLeaderboard();
  };

  const closeLeaderboard = () => {
    setLeaderboardOpen(false);
  };

  const handleSubmit = async () => {
    if (!submitForm.answer) {
      setError('Please select an answer.');
      return;
    }
    setLoading(true);
    setError('');
    setStatus('Submitting...');
    try {
      const payload = {
        gameRoundId: solveResult.gameRoundId,
        playerName: submitForm.playerName,
        answer: submitForm.answer,
        boardSize: solveForm.boardSize
      };
      const result = await submitSnakeLadder(payload);
      setSubmitResult(result);
      if (result.outcome === 'WIN') {
        setStatus('Win! Correct answer identified.');
      } else if (result.outcome === 'DRAW') {
        setStatus('Draw! Very close, try once more.');
      } else {
        setStatus('Lose! Incorrect answer, try again.');
      }
    } catch (err) {
      setError(err.response?.data?.message || err.message);
      setStatus('Error occurred.');
    } finally {
      setLoading(false);
    }
  };

  const renderBoard = () => {
    if (!solveResult) return null;

    const n = solveResult.boardSize;
    const ladders = solveResult.ladders || [];
    const snakes = solveResult.snakes || [];

    const board = [];
    for (let row = n - 1; row >= 0; row--) {
      const cells = [];
      for (let col = 0; col < n; col++) {
        let cellNum;
        if (row % 2 === 0) {
          // Even rows (from bottom): left to right
          cellNum = row * n + col + 1;
        } else {
          // Odd rows: right to left
          cellNum = row * n + n - col;
        }
        let cellClass = 'cell';
        let content = cellNum;

        if (cellNum === 1) cellClass += ' start';
        if (cellNum === n * n) cellClass += ' end';

        const ladder = ladders.find(l => l.start === cellNum);
        if (ladder) {
          cellClass += ' ladder-start';
          content = `↑${ladder.end}`;
        }

        const snake = snakes.find(s => s.head === cellNum);
        if (snake) {
          cellClass += ' snake-head';
          content = `↓${snake.tail}`;
        }

        cells.push(
          <div key={cellNum} className={cellClass}>
            {content}
          </div>
        );
      }
      board.push(<div key={row} className="board-row">{cells}</div>);
    }

    return <div className="board">{board}</div>;
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
          <p className="eyebrow">Snake and Ladder Challenge</p>
          <h1>Snake and Ladder Game</h1>
          <p className="subtitle">
            Generate a random {solveForm.boardSize}×{solveForm.boardSize} board with snakes and ladders,
            then choose the minimum dice throws from three algorithm-derived options.
          </p>
        </div>
        <div style={{ display: 'flex', flexDirection: 'column', gap: '10px', alignItems: 'flex-end' }}>
          <div className="status-card">
            <span>Status</span>
            <strong>{status}</strong>
          </div>
          <button
            className="leaderboard-button"
            onClick={openLeaderboard}
            title="View leaderboard"
            style={{
              padding: '10px 18px',
              backgroundColor: '#10b981',
              color: '#fff',
              border: 'none',
              borderRadius: '999px',
              cursor: 'pointer',
              fontSize: '14px',
              fontWeight: '600',
              boxShadow: '0 14px 30px rgba(16, 185, 129, 0.2)',
              transition: 'transform 150ms ease, box-shadow 150ms ease'
            }}
            onMouseEnter={(e) => e.currentTarget.style.transform = 'translateY(-1px)'}
            onMouseLeave={(e) => e.currentTarget.style.transform = 'translateY(0)'}
          >
            📊 Leaderboard
          </button>
        </div>
      </section>

      {submitForm.playerName && (
        <section className="welcome-message">
          <p>Welcome, <strong>{submitForm.playerName}</strong>! Ready to play Snake & Ladder?</p>
        </section>
      )}

      <div className="grid-layout">
        <section className="panel">
          <h2>Generate Board</h2>
          <div className="form-group" style={{ display: 'flex', alignItems: 'center', gap: '10px', justifyContent: 'space-between' }}>
            <label>Select Board Size (6-12):</label>
            <input
              type="number"
              min="6"
              max="12"
              value={solveForm.boardSize}
              onChange={(e) => setSolveForm(prev => ({ ...prev, boardSize: parseInt(e.target.value, 10) || 6 }))}
            />
            <button onClick={handleSolve} disabled={loading} style={{ width: 'auto' }}>Generate Board</button>
          </div>
          {error && <p className="error-banner">{error}</p>}
        </section>

        {solveResult && (
          <section className="panel">
            <h2>Board Preview</h2>
            {renderBoard()}
            <div className="algorithm-results">
              <p>BFS Time: {solveResult.bfsTimeMs}ms</p>
              <p>DP Time: {solveResult.dpTimeMs}ms</p>
            </div>
          </section>
        )}
      </div>

      {solveResult && (
        <section className="panel">

          <div className="form-group">
            <h2>Your Name: {submitForm.playerName}</h2>
          </div>

          <h2>Choose the Minimum Throws</h2>
          <div className="choices">
            {solveResult.choices.map((choice, index) => (
              <button
                key={index}
                className={submitForm.answer === choice ? 'selected' : ''}
                onClick={() => setSubmitForm(prev => ({ ...prev, answer: choice }))}
              >
                {choice}
              </button>
            ))}
          </div>
          <button onClick={handleSubmit} disabled={loading || !submitForm.playerName.trim() || !submitForm.answer}>
            Submit Answer
          </button>
        </section>
      )}

      {submitResult && (
        <section className="panel">
          <div>
            <p><strong>Outcome:</strong> {submitResult.outcome || (submitResult.isCorrect ? 'WIN' : 'LOSE')}</p>
            <h2>{submitResult.message}</h2>
            {!submitResult.isCorrect && <p>Correct answer: {submitResult.correctAnswer}</p>}
          </div>
        </section>
      )}

      {leaderboardOpen && (
        <div className="modal-overlay" onClick={closeLeaderboard}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h2>Snake & Ladder Leaderboard</h2>
              <button className="modal-close" onClick={closeLeaderboard}>&times;</button>
            </div>

            {leaderboardLoading && <p>Loading leaderboard...</p>}
            {leaderboardError && <p className="error-banner">{leaderboardError}</p>}

            {!leaderboardLoading && !leaderboardError && leaderboard.length === 0 && (
              <p>No leaderboard data available yet.</p>
            )}

            {!leaderboardLoading && leaderboard.length > 0 && (
              <div className="leaderboard-modal-table-wrapper">
                <table className="leaderboard-table">
                  <thead>
                    <tr>
                      <th>#</th>
                      <th>Player Name</th>
                      <th>Total Attempts</th>
                      <th>Correct Answers</th>
                      <th>Accuracy</th>
                      <th>Last Submitted</th>
                    </tr>
                  </thead>
                  <tbody>
                    {leaderboard.map((entry, index) => (
                      <tr key={entry.playerId}>
                        <td>{index + 1}</td>
                        <td>{entry.playerName}</td>
                        <td>{entry.totalAnswers}</td>
                        <td>{entry.correctAnswers}</td>
                        <td>{entry.accuracy.toFixed(2)}%</td>
                        <td>{new Date(entry.lastSubmittedAt).toLocaleDateString()}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        </div>
      )}
      <style>{`
       .modal-overlay {
          position: fixed;
          inset: 0;
          background: rgba(15, 23, 42, 0.72);
          backdrop-filter: blur(10px);
          display: flex;
          align-items: center;
          justify-content: center;
          z-index: 50;
          padding: 16px;
        }

        .modal-content {
          width: 75vw;              /* 3/4 of screen width */
          height: 75vh;             /* 3/4 of screen height */
          max-width: 1200px;        /* optional limit for large screens */
          max-height: 90vh;
          overflow-y: auto;

          background: linear-gradient(180deg, rgba(255,255,255,0.98) 0%, rgba(243, 246, 255, 0.96) 100%);
          border: 1px solid rgba(255, 255, 255, 0.8);
          border-radius: 24px;
          padding: 28px;
          box-shadow: 0 36px 90px rgba(15, 23, 42, 0.25);
          position: relative;
          backdrop-filter: blur(16px);
        }

        .modal-header {
          display: flex;
          align-items: center;
          justify-content: space-between;
          gap: 16px;
          margin-bottom: 22px;
          flex-wrap: wrap;
        }

        .modal-header h2 {
          margin: 0;
          font-size: 1.6rem;
          letter-spacing: -0.02em;
          color: #111827;
        }

        .modal-close {
          border: none;
          background: #111827;
          color: #ffffff;
          width: 38px;
          height: 38px;
          border-radius: 50%;
          display: grid;
          place-items: center;
          font-size: 22px;
          cursor: pointer;
          transition: transform 160ms ease, background-color 160ms ease;
        }

        .modal-close:hover {
          transform: scale(1.08);
          background-color: #1f2937;
        }

        .leaderboard-modal-table-wrapper {
          overflow-x: auto;
          border-radius: 18px;
          border: 1px solid rgba(148, 163, 184, 0.35);
          background: rgba(255, 255, 255, 0.86);
          padding: 2px;
        }

        .leaderboard-table {
          width: 100%;
          min-width: 720px;
          border-collapse: separate;
          border-spacing: 0;
          margin-top: 8px;
          font-size: 0.95rem;
        }

        .leaderboard-table th,
        .leaderboard-table td {
          padding: 14px 16px;
          border-bottom: 1px solid rgba(226, 232, 240, 0.9);
          text-align: left;
        }

        .leaderboard-table thead {
          background: linear-gradient(90deg, rgba(59, 130, 246, 0.1), rgba(16, 185, 129, 0.08));
          color: #0f172a;
          font-weight: 700;
          text-transform: uppercase;
          letter-spacing: 0.04em;
        }

        .leaderboard-table tbody tr {
          transition: background-color 180ms ease, transform 180ms ease;
        }

        .leaderboard-table tbody tr:hover {
          background-color: rgba(59, 130, 246, 0.06);
          transform: translateX(2px);
        }

        .leaderboard-table tbody tr:nth-child(odd) {
          background-color: rgba(255, 255, 255, 0.92);
        }

        .leaderboard-table tbody tr:nth-child(even) {
          background-color: rgba(248, 250, 252, 0.9);
        }

        .leaderboard-table td:first-child {
          font-weight: 700;
          color: #111827;
        }

        .leaderboard-table td:last-child {
          color: #475569;
        }

        .leaderboard-table td {
          color: #334155;
        }
      `}</style>
    </main>
  );
}

export default SnakeLadderPage;