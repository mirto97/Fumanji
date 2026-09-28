package com.project.fumanji.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import com.project.fumanji.dto.GameStateResponse;
import com.project.fumanji.dto.PlayerStateResponse;
import com.project.fumanji.entity.Game;

@Component
public class GameStateAssembler {

    public GameStateResponse toResponse(Game game) {
        List<PlayerStateResponse> players = game.getPlayers().stream()
                .map(player -> new PlayerStateResponse(player.getName(), player.getPosition(),
                        player.getSnack(), player.getDrink(), player.getCheckpoint()))
                .toList();

        List<Integer> choiceOptions = game.isAwaitingChoice() ? List.copyOf(game.getChoiceOptions()) : null;
        String winnerName = game.getWinner() != null ? game.getWinner().getName() : null;

        return new GameStateResponse(game.getCurrentPlayer().getName(), game.getTurnPhase(),
                game.getLastRoll(), players, choiceOptions, winnerName);
    }
}