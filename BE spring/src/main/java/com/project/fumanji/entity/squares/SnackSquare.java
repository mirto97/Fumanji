package com.project.fumanji.entity.squares;

import com.project.fumanji.entity.Game;
import com.project.fumanji.entity.Player;
import com.project.fumanji.entity.Square;

public class SnackSquare extends Square {

    private final int amount;   // può essere sia positivo che negativo

    public SnackSquare(int position, int amount) {
        super(position);
        this.amount = amount;
    }

    @Override
    public void applyEffect(Player player, Game game) {
        game.addSnack(player, amount);
    }

    public int getAmount() {
        return amount;
    }
    
}
