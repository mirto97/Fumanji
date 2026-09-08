package entity.squares;

import java.util.List;

import entity.Game;
import entity.Player;
import entity.Square;

public class PortalSquare extends Square {

    // posizioni dei portali collegati
    private final List<Integer> linkedPositions;

    public PortalSquare(int position, List<Integer> linkedPositions) {
        super(position);
        this.linkedPositions = linkedPositions;
    }

    @Override
    public void applyEffect(Player player, Game game) {
        if (linkedPositions.size() > 1) {
            game.requestPortalChoice(player, linkedPositions);
        } else {
            game.teleportTo(player, linkedPositions.get(0));
        }
    }
}