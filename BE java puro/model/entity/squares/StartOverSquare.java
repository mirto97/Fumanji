package entity.squares;

import entity.Game;
import entity.Player;
import entity.Square;

public class StartOverSquare extends Square {

    public StartOverSquare(int position) {
        super(position);
    }

    @Override
    public void applyEffect(Player player, Game game) {
        game.resetToStart(player);
        game.landOnCurrentSquare(player);  
    }
}