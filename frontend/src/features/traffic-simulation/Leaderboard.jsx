/**
 * Leaderboard component for Traffic Simulation game
 */
function Leaderboard({ entries, loading, error }) {
  if (loading) {
    return <div className="leaderboard">Loading leaderboard...</div>;
  }

  if (error) {
    return <div className="leaderboard error">Error loading leaderboard: {error}</div>;
  }

  if (!entries || entries.length === 0) {
    return <div className="leaderboard empty">No players yet. Be the first!</div>;
  }

  return (
    <div className="leaderboard">
      <h3>🏆 Leaderboard</h3>
      <table className="leaderboard-table">
        <thead>
          <tr>
            <th>Rank</th>
            <th>Player Name</th>
            <th>Correct Answers</th>
            <th>Avg Algorithm Time (ms)</th>
          </tr>
        </thead>
        <tbody>
          {entries.map((entry, idx) => (
            <tr key={idx} className={idx === 0 ? 'rank-1' : idx === 1 ? 'rank-2' : idx === 2 ? 'rank-3' : ''}>
              <td className="rank">
                {idx === 0 ? '🥇' : idx === 1 ? '🥈' : idx === 2 ? '🥉' : `#${idx + 1}`}
              </td>
              <td className="player-name">{entry.playerName}</td>
              <td className="correct-answers">{entry.correctAnswers}</td>
              <td className="avg-time">{entry.averageExecutionTimeMs.toFixed(2)}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

export default Leaderboard;
