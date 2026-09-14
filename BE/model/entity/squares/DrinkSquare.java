package entity.squares;

import entity.Game;
import entity.Player;
import entity.Square;

public class DrinkSquare extends Square {

    private final int amount;   // può essere sia positivo che negativo

    public DrinkSquare(int position, int amount) {
        super(position);
        this.amount = amount;
    }

    @Override
    public void applyEffect(Player player, Game game) {
        game.addDrink(player, amount);
    }
}