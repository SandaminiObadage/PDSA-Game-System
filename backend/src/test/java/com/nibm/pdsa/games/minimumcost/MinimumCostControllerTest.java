package com.nibm.pdsa.games.minimumcost;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nibm.pdsa.games.minimumcost.controller.MinimumCostController;
import com.nibm.pdsa.games.minimumcost.dto.AlgorithmRunResponse;
import com.nibm.pdsa.games.minimumcost.dto.MinimumCostHistoryResponse;
import com.nibm.pdsa.games.minimumcost.dto.MinimumCostLeaderboardResponse;
import com.nibm.pdsa.games.minimumcost.dto.MinimumCostRoundResponse;
import com.nibm.pdsa.games.minimumcost.dto.MinimumCostSubmissionRequest;
import com.nibm.pdsa.games.minimumcost.dto.MinimumCostSubmissionResponse;
import com.nibm.pdsa.games.minimumcost.service.MinimumCostService;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class MinimumCostControllerTest {

    @Test
    void shouldStartRound() throws Exception {
        MockMvc mockMvc = buildMockMvc();

        mockMvc.perform(post("/api/games/minimum-cost/game/start"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roundId").value(1))
                .andExpect(jsonPath("$.hungarian.totalCost").value(120));
    }

    @Test
    void shouldReturnLeaderboard() throws Exception {
        MockMvc mockMvc = buildMockMvc();

        mockMvc.perform(get("/api/games/minimum-cost/leaderboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roundId").value(1));
    }

    @Test
    void shouldAcceptSubmission() throws Exception {
        MockMvc mockMvc = buildMockMvc();

        mockMvc.perform(post("/api/games/minimum-cost/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"playerName\":\"Alice\",\"roundId\":1,\"submittedCost\":120}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correct").value(true));
    }

    @Test
    void shouldReturnHistory() throws Exception {
        MockMvc mockMvc = buildMockMvc();

        mockMvc.perform(get("/api/games/minimum-cost/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRounds").value(0));
    }

    private MockMvc buildMockMvc() {
        MinimumCostService service = new StubMinimumCostService();
        return MockMvcBuilders.standaloneSetup(new MinimumCostController(service)).build();
    }

    private static class StubMinimumCostService extends MinimumCostService {
        StubMinimumCostService() {
            super(null, new ObjectMapper());
        }

        @Override
        public MinimumCostRoundResponse startRound() {
            AlgorithmRunResponse hungarian = new AlgorithmRunResponse("HUNGARIAN", "OPTIMAL", 120, 5, new int[]{0, 1, 2});
            AlgorithmRunResponse alternative = new AlgorithmRunResponse("GREEDY_LOCAL_OPTIMIZATION", "GREEDY_WITH_SWAP_REFINEMENT", 140, 7, new int[]{1, 0, 2});
            return new MinimumCostRoundResponse(1L, 1L, 3, new int[][]{{1, 2, 3}, {4, 5, 6}, {7, 8, 9}}, "2026-04-20T00:00:00Z", hungarian, alternative);
        }

        @Override
        public MinimumCostLeaderboardResponse getLeaderboard(Integer limit, Long roundId) {
            return new MinimumCostLeaderboardResponse(1L, List.of());
        }

        @Override
        public MinimumCostSubmissionResponse submit(MinimumCostSubmissionRequest request) {
            return new MinimumCostSubmissionResponse(1L, request.playerName(), request.submittedCost(), 120, true, false, "Optimal submission saved.");
        }

        @Override
        public MinimumCostHistoryResponse getHistory(int limit) {
            return new MinimumCostHistoryResponse(List.of(), List.of(), 0, 0, 0, 0.0, 0.0, 0.0, 0, 0);
        }
    }
}