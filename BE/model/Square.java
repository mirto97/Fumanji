public class Square {

    private final int position;
    // è il numero progressivo della casella sul tabellone (da 1 a size), assegnato da Board quando crea la lista.

    public Square(int position) {
        this.position = position;
    }

    public int getPosition() {
        return position;
    }
}
