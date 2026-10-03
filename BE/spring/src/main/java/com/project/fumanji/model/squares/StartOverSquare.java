package com.project.fumanji.model.squares;

import com.project.fumanji.model.Game;
import com.project.fumanji.model.Player;
import com.project.fumanji.model.Square;

public class StartOverSquare extends Square {

    private static final String MESSAGE_TEMPLATE = "{player} torna all'ultimo checkpoint";

    public StartOverSquare(int position) {
        super(position);
    }

    @Override
    public void applyEffect(Player player, Game game) {
        game.resetToStart(player, buildMessage(player));
        game.landOnCurrentSquare(player);  
    }

    @Override
    public String getDescription() {
        return "Torna all'ultimo checkpoint (o alla partenza)";
    }

    public String buildMessage(Player player) {
        return MESSAGE_TEMPLATE.replace("{player}", player.getName());
    }
}