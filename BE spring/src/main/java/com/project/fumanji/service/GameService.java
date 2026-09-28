package com.project.fumanji.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.project.fumanji.entity.Board;
import com.project.fumanji.entity.Dice;
import com.project.fumanji.entity.Game;
import com.project.fumanji.entity.Player;
import com.project.fumanji.exception.GameNotFoundException;
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

    // TODO commentareeeee

    // hardcoded: coerente con "una sola board fissa", da rivedere quando arriveranno board multiple
    private static final long OG_BOARD_ID = 1L;

    private final BoardRepository boardRepository;
    private final BoardAssembler boardAssembler;
    private final GameRegistry gameRegistry;
    private final EndGameService endGameService;

    public GameService(BoardRepository boardRepository, BoardAssembler boardAssembler, GameRegistry gameRegistry, EndGameService endGameService) {
        this.boardRepository = boardRepository;
        this.boardAssembler = boardAssembler;
        this.gameRegistry = gameRegistry;
        this.endGameService = endGameService;
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

    public Game roll(UUID gameId) {
        Game game = getGame(gameId);
        game.performRoll();
        return game;
    }

    public Game move(UUID gameId, boolean forward) {
        Game game = getGame(gameId);
        game.performMove(forward);
        closeIfFinished(gameId, game);
        return game;
    }

    public Game choose(UUID gameId, int position) {
        Game game = getGame(gameId);
        game.performChoice(position);
        closeIfFinished(gameId, game);
        return game;
    }

    private void closeIfFinished(UUID gameId, Game game) {
        if (game.getWinner() == null) {
            return;
        }
        endGameService.save(game, OG_BOARD_ID);
        gameRegistry.remove(gameId);
    }

    public Game getGame(UUID gameId) {
        return gameRegistry.get(gameId).orElseThrow(() -> new GameNotFoundException(gameId));
    }
}