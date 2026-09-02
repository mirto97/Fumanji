package entity.squares;

import entity.Game;
import entity.Player;
import entity.Square;

public class CheckpointSquare extends Square {

    public CheckpointSquare(int position) {
        super(position);
    }

    @Override
    public void applyEffect(Player player, Game game) {
        game.updateCheckpoint(player);
        System.out.println(buildMessage(player));
    }

    public String buildMessage(Player player) {
        return player.getName() + " ha raggiunto il checkpoint! Quindi deve fare 3 tiri di creazione";
    }
}
