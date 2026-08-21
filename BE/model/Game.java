import java.util.List;

public class Game {

    // final perchè le settiamo nel costruttore e non cambieranno mai più
    private final Board board;
    private final List<Player> players;
    private final Dice dice;
    private int currentPlayerIndex;     // è l'indice (posizione nell'array/lista, da 0) del giocatore di cui è il turno in players
    private Player winner;

    public Game(Board board, List<Player> players, Dice dice) {
        if (players == null || players.isEmpty()) {
            throw new IllegalArgumentException("At least one player is required");
        }
        this.board = board;
        this.players = players;
        this.dice = dice;
        this.currentPlayerIndex = 0;
        this.winner = null;
    }

    public void playTurn() {
         
        if (winner != null) {
            return;
        }

        // il giocatore di turno è quello nella posizione currentPlayerIndex della lista players
        Player currentPlayer = players.get(currentPlayerIndex);
        int roll = dice.roll();
        int newPosition = currentPlayer.getPosition() + roll;

        if (newPosition > board.getFinishPosition()) {
            int overshoot = newPosition - board.getFinishPosition();
            newPosition = board.getFinishPosition() - overshoot;
        }

        currentPlayer.setPosition(newPosition);

        if (newPosition == board.getFinishPosition()) {
            winner = currentPlayer;
            return;
        }

        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
        //per passare al prossimo, tornando a 0 dopo l'ultimo (modulo = "giro" circolare)
    }

    public Player getWinner() {
        return winner;
    }

    public List<Player> getPlayers() {
        return players;
    }
}
