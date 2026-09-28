package com.project.fumanji.dto;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

public record CreateGameRequest(@NotEmpty List<@NotBlank String> playerNames) {
    
}
