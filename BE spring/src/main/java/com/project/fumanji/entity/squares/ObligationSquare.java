package com.project.fumanji.entity.squares;

import com.project.fumanji.entity.Game;
import com.project.fumanji.entity.Player;
import com.project.fumanji.entity.Square;

public class ObligationSquare extends Square {

    private final String message;

    public ObligationSquare(int position, String message) {
        super(position);
        this.message = message;
    }

    @Override
    public void applyEffect(Player player, Game game) {
        System.out.println(buildMessage(player, game));
    }

    /**
     * metodo per costruire il messaggio che poi diventerà tutti i tipi di messaggio che possono comparire:
     *  "obbliga qualcuno a fare qualcosa, fai 2 tiri, bong, fai fumare player2 dalla tua, creazione"
     * TODO aggiungere controllo che se sceglie di dare, togliere eat/drink si attivi il metodo per farlo, ma per il futuro più lontano
     *  non vorrei fare che i giocatori possono far cambiare posizione agli altri giocatori, sarebbe troppo da infame, ma in un futuro ancora più lontano, può darsi
     * @param game
     * @return
     */
    public String buildMessage(Player player, Game game) {
        return message.replace("{player}", player.getName());
    }
}
