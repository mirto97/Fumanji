package com.project.fumanji.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.fumanji.dto.CreateGameRequest;
import com.project.fumanji.dto.CreateGameResponse;
import com.project.fumanji.service.GameCreationResult;
import com.project.fumanji.service.GameService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/games")
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @PostMapping
    public ResponseEntity<CreateGameResponse> createGame(@RequestBody @Valid CreateGameRequest request) {        GameCreationResult result = gameService.createGame(request.playerNames());
        return ResponseEntity.ok(new CreateGameResponse(result.gameId(), result.currentPlayerName()));
    }
}