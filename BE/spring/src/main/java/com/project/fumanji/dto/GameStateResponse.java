package com.project.fumanji.dto;

import java.util.List;

import com.project.fumanji.entity.TurnPhase;

public record GameStateResponse(String currentPlayerName,
        TurnPhase turnPhase,
        int lastRoll,
        List<PlayerStateResponse> players,
        List<Integer> choiceOptions,
        String winnerName) {
    
}
