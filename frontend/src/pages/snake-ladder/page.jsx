import { useState, useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { solveSnakeLadder, submitSnakeLadder } from '../../api/snakeLadderApi';

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
      setStatus(result.isCorrect ? 'Correct! Well done.' : 'Incorrect. Try again.');
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
            onClick={() => navigate('/games/snake-ladder/leaderboard')}
            title="View leaderboard"
            style={{
              padding: '8px 16px',
              backgroundColor: '#4CAF50',
              color: 'white',
              border: 'none',
              borderRadius: '4px',
              cursor: 'pointer',
              fontSize: '14px',
              fontWeight: '500'
            }}
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
            <h2>{submitResult.message}</h2>
            {!submitResult.isCorrect && <p>Correct answer: {submitResult.correctAnswer}</p>}
          </div>
        </section>
      )}
    </main>
  );
}

export default SnakeLadderPage;