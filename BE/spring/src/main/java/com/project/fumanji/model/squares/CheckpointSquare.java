package com.project.fumanji.model.squares;

import java.util.ArrayList;
import java.util.List;

import com.project.fumanji.model.Game;
import com.project.fumanji.model.Player;
import com.project.fumanji.model.Square;

public class CheckpointSquare extends Square {

    private static final String MESSAGE_TEMPLATE = "{player} ha raggiunto il checkpoint! Quindi deve fare 3 tiri di creazione";

    private final List<Integer> previousPositions;

    public CheckpointSquare(int position, List<Integer> previousPositions) {
        super(position);
        this.previousPositions = new ArrayList<>(previousPositions);
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

    @Override
    public List<Integer> getPreviousPositions() {
        return previousPositions.isEmpty() ? super.getPreviousPositions() : previousPositions;
    }
}
