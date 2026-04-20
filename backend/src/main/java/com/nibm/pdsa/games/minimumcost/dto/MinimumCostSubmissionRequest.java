package com.nibm.pdsa.games.minimumcost.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record MinimumCostSubmissionRequest(
        @NotBlank String playerName,
        Long roundId,
        @NotNull @Min(0) Integer submittedCost,
        List<Integer> assignment
) {
}