export default function SubmissionPanel({ form, onFormChange, onSubmit, loading, canSubmit, roundId, lastSubmission }) {
  return (
    <article className="panel">
      <h3>Decision Deck</h3>
      <p className="grid-hint">Round target: {roundId ? `#${roundId}` : 'No active round yet'}.</p>

      <form className="form-grid" onSubmit={onSubmit}>
        <label>
          Player name
          <input
            value={form.playerName}
            onChange={(event) => onFormChange('playerName', event.target.value)}
            placeholder="Your player handle"
          />
        </label>

        <label>
          Submitted cost
          <input
            type="number"
            value={form.submittedCost}
            onChange={(event) => onFormChange('submittedCost', event.target.value)}
            placeholder="Enter total cost"
          />
        </label>

        <label className="full-width">
          Assignment mapping (optional)
          <textarea
            className="text-area"
            value={form.assignmentInput}
            rows="5"
            onChange={(event) => onFormChange('assignmentInput', event.target.value)}
            placeholder="[3,1,0,...] or 3,1,0,..."
          />
        </label>

        <div className="full-width actions-row compact-actions">
          <button type="submit" disabled={loading || !canSubmit}>Submit Strategy</button>
        </div>
      </form>

      {lastSubmission ? (
        <div className={lastSubmission.correct ? 'mc-submission mc-good-bg' : 'mc-submission mc-warn-bg'}>
          <strong>{lastSubmission.message}</strong>
          <p>
            Submitted {lastSubmission.submittedCost}
            {lastSubmission.optimalCost != null ? ` / Optimal ${lastSubmission.optimalCost}` : ''}
          </p>
        </div>
      ) : null}
    </article>
  );
}
