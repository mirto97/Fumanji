package com.project.fumanji.entity.squares;

import java.util.List;

import com.project.fumanji.entity.Game;
import com.project.fumanji.entity.InteractiveSquare;
import com.project.fumanji.entity.Player;
import com.project.fumanji.entity.Square;

public class PortalSquare extends Square implements InteractiveSquare {

    // posizioni dei portali collegati
    private final List<Integer> linkedPositions;

    public PortalSquare(int position, List<Integer> linkedPositions) {
        super(position);
        this.linkedPositions = linkedPositions;
    }

    @Override
    public void applyEffect(Player player, Game game) {
        if (linkedPositions.size() > 1) {
            game.requestChoice(player, linkedPositions);
        } else {
            game.teleportTo(player, linkedPositions.get(0));
        }
    }

    public List<Integer> getLinkedPositions() {
        return linkedPositions;
    }
}