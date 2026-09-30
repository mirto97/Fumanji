package com.project.fumanji.entity.squares;

import com.project.fumanji.entity.Game;
import com.project.fumanji.entity.Player;
import com.project.fumanji.entity.Square;

public class CheckpointSquare extends Square {

    private static final String MESSAGE_TEMPLATE = "{player} ha raggiunto il checkpoint! Quindi deve fare 3 tiri di creazione";

    public CheckpointSquare(int position) {
        super(position);
    }

    @Override
    public void applyEffect(Player player, Game game) {
        game.updateCheckpoint(player);
        game.addMessage(buildMessage(player));
    }

    /**    (non-Javadoc)
     * mi sa che per adesso ho messo che la regola di fermarsi al check è sempre attiva TODO vedremo
     * @see entity.Square#isMandatoryStop()
     */
    @Override
    public boolean isMandatoryStop() {
        return true;
    }

    public String buildMessage(Player player) {
        return MESSAGE_TEMPLATE.replace("{player}", player.getName());
    }

    @Override
    public String getDescription() {
        return MESSAGE_TEMPLATE;
    }
}
