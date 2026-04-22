import { analyzeBoard, createEmptyBoard, evaluatePreview, getBoardStatus, toAnswerString } from './validation';

describe('validation engine', () => {
  it('serializes a complete board and reports status', () => {
    const board = Array.from({ length: 16 }, (_, index) => index);
    const analysis = analyzeBoard(board, 16);

    expect(analysis.isComplete).toBe(true);
    expect(getBoardStatus(analysis)).toBe('conflict');
    expect(toAnswerString(board)).toBe(board.join(','));
  });

  it('detects column and diagonal conflicts', () => {
    const board = createEmptyBoard(16);
    board[0] = 0;
    board[1] = 0;
    board[2] = 2;

    const analysis = analyzeBoard(board, 16);

    expect(analysis.isValid).toBe(false);
    expect(analysis.conflictColumns.has(0)).toBe(true);
    expect(analysis.conflictingCells.size).toBeGreaterThan(0);
  });

  it('evaluates hover preview before placement', () => {
    const board = createEmptyBoard(16);
    board[0] = 2;

    const preview = evaluatePreview(board, 3, 8, 16);
    const conflictPreview = evaluatePreview(board, 1, 3, 16);

    expect(preview.isValidPlacement).toBe(true);
    expect(conflictPreview.isValidPlacement).toBe(false);
  });
});
