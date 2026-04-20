function formatNumber(value) {
  return Number(value || 0).toLocaleString();
}

export default function AlgorithmDuelCard({ title, result, opponent }) {
  if (!result) {
    return (
      <article className="panel mc-duel-card">
        <h3>{title}</h3>
        <p className="empty-state">No run data yet.</p>
      </article>
    );
  }

  const costDelta = opponent ? result.totalCost - opponent.totalCost : 0;
  const timeDelta = opponent ? result.executionTimeMs - opponent.executionTimeMs : 0;

  return (
    <article className="panel mc-duel-card">
      <p className="eyebrow">{result.algorithmVariant}</p>
      <h3>{title}</h3>
      <p className="mc-duel-main">Cost {formatNumber(result.totalCost)}</p>
      <p className="mc-duel-sub">Time {formatNumber(result.executionTimeMs)} ms</p>
      {opponent ? (
        <div className="mc-delta-row">
          <span className={costDelta <= 0 ? 'mc-good' : 'mc-warn'}>
            Cost delta {costDelta >= 0 ? '+' : ''}{formatNumber(costDelta)}
          </span>
          <span className={timeDelta <= 0 ? 'mc-good' : 'mc-warn'}>
            Time delta {timeDelta >= 0 ? '+' : ''}{formatNumber(timeDelta)} ms
          </span>
        </div>
      ) : null}
    </article>
  );
}
