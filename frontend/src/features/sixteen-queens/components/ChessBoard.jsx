import { useMemo } from 'react';
import { motion } from 'framer-motion';
import { evaluatePreview } from '../validation';

function cellKey(row, col) {
  return `${row}-${col}`;
}

export function ChessBoard({
  size,
  board,
  analysis,
  hoverCell,
  onHoverCell,
  onCellClick,
  invalidMoveCellKey,
  lastMoveCellKey
}) {
  const hoverPreview = useMemo(() => {
    if (!hoverCell) {
      return null;
    }

    return evaluatePreview(board, hoverCell.row, hoverCell.col, size);
  }, [board, hoverCell, size]);

  return (
    <div className="board-shell">
      <div className="board-scroll-wrap">
        <div className="queens-board-grid" role="grid" aria-label="Sixteen queens chessboard">
          {Array.from({ length: size }).map((_, row) => (
            <div key={`row-${row}`} className="queens-board-row" role="row">
              {Array.from({ length: size }).map((__, col) => {
                const key = cellKey(row, col);
                const hasQueen = board[row] === col;
                const isDark = (row + col) % 2 === 1;
                const isConflictQueen = analysis.conflictingCells.has(key);
                const isConflictLane = analysis.conflictRows.has(row) || analysis.conflictColumns.has(col);
                const isDiagonalConflict =
                  analysis.conflictDiagonals.has(row - col) ||
                  analysis.conflictAntiDiagonals.has(row + col);

                const isHoverCell = hoverCell && hoverCell.row === row && hoverCell.col === col;
                const hoverConflictLane =
                  hoverPreview &&
                  (hoverPreview.attackRows.has(row) ||
                    hoverPreview.attackColumns.has(col) ||
                    hoverPreview.attackDiagonals.has(row - col) ||
                    hoverPreview.attackAntiDiagonals.has(row + col));

                return (
                  <button
                    key={key}
                    type="button"
                    className={[
                      'board-cell',
                      isDark ? 'dark' : 'light',
                      hasQueen ? 'has-queen' : '',
                      isConflictLane ? 'conflict-lane' : '',
                      isDiagonalConflict ? 'diagonal-danger' : '',
                      isConflictQueen ? 'conflict-queen' : '',
                      key === invalidMoveCellKey ? 'invalid-pulse' : '',
                      key === lastMoveCellKey && !isConflictQueen ? 'valid-pop' : '',
                      isHoverCell ? 'hover-cell' : '',
                      hoverConflictLane ? 'hover-danger' : '',
                      isHoverCell && hoverPreview?.isValidPlacement ? 'hover-valid' : ''
                    ]
                      .filter(Boolean)
                      .join(' ')}
                    aria-label={`Row ${row + 1} column ${col + 1}${hasQueen ? ', queen placed' : ''}`}
                    title={`Row ${row + 1}, Column ${col + 1}`}
                    onClick={() => onCellClick(row, col)}
                    onMouseEnter={() => onHoverCell({ row, col })}
                    onFocus={() => onHoverCell({ row, col })}
                    onMouseLeave={() => onHoverCell(null)}
                    onBlur={() => onHoverCell(null)}
                  >
                    {hasQueen && (
                      <motion.span
                        layout
                        initial={{ opacity: 0, scale: 0.55 }}
                        animate={{ opacity: 1, scale: 1 }}
                        transition={{ duration: 0.18 }}
                        className="queen-token"
                      >
                        Q
                      </motion.span>
                    )}
                  </button>
                );
              })}
            </div>
          ))}
        </div>
      </div>

      <div className="board-legend">
        <span className="legend-item"><i className="legend-dot valid" />Valid lane</span>
        <span className="legend-item"><i className="legend-dot invalid" />Conflict lane</span>
        <span className="legend-item"><i className="legend-dot duplicate" />Duplicate solution</span>
      </div>
    </div>
  );
}
