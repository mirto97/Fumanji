package entity;

import java.util.List;

public class Square {

    private final int position;
    // è il numero progressivo della casella sul tabellone (da 1 a size), assegnato da Board quando crea la lista.

    public Square(int position) {
        this.position = position;
    }

    public int getPosition() {
        return position;
    }

    public void applyEffect(Player player, Game game) {
        // qua è vuoto perchè le caselle vuote non fanno niente
        // nelle classi figlie verrà overridato (con gli effetti delle caselle speciali)
    }

    /**
     * metodo che restituisce la/e prossima/e casella/e della board, rispetto alla posizione della casella attuale
     * @return
     */
    public List<Integer> getNextPositions() {
        return List.of(position + 1);
    }

    /**
     * metodo che restituisce la/e precedente/i casella/e della board, rispetto alla posizione della casella attuale
     * @return
     */
    public List<Integer> getPreviousPositions() {
        return List.of(position - 1);
    }
}
