function badgeForRank(index) {
  if (index === 0) {
    return 'mc-rank mc-rank-gold';
  }
  if (index === 1) {
    return 'mc-rank mc-rank-silver';
  }
  if (index === 2) {
    return 'mc-rank mc-rank-bronze';
  }
  return 'mc-rank';
}

export default function LeaderboardTable({ leaderboard }) {
  const rows = leaderboard?.leaderboard || [];

  return (
    <section className="panel">
      <h3>Leaderboard Arena</h3>
      <div className="table-shell">
        <table className="data-table">
          <thead>
            <tr>
              <th>Rank</th>
              <th>Player</th>
              <th>Best Cost</th>
              <th>Optimal</th>
              <th>Total</th>
              <th>Last Seen</th>
            </tr>
          </thead>
          <tbody>
            {rows.length ? rows.map((entry, index) => (
              <tr key={`${entry.playerName}-${index}`}>
                <td><span className={badgeForRank(index)}>#{index + 1}</span></td>
                <td>{entry.playerName}</td>
                <td>{entry.bestSubmittedCost}</td>
                <td>{entry.optimalSubmissions}</td>
                <td>{entry.totalSubmissions}</td>
                <td>{entry.lastSubmittedAt}</td>
              </tr>
            )) : (
              <tr>
                <td colSpan="6" className="empty-state">No leaderboard entries yet.</td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </section>
  );
}
