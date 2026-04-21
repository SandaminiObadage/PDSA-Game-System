const KNIGHT_DELTAS = [
  [2, 1],
  [1, 2],
  [-1, 2],
  [-2, 1],
  [-2, -1],
  [-1, -2],
  [1, -2],
  [2, -1]
];

const NODE_LIMITS = {
  8: 3000000,
  16: 6000000
};

const neighborCache = new Map();

function keyOf(row, col) {
  return `${row},${col}`;
}

function parseKey(key) {
  const parts = key.split(',');
  if (parts.length !== 2) {
    throw new Error(`Invalid move format: ${key}. Expected row,col.`);
  }

  const row = Number(parts[0]);
  const col = Number(parts[1]);
  if (!Number.isInteger(row) || !Number.isInteger(col)) {
    throw new Error(`Invalid move format: ${key}. Expected row,col.`);
  }

  return { row, col, key: keyOf(row, col) };
}

function getCenterDistance(row, col, boardSize) {
  const center = (boardSize - 1) / 2;
  const dr = row - center;
  const dc = col - center;
  return dr * dr + dc * dc;
}

function cacheId(boardSize) {
  return `neighbors:${boardSize}`;
}

export function getNeighborCache(boardSize) {
  const id = cacheId(boardSize);
  if (neighborCache.has(id)) {
    return neighborCache.get(id);
  }

  const map = new Map();
  for (let row = 0; row < boardSize; row += 1) {
    for (let col = 0; col < boardSize; col += 1) {
      const neighbors = [];
      KNIGHT_DELTAS.forEach(([dr, dc], moveOrder) => {
        const nr = row + dr;
        const nc = col + dc;
        if (nr >= 0 && nc >= 0 && nr < boardSize && nc < boardSize) {
          neighbors.push({ row: nr, col: nc, key: keyOf(nr, nc), moveOrder });
        }
      });
      map.set(keyOf(row, col), neighbors);
    }
  }

  neighborCache.set(id, map);
  return map;
}

export function validateMoveList(moves, boardSize) {
  if (!Array.isArray(moves) || moves.length === 0) {
    throw new Error('Moves must be a non-empty array.');
  }

  const visited = new Set();
  const parsed = [];

  moves.forEach((move, index) => {
    const current = parseKey(move);

    if (current.row < 0 || current.col < 0 || current.row >= boardSize || current.col >= boardSize) {
      throw new Error(`Move out of bounds: ${move}`);
    }

    if (visited.has(current.key)) {
      throw new Error(`Duplicate position detected: ${current.key}`);
    }

    if (index > 0) {
      const previous = parsed[index - 1];
      const rowDelta = Math.abs(previous.row - current.row);
      const colDelta = Math.abs(previous.col - current.col);
      const isKnightJump = (rowDelta === 2 && colDelta === 1) || (rowDelta === 1 && colDelta === 2);
      if (!isKnightJump) {
        throw new Error(`Invalid knight jump from ${previous.key} to ${current.key}`);
      }
    }

    parsed.push(current);
    visited.add(current.key);
  });

  return parsed;
}

function sortCandidates(candidates) {
  return [...candidates].sort((a, b) => {
    if (a.onwardCount !== b.onwardCount) {
      return a.onwardCount - b.onwardCount;
    }
    if (a.centerDistance !== b.centerDistance) {
      return b.centerDistance - a.centerDistance;
    }
    return a.moveOrder - b.moveOrder;
  });
}

export function getLegalCandidates(currentKey, visitedSet, boardSize) {
  const neighbors = getNeighborCache(boardSize).get(currentKey) || [];
  return neighbors.filter((neighbor) => !visitedSet.has(neighbor.key));
}

function isFutureAccessible(candidate, moveCount, boardSize) {
  const total = boardSize * boardSize;
  const nextMoveCount = moveCount + 1;
  if (nextMoveCount >= total) {
    return true;
  }
  return candidate.onwardCount > 0;
}

export function getWarnsdorffCandidates(currentKey, visitedSet, boardSize) {
  const raw = getLegalCandidates(currentKey, visitedSet, boardSize)
    .map((neighbor) => {
      const onwardCount = (getNeighborCache(boardSize).get(neighbor.key) || []).filter(
        (next) => !visitedSet.has(next.key) && next.key !== currentKey
      ).length;

      return {
        ...neighbor,
        onwardCount,
        centerDistance: getCenterDistance(neighbor.row, neighbor.col, boardSize)
      };
    });

  const sorted = sortCandidates(raw);

  return sorted.map((candidate, index) => ({
    key: candidate.key,
    row: candidate.row,
    col: candidate.col,
    onwardCount: candidate.onwardCount,
    rank: index + 1,
    best: index === 0,
    moveOrder: candidate.moveOrder,
    centerDistance: candidate.centerDistance
  }));
}

export function findNextMoveWarnsdorff(moves, boardSize) {
  const parsed = validateMoveList(moves, boardSize);
  const visited = new Set(parsed.map((move) => move.key));
  const current = parsed[parsed.length - 1];

  if (visited.size >= boardSize * boardSize) {
    return null;
  }

  const ranked = getWarnsdorffCandidates(current.key, visited, boardSize);
  return ranked[0]?.key || null;
}

function backtrackingSearch(current, visited, boardSize, nodeLimit, state) {
  if (state.nodes > nodeLimit) {
    return false;
  }

  if (visited.size === boardSize * boardSize) {
    return true;
  }

  const candidates = getWarnsdorffCandidates(current.key, visited, boardSize)
    .map((candidate) => ({
      row: candidate.row,
      col: candidate.col,
      key: candidate.key,
      moveOrder: candidate.moveOrder
    }));

  for (const candidate of candidates) {
    if (state.nodes > nodeLimit) {
      return false;
    }

    state.nodes += 1;
    visited.add(candidate.key);

    const nextNode = { row: candidate.row, col: candidate.col, key: candidate.key };
    const solved = backtrackingSearch(nextNode, visited, boardSize, nodeLimit, state);
    if (solved) {
      return true;
    }

    visited.delete(candidate.key);
  }

  return false;
}

export function findNextMoveBacktracking(moves, boardSize) {
  const parsed = validateMoveList(moves, boardSize);
  const visited = new Set(parsed.map((move) => move.key));
  const current = parsed[parsed.length - 1];

  if (visited.size >= boardSize * boardSize) {
    return null;
  }

  const nodeLimit = NODE_LIMITS[boardSize] || 3000000;
  const firstMoves = getWarnsdorffCandidates(current.key, visited, boardSize)
    .map((candidate) => ({
      row: candidate.row,
      col: candidate.col,
      key: candidate.key,
      moveOrder: candidate.moveOrder
    }));

  for (const firstMove of firstMoves) {
    const state = { nodes: 0 };
    visited.add(firstMove.key);
    const solved = backtrackingSearch(
      { row: firstMove.row, col: firstMove.col, key: firstMove.key },
      visited,
      boardSize,
      nodeLimit,
      state
    );
    visited.delete(firstMove.key);

    if (solved) {
      return firstMove.key;
    }
  }

  return null;
}

export function findNextMoveByAlgorithm(algorithmType, moves, boardSize) {
  if (algorithmType === 'BACKTRACKING') {
    return findNextMoveBacktracking(moves, boardSize);
  }
  return findNextMoveWarnsdorff(moves, boardSize);
}
