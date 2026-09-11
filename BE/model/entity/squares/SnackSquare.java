package entity.squares;

import entity.Game;
import entity.Player;
import entity.Square;

public class SnackSquare extends Square {

    private final int amount;   // può essere sia positivo che negativo

    public SnackSquare(int position, int amount) {
        super(position);
        this.amount = amount;
    }

    @Override
    public void applyEffect(Player player, Game game) {
        int newValue = Math.max(0, player.getSnack() + amount);
        player.setSnack(newValue);
    }
}