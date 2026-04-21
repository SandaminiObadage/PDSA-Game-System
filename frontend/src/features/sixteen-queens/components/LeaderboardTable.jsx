import { useMemo, useState } from 'react';

function rankBadge(rank) {
  if (rank === 1) return '🥇';
  if (rank === 2) return '🥈';
  if (rank === 3) return '🥉';
  return `#${rank}`;
}

const SORTERS = {
  recognized: (a, b) => b.recognizedSolutionCount - a.recognizedSolutionCount,
  answers: (a, b) => b.totalAnswers - a.totalAnswers,
  recent: (a, b) => new Date(b.lastSubmittedAt || 0) - new Date(a.lastSubmittedAt || 0)
};

export function LeaderboardTable({ leaderboardResult }) {
  const [filter, setFilter] = useState('');
  const [sortBy, setSortBy] = useState('recognized');

  const rows = useMemo(() => {
    const data = leaderboardResult?.leaderboard || [];
    return data
      .filter((entry) => entry.playerName.toLowerCase().includes(filter.trim().toLowerCase()))
      .sort(SORTERS[sortBy]);
  }, [leaderboardResult, filter, sortBy]);

  if (!leaderboardResult) {
    return <p className="panel-empty">Load leaderboard to see top players.</p>;
  }

  return (
    <div className="data-table-wrap">
      <p className="leaderboard-meta">Showing {rows.length} player records.</p>
      <div className="table-controls">
        <input
          value={filter}
          onChange={(event) => setFilter(event.target.value)}
          placeholder="Filter player"
          aria-label="Filter players"
        />
        <select value={sortBy} onChange={(event) => setSortBy(event.target.value)} aria-label="Sort leaderboard">
          <option value="recognized">Sort by recognized</option>
          <option value="answers">Sort by total answers</option>
          <option value="recent">Sort by last submission</option>
        </select>
      </div>

      {rows.length === 0 && <p className="panel-empty">No players found for the current leaderboard data.</p>}

      <table className="leaderboard-table">
        <thead>
          <tr>
            <th>Rank</th>
            <th>Player</th>
            <th>Solutions</th>
            <th>Attempts</th>
            <th>Accuracy</th>
            <th>Last Seen</th>
          </tr>
        </thead>
        <tbody>
          {rows.map((entry, index) => {
            const rank = index + 1;
            return (
              <tr key={entry.playerId} className={rank <= 3 ? `podium rank-${rank}` : ''}>
                <td>{rankBadge(rank)}</td>
                <td>{entry.playerName}</td>
                <td>{entry.recognizedSolutionCount}</td>
                <td>{entry.totalAnswers}</td>
                <td>{entry.accuracy?.toFixed(1)}%</td>
                <td>{entry.lastSubmittedAt ? new Date(entry.lastSubmittedAt).toLocaleString() : 'N/A'}</td>
              </tr>
            );
          })}
        </tbody>
      </table>
    </div>
  );
}
