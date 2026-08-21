import java.util.ArrayList;
import java.util.List;

public class Board {

    private final int finishPosition;
    private final List<Square> squares;

    public Board(int size) {
        if (size <= 0) {
            throw new IllegalArgumentException("Board size must be positive");
        }
        this.finishPosition = size;
        this.squares = new ArrayList<>();
        for (int position = 0; position <= size; position++) {
            squares.add(new Square(position));
        }
    }

    public int getFinishPosition() {
        return finishPosition;
    }

    public List<Square> getSquares() {
        return squares;
    }
}
