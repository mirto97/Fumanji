package entity;
 
import java.util.List;
 
public class Game {

    // TODO commentareeeeeee
 
    private final Board board;
    private final List<Player> players;
    private final Dice dice;
    private int currentPlayerIndex;
    private Player winner;
    private int lastRoll;
    private Player lastPlayerToMove;
    private Player awaitingPlayer;
    private List<Integer> awaitingOptions;
    private int awaitingRemainingSteps;
    private boolean awaitingForward;
    private boolean awaitingRespectCheckpoint;
    private boolean awaitingContinueAfterChoice;
 
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
 
        Player currentPlayer = players.get(currentPlayerIndex);
        rollAndMove(currentPlayer);
 
        if (winner == null) {
            currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
        }
    }
 
    public int roll(Player player) {
        int rollValue = dice.roll();
        lastRoll = rollValue;
        lastPlayerToMove = player;
        return rollValue;
    }
 
    public void rollAndMove(Player player) {
        int rollValue = roll(player);
        moveForwardBy(player, rollValue);
    }
 
    public void moveByChosenDirection(Player player, boolean forward) {
        // se non specificato, va avanti
        if (forward || !board.isDirectionChoiceEnabled()) {
            moveForwardBy(player, lastRoll);
        } else {
            moveBackwardBy(player, lastRoll);
        }
    }
 
    public void moveForwardBy(Player player, int steps) {
        int remaining = steps;
 
        while (remaining > 0) {
            int currentPosition = player.getPosition();
            Square currentSquare = board.getSquares().get(currentPosition);
            List<Integer> options = currentSquare.getNextPositions();
 
            if (options.size() > 1) {
                // bivio: si ferma qui, in attesa della scelta del giocatore; si prosegue dopo la scelta
                requestBivioChoice(player, options, remaining, true, false);
                return;
            }
 
            int nextPosition = options.get(0);
 
            if (nextPosition > board.getFinishPosition()) {
                // rimbalzo: il passo si riflette, si muove indietro di 1 invece che avanti,
                // senza rispettare il checkpoint (stesso comportamento aritmetico di prima)
                player.setPosition(currentPosition - 1);
                moveBackwardByInternal(player, remaining - 1, false);
                return;
            }
 
            player.setPosition(nextPosition);
            remaining--;
 
            // fermo obbligatorio: se la regola e' attiva e la casella appena raggiunta e' un checkpoint,
            // ci si ferma li' anche se restano passi (scartati), stesso principio del pavimento a 0 sotto
            Square nextSquare = board.getSquares().get(nextPosition);
            if (board.isMandatoryCheckpointEnabled() && nextSquare.isMandatoryStop()) {
                remaining = 0;
            }
        }
 
        doLandOnCurrentSquare(player);
    }
 
    public void moveBackwardBy(Player player, int steps) {
        boolean respectCheckpoint = board.isMandatoryCheckpointEnabled();
        moveBackwardByInternal(player, steps, respectCheckpoint);
    }
 
    private void moveBackwardByInternal(Player player, int steps, boolean respectCheckpoint) {
        int remaining = steps;
 
        while (remaining > 0) {
            // pavimento fisso a 0, sempre attivo indipendentemente dalla regola del checkpoint
            if (player.getPosition() <= 0) {
                break;
            }
            if (respectCheckpoint && player.getPosition() <= player.getCheckpoint()) {
                break;
            }
 
            int currentPosition = player.getPosition();
            Square currentSquare = board.getSquares().get(currentPosition);
            List<Integer> options = currentSquare.getPreviousPositions();
 
            if (options.size() > 1) {
                requestBivioChoice(player, options, remaining, false, respectCheckpoint);
                return;
            }
 
            int nextPosition = options.get(0);
            player.setPosition(nextPosition);
            remaining--;
        }
 
        doLandOnCurrentSquare(player);
    }
 
    private void requestBivioChoice(Player player, List<Integer> options, int remainingSteps,
            boolean forward, boolean respectCheckpoint) {
        awaitingPlayer = player;
        awaitingOptions = options;
        awaitingRemainingSteps = remainingSteps;
        awaitingForward = forward;
        awaitingRespectCheckpoint = respectCheckpoint;
        awaitingContinueAfterChoice = true;
    }
 
    public void requestChoice(Player player, List<Integer> options) {
        // usato dalle caselle che si attivano solo su atterraggio esatto (es. Portale):
        // nessuna prosecuzione dopo la scelta, "niente di niente" come il teletrasporto normale
        awaitingPlayer = player;
        awaitingOptions = options;
        awaitingRemainingSteps = 0;
        awaitingForward = true;
        awaitingRespectCheckpoint = false;
        awaitingContinueAfterChoice = false;
    }
 
    public boolean isAwaitingChoice() {
        return awaitingOptions != null;
    }
 
    public List<Integer> getChoiceOptions() {
        return awaitingOptions;
    }
 
    public void chooseDestination(Player player, int chosenPosition) {
        if (awaitingOptions == null) {
            throw new IllegalStateException("No choice is pending");
        }
        if (player != awaitingPlayer) {
            throw new IllegalArgumentException("This player has no pending choice");
        }
        if (!awaitingOptions.contains(chosenPosition)) {
            throw new IllegalArgumentException("Chosen position is not among the available options");
        }
 
        Player waitingPlayer = awaitingPlayer;
        int remainingSteps = awaitingRemainingSteps;
        boolean forward = awaitingForward;
        boolean respectCheckpoint = awaitingRespectCheckpoint;
        boolean continueAfterChoice = awaitingContinueAfterChoice;
 
        awaitingPlayer = null;
        awaitingOptions = null;
 
        waitingPlayer.setPosition(chosenPosition);
 
        if (!continueAfterChoice) {
            return;
        }
 
        int nextRemaining = remainingSteps - 1;
        if (forward) {
            moveForwardBy(waitingPlayer, nextRemaining);
        } else {
            moveBackwardByInternal(waitingPlayer, nextRemaining, respectCheckpoint);
        }
    }
 
    public void landOnCurrentSquare(Player player) {
        doLandOnCurrentSquare(player);
    }
 
    private void doLandOnCurrentSquare(Player player) {
        if (player.getPosition() == board.getFinishPosition()) {
            winner = player;
            return;
        }
 
        Square square = board.getSquares().get(player.getPosition());
        square.applyEffect(player, this);
    }
 
    public void teleportTo(Player player, int position) {
        player.setPosition(position);
    }
 
    public void resetToStart(Player player) {
        player.setPosition(player.getCheckpoint());
    }
 
    public void updateCheckpoint(Player player) {
        player.setCheckpoint(player.getPosition());
    }
 
    public Board getBoard() {
        return board;
    }
 
    public Player getWinner() {
        return winner;
    }
 
    public int getLastRoll() {
        return lastRoll;
    }
 
    public Player getLastPlayerToMove() {
        return lastPlayerToMove;
    }
 
    public List<Player> getPlayers() {
        return players;
    }
}
 
