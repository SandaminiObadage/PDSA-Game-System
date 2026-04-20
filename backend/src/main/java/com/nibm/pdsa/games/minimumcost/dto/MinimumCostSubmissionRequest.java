package com.nibm.pdsa.games.minimumcost.dto;

import java.util.List;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MinimumCostSubmissionRequest(
        @NotBlank String playerName,
        Long roundId,
        @NotNull @Min(0) Integer submittedCost,
        List<Integer> assignment
) {
}