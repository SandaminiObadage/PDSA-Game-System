import { useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { fetchKnightLeaderboard } from '../../api/knightsTourApi';

function Leaderboard() {
  const navigate = useNavigate();
  const [rows, setRows] = useState([]);
  const [error, setError] = useState('');

  const lastResult = useMemo(() => {
    const raw = localStorage.getItem('knightLastResult');
    return raw ? JSON.parse(raw) : null;
  }, []);

  useEffect(() => {
    const load = async () => {
      try {
        const data = await fetchKnightLeaderboard();
        setRows(data || []);
      } catch (requestError) {
        const message = requestError?.response?.data?.message || requestError.message || 'Failed to load leaderboard.';
        setError(message);
      }
    };

    load();
  }, []);

  return (
    <main className="shell">
      <section className="hero">
        <div>
          <button className="back-button" onClick={() => navigate('/')} title="Back to dashboard">
            ← Dashboard
          </button>
          <p className="eyebrow">Knight&apos;s Tour</p>
          <h1>Leaderboard</h1>
          <p className="subtitle">Ranked by most moves, then wins, then player name.</p>
        </div>
      </section>

      <section className="grid-layout">
        <article className="panel">
          <h2>Top Players</h2>

          {error && <p className="error-banner">{error}</p>}

          <table className="kt-table">
            <thead>
              <tr>
                <th>Rank</th>
                <th>Player</th>
                <th>Most Moves</th>
                <th>Wins</th>
              </tr>
            </thead>
            <tbody>
              {rows.map((row, index) => {
                const isLatestWinner = lastResult?.status === 'WIN' && lastResult?.playerName === row.playerName;
                return (
                  <tr key={row.playerName} className={isLatestWinner ? 'kt-highlight' : ''}>
                    <td>{index + 1}</td>
                    <td>{row.playerName}</td>
                    <td>{row.mostMoves}</td>
                    <td>{row.wins}</td>
                  </tr>
                );
              })}
            </tbody>
          </table>

          <button type="button" onClick={() => navigate('/games/knights-tour')}>Back to Home</button>
        </article>
      </section>
    </main>
  );
}

export default Leaderboard;
