public class Player {

    private final String name;
    private int position;
    private int snack;
    private int drink;

    public Player(String name) {
        this.name = name;
        this.position = 0;
        this.snack = 0;
        this.drink = 0;
    }

    public String getName() {
        return name;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
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
