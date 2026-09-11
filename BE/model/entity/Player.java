package entity;

public class Player {

    private final String name;
    private int position;
    private int snack;
    private int drink;
    private int checkpoint;     // la posizione dell'ultimo checkpoint che ha raggiunto il player

    public Player(String name) {
        this.name = name;
        this.position = 0;
        this.snack = 0;
        this.drink = 0;
        this.checkpoint = 0;    // il primo checkpoint è a partenza
    }

    public int getCheckpoint() {
        return checkpoint;
    }

    void setCheckpoint(int checkpoint) {
        // per non farlo andare a meno della partenza
        this.checkpoint = Math.max(0, checkpoint);
    }

    public String getName() {
        return name;
    }

    public int getPosition() {
        return position;
    }

    void setPosition(int position) {
        this.position = position;
    }

    public int getSnack() {
        return snack;
    }

    public void setSnack(int snack) {
        this.snack = snack;
    }

    public int getDrink() {
        return drink;
    }

    public void setDrink(int drink) {
        this.drink = drink;
    }

    
}
