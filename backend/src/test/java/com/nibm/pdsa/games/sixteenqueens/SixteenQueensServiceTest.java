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
        first.setBoardSize(16);
        first.setPlayerName("A");
        first.setAnswer("0,2,4,1,12,8,13,11,14,5,15,6,3,10,7,9");

        SubmitAnswerRequest second = new SubmitAnswerRequest();
        second.setBoardSize(16);
        second.setPlayerName("B");
        second.setAnswer("0,2,4,1,12,8,13,11,14,5,15,6,3,10,7,9");

        SubmitAnswerResponse r1 = service.submitAnswer(first);
        SubmitAnswerResponse r2 = service.submitAnswer(second);

        assertTrue(r1.isCorrect());
        assertFalse(r1.isAlreadyRecognized());

        assertTrue(r2.isCorrect());
        assertTrue(r2.isAlreadyRecognized());
    }
}
