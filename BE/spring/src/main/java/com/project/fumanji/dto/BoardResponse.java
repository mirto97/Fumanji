package com.project.fumanji.dto;

import java.util.List;

public record BoardResponse(int finishPosition, List<SquareResponse> squares) {
    
}
