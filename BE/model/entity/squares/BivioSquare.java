package entity.squares;

import java.util.List;

import entity.InteractiveSquare;
import entity.Square;

public class BivioSquare extends Square implements InteractiveSquare {

    // perchè plurale anche previousPositions? TODO
    
    private final List<Integer> nextPositions;
    private final List<Integer> previousPositions;

    public BivioSquare(int position, List<Integer> nextPositions, List<Integer> previousPositions) {
        super(position);
        this.nextPositions = nextPositions;
        this.previousPositions = previousPositions;
    }

    @Override
    public List<Integer> getNextPositions() { return nextPositions; }

    @Override
    public List<Integer> getPreviousPositions() { return previousPositions; }
}