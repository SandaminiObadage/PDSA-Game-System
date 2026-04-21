import { create } from 'zustand';
import { analyzeBoard, BOARD_SIZE, createEmptyBoard } from './validation';

const defaultSolveConfig = {
  threadCount: 8,
  solutionSampleLimit: 8,
  persistSolutionLimit: 200
};

const defaultSubmitForm = {
  playerName: '',
  gameRoundId: ''
};

function nextToastId() {
  return `${Date.now()}-${Math.random().toString(16).slice(2)}`;
}

export const useQueensGameStore = create((set, get) => ({
  size: BOARD_SIZE,
  board: createEmptyBoard(BOARD_SIZE),
  analysis: analyzeBoard(createEmptyBoard(BOARD_SIZE), BOARD_SIZE),
  hoverCell: null,
  lastMoveCellKey: null,
  invalidMoveCellKey: null,
  gameState: 'playing',
  viewerRole: 'PLAYER',
  solveConfig: defaultSolveConfig,
  submitForm: defaultSubmitForm,
  solveResult: null,
  sampleSolutions: [],
  selectedSampleIndex: 0,
  submitResult: null,
  historyResult: null,
  leaderboardResult: null,
  reportResult: null,
  roundClosed: false,
  loading: false,
  statusMessage: 'Set your board and start solving.',
  errorMessage: '',
  toasts: [],
  celebration: null,

  setLoading: (value) => set({ loading: value }),
  setStatusMessage: (statusMessage) => set({ statusMessage }),
  setErrorMessage: (errorMessage) => set({ errorMessage }),
  setViewerRole: (viewerRole) => set({ viewerRole }),
  setRoundClosed: (roundClosed) => set({ roundClosed }),

  updateSolveConfig: (patch) =>
    set((state) => ({
      solveConfig: { ...state.solveConfig, ...patch }
    })),

  updateSubmitForm: (patch) =>
    set((state) => ({
      submitForm: { ...state.submitForm, ...patch }
    })),

  setApiData: (key, value) => set({ [key]: value }),

  pushToast: (payload) => {
    const toast = {
      id: nextToastId(),
      tone: payload.tone || 'info',
      title: payload.title || 'Notice',
      message: payload.message || '',
      durationMs: payload.durationMs || 2200
    };

    set((state) => ({
      toasts: [...state.toasts, toast]
    }));

    window.setTimeout(() => {
      get().removeToast(toast.id);
    }, toast.durationMs);
  },

  removeToast: (id) => {
    set((state) => ({
      toasts: state.toasts.filter((toast) => toast.id !== id)
    }));
  },

  setCelebration: (celebration) => set({ celebration }),

  clearBoard: () => {
    const board = createEmptyBoard(BOARD_SIZE);
    set({
      board,
      analysis: analyzeBoard(board, BOARD_SIZE),
      hoverCell: null,
      lastMoveCellKey: null,
      invalidMoveCellKey: null,
      gameState: 'playing'
    });
  },

  setHoverCell: (hoverCell) => set({ hoverCell }),

  loadBoard: (board) => {
    const analysis = analyzeBoard(board, BOARD_SIZE);
    set({
      board: [...board],
      analysis,
      hoverCell: null,
      invalidMoveCellKey: null,
      gameState: analysis.isValid ? 'playing' : 'invalid_move'
    });
  },

  toggleQueen: (row, col) => {
    const { board } = get();
    const nextBoard = [...board];
    const placingQueen = board[row] !== col;
    nextBoard[row] = placingQueen ? col : -1;

    const analysis = analyzeBoard(nextBoard, BOARD_SIZE);
    const currentCell = `${row}-${col}`;
    const invalidMoveCellKey = placingQueen && analysis.conflictingCells.has(currentCell) ? currentCell : null;

    set({
      board: nextBoard,
      analysis,
      lastMoveCellKey: currentCell,
      invalidMoveCellKey,
      gameState: invalidMoveCellKey ? 'invalid_move' : 'playing'
    });

    return {
      placingQueen,
      isValidMove: !invalidMoveCellKey,
      analysis
    };
  }
}));
