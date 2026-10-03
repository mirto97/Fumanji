package com.project.fumanji.model.squares;

import com.project.fumanji.model.Game;
import com.project.fumanji.model.Player;
import com.project.fumanji.model.Square;

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
    
    @Override
    public String getDescription() {
        // alternativa: String.format("%+d snack", amount), che sceglie il segno da solo
        return (amount<0 ? amount : "+"+amount) + " snack";
    }
}
