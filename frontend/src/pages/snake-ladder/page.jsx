import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
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
  const [solveForm, setSolveForm] = useState(defaultSolveForm);
  const [submitForm, setSubmitForm] = useState(defaultSubmitForm);
  const [solveResult, setSolveResult] = useState(null);
  const [submitResult, setSubmitResult] = useState(null);
  const [status, setStatus] = useState('Ready');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

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
    <div className="snake-ladder-page">
      <h1>Snake and Ladder Game</h1>

      <div className="solve-section">
        <h2>Generate Board</h2>
        <div className="form-group">
          <label>Board Size (6-12):</label>
          <input
            type="number"
            min="6"
            max="12"
            value={solveForm.boardSize}
            onChange={(e) => setSolveForm(prev => ({ ...prev, boardSize: parseInt(e.target.value) }))}
          />
        </div>
        <button onClick={handleSolve} disabled={loading}>Generate Board</button>
      </div>

      {solveResult && (
        <div className="board-section">
          <h2>Board</h2>
          {renderBoard()}
          <div className="algorithm-results">
            <p>BFS Time: {solveResult.bfsTimeMs}ms</p>
            <p>DP Time: {solveResult.dpTimeMs}ms</p>
          </div>
        </div>
      )}

      {solveResult && (
        <div className="answer-section">
          <h2>What is the minimum number of dice throws to reach the end?</h2>
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
          <div className="form-group">
            <label>Your Name:</label>
            <input
              type="text"
              value={submitForm.playerName}
              onChange={(e) => setSubmitForm(prev => ({ ...prev, playerName: e.target.value }))}
            />
          </div>
          <button onClick={handleSubmit} disabled={loading || !submitForm.playerName.trim() || !submitForm.answer}>
            Submit Answer
          </button>
        </div>
      )}

      {submitResult && (
        <div className={`result ${submitResult.isCorrect ? 'success' : 'failure'}`}>
          <h2>{submitResult.isCorrect ? 'Correct!' : 'Incorrect'}</h2>
          <p>{submitResult.message}</p>
          {!submitResult.isCorrect && <p>Correct answer: {submitResult.correctAnswer}</p>}
        </div>
      )}

      <div className="status">
        <p>Status: {status}</p>
        {error && <p className="error">Error: {error}</p>}
      </div>

      <button onClick={() => navigate('/dashboard')}>Back to Dashboard</button>
    </div>
  );
}

export default SnakeLadderPage;