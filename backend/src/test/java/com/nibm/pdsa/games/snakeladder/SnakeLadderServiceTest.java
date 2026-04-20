package com.nibm.pdsa.games.snakeladder;

import com.nibm.pdsa.games.snakeladder.dto.SolveComparisonResponse;
import com.nibm.pdsa.games.snakeladder.dto.SubmitAnswerRequest;
import com.nibm.pdsa.games.snakeladder.dto.SubmitAnswerResponse;
import com.nibm.pdsa.games.snakeladder.service.SnakeLadderService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SnakeLadderServiceTest {

    private final SnakeLadderService service = new SnakeLadderService(null);

    @Test
    void shouldGenerateValidBoard() {
        SolveComparisonResponse response = service.runComparison(6);

        assertEquals(6, response.getBoardSize());
        assertEquals(4, response.getLadders().size()); // 6-2=4
        assertEquals(4, response.getSnakes().size());
        assertEquals(response.getBfsMinThrows(), response.getDpMinThrows());
        assertNotNull(response.getChoices());
        assertEquals(3, response.getChoices().size());
        assertTrue(response.getChoices().contains(response.getBfsMinThrows()));
    }

    @Test
    void shouldRejectInvalidBoardSize() {
        assertThrows(IllegalArgumentException.class, () -> service.runComparison(5));
        assertThrows(IllegalArgumentException.class, () -> service.runComparison(13));
    }

    @Test
    void shouldHandleAnswerSubmission() {
        // First create a round
        SolveComparisonResponse solveResponse = service.runComparison(8);
        Long roundId = solveResponse.getGameRoundId();

        // Submit correct answer
        SubmitAnswerRequest request = new SubmitAnswerRequest();
        request.setGameRoundId(roundId);
        request.setPlayerName("TestPlayer");
        request.setAnswer(solveResponse.getBfsMinThrows());
        request.setBoardSize(8);

        // Since repository is null, it should handle gracefully
        SubmitAnswerResponse response = service.submitAnswer(request);
        assertNotNull(response);
        // Since no DB, it will say invalid round
        assertFalse(response.isCorrect());
    }
}