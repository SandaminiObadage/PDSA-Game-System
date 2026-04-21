import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { startKnightGame } from '../../api/knightsTourApi';

function Home() {
  const navigate = useNavigate();
  const [boardSize, setBoardSize] = useState(8);
  const [algorithmType, setAlgorithmType] = useState('RANDOM');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleStart = async () => {
    setLoading(true);
    setError('');

    try {
      const response = await startKnightGame({ boardSize, algorithmType });
      localStorage.removeItem('knightPendingOutcome');
      localStorage.setItem(
        'knightGame',
        JSON.stringify({
          ...response,
          boardSize,
          moves: [response.startPosition]
        })
      );
      navigate('/game');
    } catch (requestError) {
      const message = requestError?.response?.data?.message || requestError.message || 'Unable to start game.';
      setError(message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <main className="shell">
      <section className="hero">
        <div>
          <button className="back-button" onClick={() => navigate('/')} title="Back to dashboard">
            ← Dashboard
          </button>
          <p className="eyebrow">Knight&apos;s Tour</p>
          <h1>Start New Game</h1>
          <p className="subtitle">Choose board size and algorithm, then begin your tour.</p>
        </div>
        <div className="status-card">
          <span>Status</span>
          <strong>{loading ? 'Starting game...' : 'Ready'}</strong>
        </div>
      </section>

      <section className="grid-layout">
        <article className="panel">
          <h2>Game Setup</h2>
          <form className="form-grid" onSubmit={(event) => { event.preventDefault(); handleStart(); }}>
            <label>
              Board Size
              <select value={boardSize} onChange={(event) => setBoardSize(Number(event.target.value))}>
                <option value={8}>8x8</option>
                <option value={16}>16x16</option>
              </select>
            </label>

            <label>
              Algorithm
              <select value={algorithmType} onChange={(event) => setAlgorithmType(event.target.value)}>
                <option value="RANDOM">Random</option>
                <option value="WARNSDORFF">Warnsdorff</option>
                <option value="BACKTRACKING">Backtracking</option>
              </select>
            </label>

            <button type="submit" disabled={loading}>
              {loading ? 'Starting...' : 'Start Game'}
            </button>
          </form>
          {error && <p className="error-banner">{error}</p>}
        </article>
      </section>
    </main>
  );
}

export default Home;
