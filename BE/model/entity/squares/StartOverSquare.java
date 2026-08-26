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
        player.setPosition(0);
        game.landOnCurrentSquare(player);   
        // per ora la tengo, ma la partenza non penso avrà mai degli effetti o comunque sarà una possibilità remota
    }
}