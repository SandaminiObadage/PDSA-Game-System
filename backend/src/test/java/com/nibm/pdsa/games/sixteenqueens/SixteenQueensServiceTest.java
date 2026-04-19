package com.nibm.pdsa.games.sixteenqueens;

import com.nibm.pdsa.games.sixteenqueens.dto.SubmitAnswerRequest;
import com.nibm.pdsa.games.sixteenqueens.dto.SubmitAnswerResponse;
import com.nibm.pdsa.games.sixteenqueens.service.SixteenQueensService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SixteenQueensServiceTest {

    @Test
    void shouldRejectAlreadyRecognizedAnswer() {
        SixteenQueensService service = new SixteenQueensService(null);

        SubmitAnswerRequest first = new SubmitAnswerRequest();
        first.setBoardSize(8);
        first.setPlayerName("A");
        first.setAnswer("0,4,7,5,2,6,1,3");

        SubmitAnswerRequest second = new SubmitAnswerRequest();
        second.setBoardSize(8);
        second.setPlayerName("B");
        second.setAnswer("0,4,7,5,2,6,1,3");

        SubmitAnswerResponse r1 = service.submitAnswer(first);
        SubmitAnswerResponse r2 = service.submitAnswer(second);

        assertTrue(r1.isCorrect());
        assertFalse(r1.isAlreadyRecognized());

        assertTrue(r2.isCorrect());
        assertTrue(r2.isAlreadyRecognized());
    }
}
