export const BOARD_SIZE = 16;

export function createEmptyBoard(size = BOARD_SIZE) {
  return Array(size).fill(-1);
}

function keyFor(row, col) {
  return `${row}-${col}`;
}

export function toAnswerString(board) {
  if (!board.length || board.some((col) => col < 0)) {
    return '';
  }
  return board.join(',');
}

export function parseAnswerString(answer, size = BOARD_SIZE) {
  if (!answer || typeof answer !== 'string') {
    return null;
  }

  const parsed = answer.split(',').map((value) => Number(value));
  if (parsed.length !== size || parsed.some((value) => Number.isNaN(value) || value < 0 || value >= size)) {
    return null;
  }

  return parsed;
}

export function analyzeBoard(board, size = BOARD_SIZE) {
  const columnCounts = new Map();
  const diagonalCounts = new Map();
  const antiDiagonalCounts = new Map();

  board.forEach((col, row) => {
    if (col < 0) {
      return;
    }

    const diagonal = row - col;
    const antiDiagonal = row + col;

    columnCounts.set(col, (columnCounts.get(col) || 0) + 1);
    diagonalCounts.set(diagonal, (diagonalCounts.get(diagonal) || 0) + 1);
    antiDiagonalCounts.set(antiDiagonal, (antiDiagonalCounts.get(antiDiagonal) || 0) + 1);
  });

  const conflictingCells = new Set();
  const conflictRows = new Set();
  const conflictColumns = new Set();
  const conflictDiagonals = new Set();
  const conflictAntiDiagonals = new Set();

  board.forEach((col, row) => {
    if (col < 0) {
      return;
    }

    const diagonal = row - col;
    const antiDiagonal = row + col;
    const inConflict =
      (columnCounts.get(col) || 0) > 1 ||
      (diagonalCounts.get(diagonal) || 0) > 1 ||
      (antiDiagonalCounts.get(antiDiagonal) || 0) > 1;

    if (!inConflict) {
      return;
    }

    conflictingCells.add(keyFor(row, col));
    conflictRows.add(row);
    conflictColumns.add(col);

    if ((diagonalCounts.get(diagonal) || 0) > 1) {
      conflictDiagonals.add(diagonal);
    }

    if ((antiDiagonalCounts.get(antiDiagonal) || 0) > 1) {
      conflictAntiDiagonals.add(antiDiagonal);
    }
  });

  const queensPlaced = board.filter((value) => value >= 0).length;

  return {
    queensPlaced,
    isComplete: queensPlaced === size,
    isValid: conflictingCells.size === 0,
    conflictingCells,
    conflictRows,
    conflictColumns,
    conflictDiagonals,
    conflictAntiDiagonals
  };
}

export function evaluatePreview(board, row, col, size = BOARD_SIZE) {
  const simulated = [...board];
  simulated[row] = col;
  const analysis = analyzeBoard(simulated, size);
  const simulatedKey = keyFor(row, col);

  return {
    isValidPlacement: !analysis.conflictingCells.has(simulatedKey),
    attackRows: analysis.conflictRows,
    attackColumns: analysis.conflictColumns,
    attackDiagonals: analysis.conflictDiagonals,
    attackAntiDiagonals: analysis.conflictAntiDiagonals
  };
}

export function getBoardStatus(analysis) {
  if (!analysis.isComplete) {
    return 'incomplete';
  }

  if (!analysis.isValid) {
    return 'conflict';
  }

  return 'valid';
}
