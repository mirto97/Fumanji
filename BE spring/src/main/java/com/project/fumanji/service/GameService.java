package com.project.fumanji.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.project.fumanji.entity.Board;
import com.project.fumanji.entity.Dice;
import com.project.fumanji.entity.Game;
import com.project.fumanji.entity.Player;
import com.project.fumanji.persistence.BoardAssembler;
import com.project.fumanji.persistence.BoardEntity;
import com.project.fumanji.repository.BoardRepository;

/**
 * Costruisce una nuova partita e la registra in GameRegistry.
 * Fase attuale: una sola board fissa (id 1, la board OG) - da rivedere
 * quando si supporteranno board multiple.
 */
@Service
public class GameService {

    // hardcoded: coerente con "una sola board fissa", da rivedere quando arriveranno board multiple
    private static final long OG_BOARD_ID = 1L;

    private final BoardRepository boardRepository;
    private final BoardAssembler boardAssembler;
    private final GameRegistry gameRegistry;

    public GameService(BoardRepository boardRepository, BoardAssembler boardAssembler, GameRegistry gameRegistry) {
        this.boardRepository = boardRepository;
        this.boardAssembler = boardAssembler;
        this.gameRegistry = gameRegistry;
    }

    public GameCreationResult createGame(List<String> playerNames) {
        BoardEntity boardEntity = boardRepository.findById(OG_BOARD_ID).orElseThrow();
        Board board = boardAssembler.toDomain(boardEntity);

        List<Player> players = new ArrayList<>();
        for (String name : playerNames) {
            players.add(new Player(name));
        }

        Game game = new Game(board, players, new Dice());
        UUID gameId = gameRegistry.register(game);
        return new GameCreationResult(gameId, players.get(0).getName());
    }
}