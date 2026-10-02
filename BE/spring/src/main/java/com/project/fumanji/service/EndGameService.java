package com.project.fumanji.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.fumanji.entity.BoardEntity;
import com.project.fumanji.entity.EndGameEntity;
import com.project.fumanji.model.Game;
import com.project.fumanji.repository.BoardRepository;
import com.project.fumanji.repository.EndGameRepository;

/**
 * Salva su DB l'esito di una partita conclusa.
 * Fase attuale: solo vincitore e board giocata - punto di estensione per lo storico completo.
 */
@Service
public class EndGameService {

    private final BoardRepository boardRepository;
    private final EndGameRepository endGameRepository;

    public EndGameService(BoardRepository boardRepository, EndGameRepository endGameRepository) {
        this.boardRepository = boardRepository;
        this.endGameRepository = endGameRepository;
    }

    @Transactional
    public void save(Game game, Long boardId) {
        BoardEntity board = boardRepository.getReferenceById(boardId);
        endGameRepository.save(new EndGameEntity(game.getWinner().getName(), board));
    }
}