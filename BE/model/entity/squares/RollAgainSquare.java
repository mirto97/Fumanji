package entity.squares;

import entity.Game;
import entity.Player;
import entity.Square;

public class RollAgainSquare extends Square {

    public RollAgainSquare(int position) {
        super(position);
    }

    @Override
    public void applyEffect(Player player, Game game) {
        game.rollAndMove(player);
    }
}