package com.project.fumanji.dto;

import java.util.List;

public record SquareResponse(int position,
        SquareType type,
        String description,
        List<Integer> nextPositions,
        List<Integer> previousPositions,
        List<Integer> linkedPositions) {
    
}
