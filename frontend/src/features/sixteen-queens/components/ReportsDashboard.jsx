function clampPercent(value, max) {
  if (!max || max <= 0) {
    return 0;
  }

  return Math.max(4, Math.min(100, (value / max) * 100));
}

function toEpoch(value, fallback) {
  const parsed = new Date(value || '').getTime();
  return Number.isNaN(parsed) ? fallback : parsed;
}

function buildTrendSeries(historyResult) {
  const answers = [...(historyResult?.recentAnswers || [])]
    .sort((a, b) => toEpoch(a.submittedAt, 0) - toEpoch(b.submittedAt, 0));

  if (answers.length === 0) {
    const rounds = [...(historyResult?.rounds || [])].reverse();
    let cumulative = 0;
    return rounds.map((entry, index) => {
      cumulative += entry.playerAnswerCount || 0;
      return {
        id: `round-${entry.gameRoundId || index}`,
        label: `Round ${entry.roundNo || index + 1}`,
        attempts: cumulative,
        correct: cumulative,
        timestamp: entry.createdAt
      };
    });
  }

  let runningCorrect = 0;
  return answers.map((entry, index) => {
    if (entry.correct) {
      runningCorrect += 1;
    }

    return {
      id: `answer-${entry.answerId || index}`,
      label: `${entry.playerName || 'Player'} (R${entry.roundNo || '-'})`,
      attempts: index + 1,
      correct: runningCorrect,
      timestamp: entry.submittedAt
    };
  });
}

function buildPolyline(series, accessor, maxY) {
  if (!series.length) {
    return '';
  }

  return series
    .map((entry, index) => {
      const x = (index / Math.max(1, series.length - 1)) * 100;
      const y = 100 - ((accessor(entry) || 0) / Math.max(1, maxY)) * 100;
      return `${x},${y}`;
    })
    .join(' ');
}

export function ReportsDashboard({ reportResult, historyResult, leaderboardResult }) {
  if (!reportResult) {
    return <p className="panel-empty">Load report to view performance analytics.</p>;
  }

  const bars = [
    { label: 'Sequential Avg', value: reportResult.averageSequentialTimeMs || 0 },
    { label: 'Threaded Avg', value: reportResult.averageParallelTimeMs || 0 }
  ];
  const maxBar = Math.max(...bars.map((entry) => entry.value), 1);

  const lineSeries = buildTrendSeries(historyResult);
  const maxTrendValue = Math.max(
    1,
    ...lineSeries.map((entry) => Math.max(entry.attempts || 0, entry.correct || 0))
  );
  const attemptsPolyline = buildPolyline(lineSeries, (entry) => entry.attempts, maxTrendValue);
  const correctPolyline = buildPolyline(lineSeries, (entry) => entry.correct, maxTrendValue);
  const latestPoint = lineSeries[lineSeries.length - 1];
  const accuracy = latestPoint?.attempts ? ((latestPoint.correct / latestPoint.attempts) * 100) : 0;
  const firstTimeLabel = lineSeries[0]?.timestamp
    ? new Date(lineSeries[0].timestamp).toLocaleString()
    : 'N/A';
  const lastTimeLabel = latestPoint?.timestamp
    ? new Date(latestPoint.timestamp).toLocaleString()
    : 'N/A';

  const fastestSolver = reportResult.averageParallelTimeMs <= reportResult.averageSequentialTimeMs
    ? 'Threaded Solver'
    : 'Sequential Solver';

  return (
    <div className="reports-grid">
      <div className="summary-cards">
        <article>
          <span>Total Solutions</span>
          <strong>{reportResult.totalKnownSolutionsPersisted}</strong>
        </article>
        <article>
          <span>Fastest Solver</span>
          <strong>{fastestSolver}</strong>
        </article>
        <article>
          <span>Active Players</span>
          <strong>{(leaderboardResult?.leaderboard || []).length}</strong>
        </article>
      </div>

      <section className="chart-card">
        <h3>Sequential vs Threaded Time</h3>
        <div className="bar-chart" role="img" aria-label="Bar chart for sequential and threaded timing">
          {bars.map((bar) => (
            <div key={bar.label} className="bar-item">
              <span>{bar.label}</span>
              <div className="bar-track">
                <div className="bar-fill" style={{ width: `${clampPercent(bar.value, maxBar)}%` }} />
              </div>
              <strong>{bar.value.toFixed(1)} ms</strong>
            </div>
          ))}
        </div>
      </section>

      <section className="chart-card">
        <h3>Solutions Found Over Time</h3>
        <div className="trend-metrics">
          <span>Total Attempts: <strong>{latestPoint?.attempts || 0}</strong></span>
          <span>Correct Progress: <strong>{latestPoint?.correct || 0}</strong></span>
          <span>Accuracy Trend: <strong>{accuracy.toFixed(1)}%</strong></span>
        </div>

        <svg
          viewBox="0 0 100 100"
          className="line-chart"
          role="img"
          aria-label="Detailed line chart of cumulative attempts and correct submissions over time"
        >
          <line x1="0" y1="100" x2="100" y2="100" className="chart-axis" />
          <line x1="0" y1="0" x2="0" y2="100" className="chart-axis" />
          <polyline points={attemptsPolyline} className="trend-line attempts" />
          <polyline points={correctPolyline} className="trend-line correct" />

          {lineSeries.map((entry, index) => {
            const x = (index / Math.max(1, lineSeries.length - 1)) * 100;
            const y = 100 - ((entry.correct || 0) / Math.max(1, maxTrendValue)) * 100;
            return (
              <circle key={entry.id} cx={x} cy={y} r="1.2" className="trend-point">
                <title>
                  {entry.label} | Attempts: {entry.attempts} | Correct: {entry.correct}
                </title>
              </circle>
            );
          })}
        </svg>

        <div className="chart-legend">
          <span><i className="legend-dot attempts" />Attempts (Cumulative)</span>
          <span><i className="legend-dot correct" />Correct (Cumulative)</span>
        </div>

        <div className="chart-range">
          <small>From: {firstTimeLabel}</small>
          <small>To: {lastTimeLabel}</small>
        </div>

        <div className="trend-details">
          <h4>Recent Activity Points</h4>
          <ul>
            {lineSeries.slice(-6).map((entry) => (
              <li key={`detail-${entry.id}`}>
                <span>{entry.label}</span>
                <span>{entry.timestamp ? new Date(entry.timestamp).toLocaleString() : 'N/A'}</span>
                <span>A:{entry.attempts} | C:{entry.correct}</span>
              </li>
            ))}
          </ul>
        </div>
      </section>
    </div>
  );
}
