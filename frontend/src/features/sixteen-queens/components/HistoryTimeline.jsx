import { useMemo } from 'react';

export function HistoryTimeline({ historyResult }) {
  const timelineEntries = useMemo(() => {
    if (!historyResult?.recentAnswers) {
      return [];
    }

    return historyResult.recentAnswers.map((entry) => ({
      id: entry.answerId,
      title: `${entry.playerName} · Round ${entry.roundNo}`,
      subtitle: entry.correct ? 'Correct answer' : 'Incorrect answer',
      submittedAt: entry.submittedAt,
      answer: entry.answerJson,
      tone: entry.correct ? 'good' : 'bad'
    }));
  }, [historyResult]);

  if (!historyResult) {
    return <p className="panel-empty">Load history to inspect solver and player activity.</p>;
  }

  return (
    <div className="timeline">
      {timelineEntries.map((entry) => (
        <details key={entry.id} className={`timeline-card ${entry.tone}`}>
          <summary>
            <span>
              <strong>{entry.title}</strong>
              <small>{entry.subtitle}</small>
            </span>
            <time>{entry.submittedAt ? new Date(entry.submittedAt).toLocaleString() : 'N/A'}</time>
          </summary>
          <pre>{entry.answer}</pre>
        </details>
      ))}
    </div>
  );
}
