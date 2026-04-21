import { MemoryRouter } from 'react-router-dom';
import { act, fireEvent, render, screen, waitFor } from '@testing-library/react';
import SixteenQueensPage from './page';
import { useQueensGameStore } from '../../features/sixteen-queens/useQueensGameStore';
import * as api from '../../api/sixteenQueensApi';

vi.mock('../../api/sixteenQueensApi');

function renderPage() {
  return render(
    <MemoryRouter>
      <SixteenQueensPage />
    </MemoryRouter>
  );
}

describe('SixteenQueensPage UI', () => {
  beforeEach(() => {
    api.fetchSixteenQueensHistory.mockResolvedValue({ rounds: [], recentAnswers: [] });
    api.fetchSixteenQueensLeaderboard.mockResolvedValue({ leaderboard: [] });
    api.fetchSixteenQueensReport.mockResolvedValue({
      totalKnownSolutionsPersisted: 20,
      activeRecognizedSolutions: 1,
      averageSequentialTimeMs: 1200,
      averageParallelTimeMs: 500
    });
    api.submitSixteenQueens.mockResolvedValue({
      correct: true,
      alreadyRecognized: false,
      message: 'Unique!'
    });
    api.solveSixteenQueens.mockResolvedValue({
      gameRoundId: 1,
      sequentialTimeMs: 1000,
      parallelTimeMs: 400,
      speedup: 2.5,
      sampleSolutions: []
    });
    api.fetchSixteenQueensSamples.mockResolvedValue({ sampleSolutions: [] });
    api.closeSixteenQueensRound.mockResolvedValue({});
    api.resetSixteenQueensRecognized.mockResolvedValue({});

    useQueensGameStore.getState().clearBoard();
    useQueensGameStore.setState({
      submitForm: { playerName: '', gameRoundId: '' },
      celebration: null,
      gameState: 'playing',
      toasts: []
    });
  });

  it('places queens and marks conflict lanes', async () => {
    const { container } = renderPage();

    const row1col1 = await screen.findByRole('button', { name: /^Row 1 column 1$/i });
    const row2col1 = screen.getByRole('button', { name: /^Row 2 column 1$/i });

    fireEvent.click(row1col1);
    fireEvent.click(row2col1);

    expect(container.querySelectorAll('.conflict-lane').length).toBeGreaterThan(0);
  });

  it('shows duplicate toast when backend marks solution as already recognized', async () => {
    api.submitSixteenQueens.mockResolvedValueOnce({
      correct: true,
      alreadyRecognized: true,
      message: 'Duplicate solution'
    });

    renderPage();

    const validBoard = Array.from({ length: 16 }, (_, index) => index);
    await act(async () => {
      useQueensGameStore.setState({
        board: validBoard,
        analysis: {
          queensPlaced: 16,
          isComplete: true,
          isValid: true,
          conflictingCells: new Set(),
          conflictRows: new Set(),
          conflictColumns: new Set(),
          conflictDiagonals: new Set(),
          conflictAntiDiagonals: new Set()
        }
      });
    });

    fireEvent.change(screen.getByLabelText(/Player name/i), { target: { value: 'Ari' } });
    fireEvent.click(screen.getByRole('button', { name: /Submit arrangement/i }));

    await waitFor(() => {
      expect(screen.getByText(/already discovered/i)).toBeInTheDocument();
    });
  });
});
