package com.project.fumanji.entity;

/**
 * Dado di test: valore del prossimo tiro impostabile a piacere,
 * per coprire nei test scenari che dipendono dal risultato del dado.
 */
public class ControllableDice extends Dice {

    private int nextRoll = 1;

    public void setNextRoll(int nextRoll) {
        this.nextRoll = nextRoll;
    }

    @Override
    public int roll() {
        return nextRoll;
    }
}