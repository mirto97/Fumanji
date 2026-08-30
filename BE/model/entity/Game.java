package entity;

import java.util.List;

public class Game {

    // final perchè le settiamo nel costruttore e non cambieranno mai più
    private final Board board;
    private final List<Player> players;
    private final Dice dice;
    private int currentPlayerIndex;     // è l'indice (posizione nell'array/lista, da 0) del giocatore di cui è il turno in players
    private Player winner;
    private int lastRoll;
    private Player lastPlayerToMove;

    public Game(Board board, List<Player> players, Dice dice) {
        if (board == null) {
            throw new IllegalArgumentException("Board must not be null");
        }
        if (dice == null) {
            throw new IllegalArgumentException("Dice must not be null");
        }
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
        rollAndMove(currentPlayer);

        if (winner == null) {
            currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
        }
    }

    /**
     * metodo default che lancia il dado e muove in avanti il player
     * @param player
     */
    public void rollAndMove(Player player) {
        int rollValue = roll(player);
        moveForwardBy(player, rollValue);
    }

    /**
     * metodo solo per tirare il dado e salvare il lastroll
     * @param player
     * @return
     */
    public int roll(Player player) {
        int rollValue = dice.roll();
        lastRoll = rollValue;
        lastPlayerToMove = player;
        return rollValue;
    }
    /**
     * metodo solo per muovere di lastroll steps nella direzione scelta all'input
     * @param player
     * @param forward
     */
    public void moveByChosenDirection(Player player, boolean forward) {
        // se non specificato, va avanti 
        if (forward || !board.isDirectionChoiceEnabled()) {
            moveForwardBy(player, lastRoll);
        } else {
            moveBackwardBy(player, lastRoll);
        }
    }

    /**
     * metodo che muove avanti il giocatore di STEPS in input
     * @param player
     * @param steps
     */
    public void moveForwardBy(Player player, int steps) {

        // controllo in caso di rimbalzo overshoot sull'arrivo
        int newPosition = player.getPosition() + steps;
        int finishPosition = board.getFinishPosition();
        if (newPosition > finishPosition) {
            int overshoot = newPosition - finishPosition;
            newPosition = finishPosition - overshoot;
        }

        player.setPosition(newPosition);
        landOnCurrentSquare(player);
    }

    /**
     *  " indietro, con clamp al checkpoint del giocatore
     * @param player
     * @param steps
     */
    public void moveBackwardBy(Player player, int steps) {
        // per controllare che non vada a meno di 0, la partenza
        // Math.max returna il più grande tra i 2 valori in input { (a >= b) ? a : b; }      
        int newPosition = Math.max(player.getCheckpoint(), player.getPosition() - steps);
        player.setPosition(newPosition);
        landOnCurrentSquare(player);
    }

    /**
     * metodo per applicare l'effetto delle caselle sul giocatore
     * @param player
     */
    public void landOnCurrentSquare(Player player) {
        // controllo se il giocatore è arrivato al traguardo
        if (player.getPosition() == board.getFinishPosition()) {
            winner = player;
            return;
        }

        Square square = board.getSquares().get(player.getPosition());
        square.applyEffect(player, this);
    }

    /**
     * metodo per riportare il player all'inizio o all'ultimo checkpoint
     * @param player
     */
    public void resetToStart(Player player) {
        player.setPosition(player.getCheckpoint());
    }

    /**
     * metodo per fissare l'ultimo checkpoint che ha raggiunto il player
     * @param player
     */
    public void updateCheckpoint(Player player) {
        player.setCheckpoint(player.getPosition());
    }

    /**
     * metodo per teletrasportare il giocatore (per i portali)
     * @param player
     * @param position
     */
    public void teleportTo(Player player, int position) {
        player.setPosition(position);
    }

    // getter setter

    public Board getBoard() {
        return board;
    }

    public Player getWinner() {
        return winner;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public int getLastRoll() {
        return lastRoll;
    }

    public Player getLastPlayerToMove() {
        return lastPlayerToMove;
    }
}