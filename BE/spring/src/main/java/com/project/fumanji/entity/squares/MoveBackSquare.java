package com.project.fumanji.entity.squares;

import com.project.fumanji.entity.Game;
import com.project.fumanji.entity.Player;
import com.project.fumanji.entity.Square;

public class MoveBackSquare extends Square {

    private final int steps;

    public MoveBackSquare(int position, int steps) {
        super(position);
        this.steps = steps;
    }

    @Override
    public void applyEffect(Player player, Game game) {
        game.moveBackwardBy(player, steps);
    }

    @Override
    public String getDescription() {
        return "Indietro di " + steps + " caselle";
    }
}