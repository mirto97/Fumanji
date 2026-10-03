package com.project.fumanji.dto;

import com.project.fumanji.model.MovementType;

public record MovementResponse(int position, MovementType type, String message) {
    
}
