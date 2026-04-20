function winnerLabel(round) {
  if (!round) {
    return '-';
  }
  if (round.hungarianCost === round.alternativeCost) {
    return 'Tie';
  }
  return round.hungarianCost < round.alternativeCost ? 'Hungarian' : 'Alternative';
}

export default function HistoryTimeline({ history, report }) {
  const rounds = history?.rounds || [];
  const submissions = history?.recentSubmissions || [];

  return (
    <section className="grid-layout">
      <article className="panel">
        <h3>Round Timeline</h3>
        <div className="mc-timeline">
          {rounds.length ? rounds.map((round) => (
            <div key={round.roundId} className="mc-timeline-item">
              <div>
                <strong>Round #{round.roundNo}</strong>
                <p>N = {round.n}</p>
              </div>
              <div>
                <p>Hungarian: {round.hungarianCost} ({round.hungarianTimeMs} ms)</p>
                <p>Alternative: {round.alternativeCost} ({round.alternativeTimeMs} ms)</p>
                <p className="mc-win-tag">Winner: {winnerLabel(round)}</p>
              </div>
            </div>
          )) : <p className="empty-state">No timeline data yet.</p>}
        </div>
      </article>

      <article className="panel">
        <h3>Performance Chronicle</h3>
        <div className="mc-report-grid">
          <div><span>Total rounds</span><strong>{report?.totalRounds ?? 0}</strong></div>
          <div><span>Average Hungarian</span><strong>{Number(report?.averageHungarianTimeMs || 0).toFixed(1)} ms</strong></div>
          <div><span>Average Alternative</span><strong>{Number(report?.averageAlternativeTimeMs || 0).toFixed(1)} ms</strong></div>
        </div>

        <h4 className="mc-subtitle">Recent Submissions</h4>
        <div className="history-list">
          {submissions.length ? submissions.map((item) => (
            <div key={item.submissionId} className="history-item">
              <div>
                <strong>{item.playerName}</strong>
                <p>Round #{item.roundId}</p>
              </div>
              <div>
                <span>{item.submittedCost}</span>
                <p>{item.optimal ? 'Optimal' : 'Not optimal'}</p>
              </div>
            </div>
          )) : <p className="empty-state">No submissions recorded yet.</p>}
        </div>
      </article>
    </section>
  );
}
