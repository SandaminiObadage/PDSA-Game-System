function Controls({
  autoSpeed,
  onAutoSpeedChange,
  onNextMove,
  onToggleAuto,
  onResetPath,
  onSubmit,
  onQuit,
  autoRunning,
  disabled
}) {
  return (
    <div className="kt-controls">
      <label>
        Auto Solve Speed ({autoSpeed}ms)
        <input
          type="range"
          min="50"
          max="1000"
          step="50"
          value={autoSpeed}
          onChange={(event) => onAutoSpeedChange(Number(event.target.value))}
          disabled={disabled}
        />
      </label>
      <div className="kt-action-row">
        <button type="button" onClick={onNextMove} disabled={disabled}>Next Move</button>
        <button type="button" onClick={onToggleAuto} disabled={disabled}>
          {autoRunning ? 'Stop Auto Solve' : 'Auto Solve'}
        </button>
        <button type="button" onClick={onResetPath} disabled={disabled}>Reset Path</button>
        <button type="button" onClick={onSubmit} disabled={disabled}>Submit Solution</button>
        <button type="button" className="kt-quit" onClick={onQuit} disabled={disabled}>Quit Game</button>
      </div>
    </div>
  );
}

export default Controls;
