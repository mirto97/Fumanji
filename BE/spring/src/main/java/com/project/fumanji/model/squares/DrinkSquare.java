package com.project.fumanji.model.squares;

import com.project.fumanji.model.Game;
import com.project.fumanji.model.Player;
import com.project.fumanji.model.Square;

public class DrinkSquare extends Square {

    private final int amount;   // può essere sia positivo che negativo

    public DrinkSquare(int position, int amount) {
        super(position);
        this.amount = amount;
    }

    @Override
    public void applyEffect(Player player, Game game) {
        game.addDrink(player, amount);
    }

    public int getAmount() {
        return amount;
    }

    @Override
    public String getDescription() {
        // alternativa: String.format("%+d snack", amount), che sceglie il segno da solo
        return (amount<0 ? amount : "+"+amount) + " drink";
    }
}