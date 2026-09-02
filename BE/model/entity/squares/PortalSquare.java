package entity.squares;

import entity.Game;
import entity.Player;
import entity.Square;

public class PortalSquare extends Square {

    // posizione del portale collegato
    private final int linkedPosition;

    // Bidirezionalità ottenuta semplicemente piazzando due istanze che si linkano a vicenda (PortalSquare(5, 15) e PortalSquare(15, 5))

    public PortalSquare(int position, int linkedPosition) {
        super(position);
        this.linkedPosition = linkedPosition;
    }

    @Override
    public void applyEffect(Player player, Game game) {
        game.teleportTo(player, linkedPosition);
    }
}