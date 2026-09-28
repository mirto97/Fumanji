package com.project.fumanji.entity.squares;

import com.project.fumanji.entity.Game;
import com.project.fumanji.entity.Player;
import com.project.fumanji.entity.Square;

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