package com.project.fumanji.dto;

import jakarta.validation.constraints.NotNull;

public record MoveRequest(@NotNull Boolean forward) {
    
}
