package com.project.fumanji.api;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.fumanji.dto.ChoiceRequest;
import com.project.fumanji.dto.CreateGameRequest;
import com.project.fumanji.dto.CreateGameResponse;
import com.project.fumanji.dto.GameStateResponse;
import com.project.fumanji.dto.MoveRequest;
import com.project.fumanji.mapper.GameStateAssembler;
import com.project.fumanji.model.Game;
import com.project.fumanji.service.GameCreationResult;
import com.project.fumanji.service.GameService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/games")
public class GameController {

    // TODO commentareeeee

    private final GameService gameService;
    private final GameStateAssembler gameStateAssembler;

    public GameController(GameService gameService, GameStateAssembler gameStateAssembler) {
        this.gameService = gameService;
        this.gameStateAssembler = gameStateAssembler;
    }

    @PostMapping
    public ResponseEntity<CreateGameResponse> createGame(@RequestBody @Valid CreateGameRequest request) {        GameCreationResult result = gameService.createGame(request.playerNames());
        return ResponseEntity.ok(new CreateGameResponse(result.gameId(), result.currentPlayerName()));
    }

    @PostMapping("/{gameId}/roll")
    public ResponseEntity<GameStateResponse> roll(@PathVariable UUID gameId) {
        Game game = gameService.roll(gameId);
        return ResponseEntity.ok(gameStateAssembler.toResponse(game));
    }

    @PostMapping("/{gameId}/move")
    public ResponseEntity<GameStateResponse> move(@PathVariable UUID gameId,
            @RequestBody @Valid MoveRequest request) {
        Game game = gameService.move(gameId, request.forward());
        return ResponseEntity.ok(gameStateAssembler.toResponse(game));
    }

    @PostMapping("/{gameId}/choice")
    public ResponseEntity<GameStateResponse> choice(@PathVariable UUID gameId,
            @RequestBody @Valid ChoiceRequest request) {
        Game game = gameService.choose(gameId, request.position());
        return ResponseEntity.ok(gameStateAssembler.toResponse(game));
    }

    @GetMapping("/{gameId}")
    public ResponseEntity<GameStateResponse> getGame(@PathVariable UUID gameId) {
        Game game = gameService.getGame(gameId);
        return ResponseEntity.ok(gameStateAssembler.toResponse(game));
    }
}