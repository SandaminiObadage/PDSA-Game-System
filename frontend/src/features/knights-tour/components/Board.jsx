import Square from './Square';

function Board({ boardSize, moves, candidates, onSquareClick, disabled }) {
  const moveIndexByKey = new Map(moves.map((move, index) => [move, index]));
  const visited = new Set(moves);
  const start = moves[0];
  const current = moves[moves.length - 1];
  const hintMap = new Map(candidates.map((candidate) => [candidate.key, candidate]));

  return (
    <div
      className="kt-board"
      style={{ gridTemplateColumns: `repeat(${boardSize}, minmax(0, 1fr))` }}
    >
      {Array.from({ length: boardSize * boardSize }, (_, index) => {
        const row = Math.floor(index / boardSize);
        const col = index % boardSize;
        const key = `${row},${col}`;

        return (
          <Square
            key={key}
            row={row}
            col={col}
            isLight={(row + col) % 2 === 0}
            isStart={start === key}
            isVisited={visited.has(key)}
            isCurrent={current === key}
            moveIndex={moveIndexByKey.has(key) ? moveIndexByKey.get(key) : null}
            hint={hintMap.get(key) || null}
            onClick={onSquareClick}
            disabled={disabled || visited.has(key)}
          />
        );
      })}
    </div>
  );
}

export default Board;
