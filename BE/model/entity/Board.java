package entity;

import java.util.ArrayList;
import java.util.List;

public class Board {

    private final int finishPosition;
    private final List<Square> squares;
    private boolean directionChoiceEnabled; // la possibilità si scegliere se andare avanti o indietro
    private boolean mandatoryCheckpointEnabled; // per fermare i giocatori al checkpoint

    public Board(int size) {
        if (size <= 0) {
            throw new IllegalArgumentException("Board size must be positive");
        }
        this.finishPosition = size;
        this.squares = new ArrayList<>();
        for (int position = 0; position <= size; position++) {
            squares.add(new Square(position));
        }
        this.directionChoiceEnabled = false;
        this.mandatoryCheckpointEnabled = false;
    }

    public boolean isDirectionChoiceEnabled() {
        return directionChoiceEnabled;
    }

    public void setDirectionChoiceEnabled(boolean directionChoiceEnabled) {
        this.directionChoiceEnabled = directionChoiceEnabled;
    }

    public int getFinishPosition() {
        return finishPosition;
    }

    public List<Square> getSquares() {
        return squares;
    }

    public boolean isMandatoryCheckpointEnabled() {
        return mandatoryCheckpointEnabled;
    }

    public void setMandatoryCheckpointEnabled(boolean mandatoryCheckpointEnabled) {
        this.mandatoryCheckpointEnabled = mandatoryCheckpointEnabled;
    }
}
