import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { getSnakeLadderLeaderboard } from '../../api/snakeLadderApi';

function SnakeLadderLeaderboardPage() {
  const navigate = useNavigate();
  const [leaderboard, setLeaderboard] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    fetchLeaderboard();
  }, []);

  const fetchLeaderboard = async () => {
    try {
      setLoading(true);
      setError('');
      const data = await getSnakeLadderLeaderboard(50);
      setLeaderboard(data.leaderboard || []);
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Failed to load leaderboard');
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
            onClick={() => navigate('/games/snake-ladder')}
            title="Back to game"
          >
            ← Back
          </button>
          <p className="eyebrow">Snake and Ladder Challenge</p>
          <h1>Leaderboard</h1>
          <p className="subtitle">
            Top players by correct answers in the Snake and Ladder game
          </p>
        </div>
      </section>

      {loading && <p style={{ textAlign: 'center', padding: '20px' }}>Loading leaderboard...</p>}

      {error && <p className="error-banner">{error}</p>}

      {!loading && leaderboard.length === 0 && !error && (
        <section className="panel">
          <p style={{ textAlign: 'center' }}>No leaderboard data available yet.</p>
        </section>
      )}

      {!loading && leaderboard.length > 0 && (
        <section className="panel">
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
        </section>
      )}

      <style>{`
        .leaderboard-table {
          width: 100%;
          border-collapse: collapse;
          margin-top: 20px;
        }

        .leaderboard-table thead {
          background-color: #f0f0f0;
          font-weight: bold;
        }

        .leaderboard-table th,
        .leaderboard-table td {
          padding: 12px;
          text-align: left;
          border-bottom: 1px solid #ddd;
        }

        .leaderboard-table tbody tr:hover {
          background-color: #f9f9f9;
        }

        .leaderboard-table tbody tr:nth-child(even) {
          background-color: #f5f5f5;
        }

        .leaderboard-table th {
          padding: 15px 12px;
        }
      `}</style>
    </main>
  );
}

export default SnakeLadderLeaderboardPage;
