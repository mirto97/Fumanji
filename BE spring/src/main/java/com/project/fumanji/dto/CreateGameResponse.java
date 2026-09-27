package com.project.fumanji.dto;

import java.util.UUID;

public record CreateGameResponse(UUID gameId, String currentPlayerName) {
    
}
