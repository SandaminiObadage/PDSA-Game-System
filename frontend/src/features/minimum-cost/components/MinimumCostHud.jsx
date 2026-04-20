function formatNumber(value) {
  return Number(value || 0).toLocaleString();
}

export default function MinimumCostHud({
  round,
  hungarian,
  alternative,
  status,
  streak,
  bestStreak,
  loading,
  onStartRound,
  onRefresh
}) {
  const speedup = hungarian && alternative
    ? (hungarian.executionTimeMs / Math.max(1, alternative.executionTimeMs)).toFixed(2)
    : null;

  return (
    <section className="mc-hud panel">
      <div className="mc-hud-title">
        <p className="eyebrow">Mission Control</p>
        <h2>Assignment Command Deck</h2>
        <p className="subtitle">Launch rounds, monitor algorithm performance, and keep your optimal streak alive.</p>
      </div>

      <div className="mc-hud-actions">
        <button onClick={onStartRound} disabled={loading}>Start New Round</button>
        <button className="secondary-button" onClick={onRefresh} disabled={loading}>Refresh Panels</button>
      </div>

      <div className="mc-hud-grid">
        <article className="mc-hud-card">
          <span>Round</span>
          <strong>{round ? `#${round.roundNo}` : 'Not started'}</strong>
          <p>{round ? `${round.n} employees x ${round.n} tasks` : 'Generate a round to begin.'}</p>
        </article>
        <article className="mc-hud-card">
          <span>Optimal Cost</span>
          <strong>{hungarian ? formatNumber(hungarian.totalCost) : 'Pending'}</strong>
          <p>Hungarian baseline for submission validation.</p>
        </article>
        <article className="mc-hud-card">
          <span>Speedup</span>
          <strong>{speedup ? `${speedup}x` : 'Pending'}</strong>
          <p>Hungarian time compared to alternative time.</p>
        </article>
        <article className="mc-hud-card">
          <span>Player Streak</span>
          <strong>{streak}</strong>
          <p>Best streak: {bestStreak}</p>
        </article>
      </div>

      <div className="status-card mc-status-card">
        <span>Live Status</span>
        <strong>{status}</strong>
      </div>
    </section>
  );
}
