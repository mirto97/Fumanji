package com.project.fumanji.service;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import com.project.fumanji.model.Game;

/**
 * Registro in-memory delle partite in corso: nessuna persistenza per mossa,
 * il salvataggio su DB avviene solo a fine partita (vedi EndGameEntity, futuro).
 * ConcurrentHashMap perche' il bean e' singleton e le richieste REST arrivano
 * concorrenti su thread diversi.
 */
@Component
public class GameRegistry {

    private final Map<UUID, Game> games = new ConcurrentHashMap<>();

    public UUID register(Game game) {
        UUID gameId = UUID.randomUUID();
        games.put(gameId, game);
        return gameId;
    }

    public Optional<Game> get(UUID gameId) {
        return Optional.ofNullable(games.get(gameId));
    }

    public void remove(UUID gameId) {
        games.remove(gameId);
    }
}