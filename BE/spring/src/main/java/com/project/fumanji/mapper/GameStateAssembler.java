package com.project.fumanji.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import com.project.fumanji.dto.GameStateResponse;
import com.project.fumanji.dto.MovementResponse;
import com.project.fumanji.dto.PlayerStateResponse;
import com.project.fumanji.model.Game;

@Component
public class GameStateAssembler {

    public GameStateResponse toResponse(Game game) {
        List<PlayerStateResponse> players = game.getPlayers().stream()
                .map(player -> new PlayerStateResponse(player.getName(), player.getPosition(),
                        player.getSnack(), player.getDrink(), player.getCheckpoint()))
                .toList();

        List<String> turnMessages = game.getTurnMessages();

        List<Integer> choiceOptions = game.isAwaitingChoice() ? List.copyOf(game.getChoiceOptions()) : null;
        Integer choiceRemainingSteps = game.isAwaitingChoice() && game.getChoiceRemainingSteps() > 0
            ? game.getChoiceRemainingSteps()
            : null;
        
        String winnerName = game.getWinner() != null ? game.getWinner().getName() : null;

        List<MovementResponse> movements = game.getMovementTrace().stream()
                .map(movement -> new MovementResponse(movement.position(), movement.type(), movement.message()))
                .toList();
        Integer movedPlayerIndex = movements.isEmpty()
                ? null
                : game.getPlayers().indexOf(game.getLastPlayerToMove());

        return new GameStateResponse(game.getCurrentPlayer().getName(), game.getTurnPhase(),
                game.getLastRoll(), players, choiceOptions, choiceRemainingSteps, winnerName,
                turnMessages.isEmpty() ? null : turnMessages,
                movedPlayerIndex, movements.isEmpty() ? null : movements);    
    }
}