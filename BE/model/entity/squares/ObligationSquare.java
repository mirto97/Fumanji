package entity.squares;

import entity.Game;
import entity.Player;
import entity.Square;

public class ObligationSquare extends Square {

    public ObligationSquare(int position) {
        super(position);
    }

    @Override
    public void applyEffect(Player player, Game game) {
        System.out.println(buildMessage(game));
    }

    /**
     * metodo per crostruire il messaggio che poi diventerà tutti i tipi di messaggio che possono comparire:
     *  "obbliga qualcuno a fare qualcosa, fai 2 tiri, bong, fai fumare player2 dalla tua, creazione"
     * TODO aggiungere controllo che se sceglie di dare, togliere eat/drink si attivi il metodo per farlo, ma per il futuro più lontano
     *  non vorrei fare che i giocatori possono far cambiare posizione agli altri giocatori, sarebbe troppo da infame, ma in un futuro ancora più lontano, può darsi
     * @param game
     * @return
     */
    public String buildMessage(Game game) {
        StringBuilder builder = new StringBuilder();
        int finishPosition = game.getBoard().getFinishPosition();

        for (Player p : game.getPlayers()) {
            builder.append(p.getName())
                   .append(" ")
                   .append(p.getPosition())
                   .append("/")
                   .append(finishPosition)
                   .append("\n");
        }

        return builder.toString();
    }
}
