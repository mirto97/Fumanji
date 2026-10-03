package com.project.fumanji.model.squares;

import java.util.List;

import com.project.fumanji.model.Game;
import com.project.fumanji.model.InteractiveSquare;
import com.project.fumanji.model.Player;
import com.project.fumanji.model.Square;


public class BivioSquare extends Square implements InteractiveSquare {

    private final List<Integer> nextPositions;
    private final List<Integer> previousPositions;
    private final Square effect; // nullable, es. una ObligationSquare usata solo per il suo applyEffect

    public BivioSquare(int position, List<Integer> nextPositions, List<Integer> previousPositions, Square effect) {
        super(position);
        this.nextPositions = nextPositions;
        this.previousPositions = previousPositions;
        this.effect = effect;
    }

    @Override
    public void applyEffect(Player player, Game game) {
        if (effect != null) {
            effect.applyEffect(player, game);
        }
    }

    @Override
    public String getDescription() {
        return effect != null ? effect.getDescription() : null;
    }

    @Override
    public List<Integer> getNextPositions() { return nextPositions; }

    @Override
    public List<Integer> getPreviousPositions() { return previousPositions; }
}