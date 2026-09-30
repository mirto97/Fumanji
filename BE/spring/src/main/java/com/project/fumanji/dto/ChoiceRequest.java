package com.project.fumanji.dto;

import jakarta.validation.constraints.NotNull;

public record ChoiceRequest(@NotNull Integer position) {
    
}
