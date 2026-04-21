import { useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { validateKnightGame } from '../../api/knightsTourApi';
import ResultModal from '../../features/knights-tour/components/ResultModal';

function Result() {
  const navigate = useNavigate();
  const game = useMemo(() => {
    const raw = localStorage.getItem('knightGame');
    return raw ? JSON.parse(raw) : null;
  }, []);
  const pending = useMemo(() => {
    const raw = localStorage.getItem('knightPendingOutcome');
    return raw ? JSON.parse(raw) : null;
  }, []);

  const [playerName, setPlayerName] = useState('');
  const [loading, setLoading] = useState(false);
  const [modal, setModal] = useState({ status: '', message: '' });

  const handleValidate = async () => {
    if (!playerName.trim()) {
      setModal({ status: 'LOSE', message: 'Player name is required.' });
      return;
    }

    if (!game) {
      setModal({ status: 'LOSE', message: 'No active game found.' });
      return;
    }

    setLoading(true);
    try {
      const response = await validateKnightGame({
        knightId: game.knightId,
        moves: game.moves,
        playerName: playerName.trim(),
        outcomeOverride: pending?.status,
        outcomeMessage: pending?.message
      });

      const forcePending = pending?.status === 'LOSE' || pending?.status === 'DRAW';
      const status = forcePending ? pending.status : response.status;
      const message = forcePending ? pending.message : response.message;

      localStorage.setItem('knightLastResult', JSON.stringify({ playerName: playerName.trim(), status }));
      localStorage.removeItem('knightPendingOutcome');
      setModal({ status, message });
    } catch (requestError) {
      const message = requestError?.response?.data?.message || requestError.message || 'Validation failed.';
      setModal({ status: 'LOSE', message });
    } finally {
      setLoading(false);
    }
  };

  const handleNewGame = () => {
    localStorage.removeItem('knightGame');
    navigate('/games/knights-tour');
  };

  return (
    <main className="shell">
      <section className="hero">
        <div>
          <button className="back-button" onClick={() => navigate('/')} title="Back to dashboard">
            ← Dashboard
          </button>
          <p className="eyebrow">Knight&apos;s Tour</p>
          <h1>Result</h1>
          <p className="subtitle">Review your run and validate your submission.</p>
        </div>
        <div className="status-card">
          <span>Move Count</span>
          <strong>{pending?.moveCount ?? game?.moves?.length ?? 0}</strong>
        </div>
      </section>

      <section className="grid-layout">
        <article className="panel">
          <h2>Submit Result</h2>
          <form className="form-grid" onSubmit={(event) => { event.preventDefault(); handleValidate(); }}>
            <label className="full-width">
              Player Name
              <input
                value={playerName}
                onChange={(event) => setPlayerName(event.target.value)}
                placeholder="Enter player name"
              />
            </label>
            <button type="submit" onClick={handleValidate} disabled={loading}>
              {loading ? 'Submitting...' : 'Validate'}
            </button>
          </form>

          <div className="kt-action-row">
            <button type="button" onClick={handleNewGame}>New Game</button>
            <button type="button" onClick={() => navigate('/leaderboard')}>View Leaderboard</button>
          </div>
        </article>
      </section>

      <ResultModal
        status={modal.status}
        message={modal.message}
        onClose={() => setModal({ status: '', message: '' })}
      />
    </main>
  );
}

export default Result;
