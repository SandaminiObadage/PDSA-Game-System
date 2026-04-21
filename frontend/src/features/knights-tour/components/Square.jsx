function Square({
  row,
  col,
  isLight,
  isStart,
  isVisited,
  isCurrent,
  moveIndex,
  hint,
  onClick,
  disabled
}) {
  const classes = [
    'kt-square',
    isLight ? 'kt-light' : 'kt-dark',
    isStart ? 'kt-start' : '',
    isVisited ? 'kt-visited' : '',
    isCurrent ? 'kt-current' : '',
    hint ? 'kt-hint' : '',
    hint?.best ? 'kt-best-hint' : ''
  ]
    .filter(Boolean)
    .join(' ');

  return (
    <button
      type="button"
      className={classes}
      onClick={() => onClick(row, col)}
      disabled={disabled}
      aria-label={`square-${row}-${col}`}
    >
      {isCurrent && <span className="kt-knight" aria-hidden>♞</span>}
      {typeof moveIndex === 'number' && (
        <span className="kt-badge">{moveIndex + 1}</span>
      )}
      {hint && (
        <span className="kt-hint-badge">
          #{hint.rank}
          {hint.best ? ' ★' : ''}
        </span>
      )}
    </button>
  );
}

export default Square;
