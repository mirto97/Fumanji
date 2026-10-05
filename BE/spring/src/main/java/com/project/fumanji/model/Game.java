package com.project.fumanji.model;
 
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
 
public class Game {

    private static final String WASTED_TURN_AT_CHECKPOINT = "Bravo {player}, hai sprecato un turno: non puoi andare indietro oltre il checkpoint";
    private static final String WASTED_TURN_AT_START = "Bravo {player}, hai sprecato un turno: non puoi andare indietro oltre la partenza";
    private static final String STOPPED_AT_CHECKPOINT = "{player} non può andare indietro oltre il checkpoint: il movimento si ferma qui";
    private static final int NO_POSITION = -1;

    private final Board board;
    private final List<Player> players;
    private final Dice dice;
    private int currentPlayerIndex;
    private Player winner;
    private int lastRoll;
    private Player lastPlayerToMove;    // ultimo giocatore che si è mosso
    private Player awaitingPlayer;  // salva il giocatore che sta aspettando di finire il turno
    private List<Integer> awaitingOptions;  // le opzioni che ha il giocatore in attesa per proseguire
    private int awaitingRemainingSteps;
    private boolean awaitingRespectCheckpoint;
    private boolean awaitingContinueAfterChoice;
    private final List<Movement> movementTrace = new ArrayList<>();
    private final Set<Integer> activatedPositions = new HashSet<>();
    private boolean chainInProgress;
    private TurnPhase turnPhase = TurnPhase.WAITING_ROLL;   // dice in quale fase del turno stiamo
    private final List<String> turnMessages = new ArrayList<>();

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
        this.players = List.copyOf(players);
        this.dice = dice;
        this.currentPlayerIndex = 0;
        this.winner = null;
    }

    /**
     * mi sa metodo solo per quando è senza bivi
     */
    public void playTurn() {
        if (winner != null) {
            return;
        }
 
        // prende il numero d'indice del giocatore in turno e chiama il metodo per tirare e muovere
        Player currentPlayer = players.get(currentPlayerIndex);
        rollAndMove(currentPlayer);
 
        if (winner == null) {
            currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
        }
    }

    // TODO commentare e studiareeeeee
    
    // synchronized: l'ho messo sui tre metodi di turno perché senza, un doppio click che genera due richieste contemporanee passerebbe entrambi i controlli di fase

    public synchronized int performRoll() {
        checkTurnAction(TurnPhase.WAITING_ROLL);
        turnMessages.clear();
        movementTrace.clear();
        int rollValue = roll(getCurrentPlayer());
        turnPhase = TurnPhase.WAITING_DIRECTION;
        return rollValue;
    }

    public synchronized void performMove(boolean forward) {
        checkTurnAction(TurnPhase.WAITING_DIRECTION);
        moveByChosenDirection(getCurrentPlayer(), forward);
        resolveTurnPhase();
    }

    public synchronized void performChoice(int chosenPosition) {
        checkTurnAction(TurnPhase.WAITING_CHOICE);
        movementTrace.clear();
        chooseDestination(getCurrentPlayer(), chosenPosition);
        resolveTurnPhase();
    }

    private void checkTurnAction(TurnPhase expectedPhase) {
        if (winner != null) {
            throw new IllegalStateException("Game is already over");
        }
        if (turnPhase != expectedPhase) {
            throw new IllegalStateException(
                    "Action not allowed in phase " + turnPhase + " (expected " + expectedPhase + ")");
        }
    }

    private void resolveTurnPhase() {
        if (winner != null) {
            return;
        }
        if (isAwaitingChoice()) {
            turnPhase = TurnPhase.WAITING_CHOICE;
            return;
        }
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
        turnPhase = TurnPhase.WAITING_ROLL;
    }




 
    /**
     * metodo per tirare il dado e controllare il valore
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
     * metodo per tirare e muovere insieme (nessun tipo di scelta)
     * @param player
     */
    public void rollAndMove(Player player) {
        int rollValue = roll(player);
        moveForwardBy(player, rollValue);
    }
 
    /**
     * metodo per muovere il giocatore nella direzione che scelta
     * @param player
     * @param forward
     */
    public void moveByChosenDirection(Player player, boolean forward) {
        // se non specificato, va avanti
        if (forward || !board.isDirectionChoiceEnabled()) {
            moveForwardBy(player, lastRoll);
        } else if (isBackwardBlocked(player)) {
            String template = player.getPosition() <= 0 ? WASTED_TURN_AT_START : WASTED_TURN_AT_CHECKPOINT;
            addMessage(template.replace("{player}", player.getName()));
        } else {
            moveBackwardBy(player, lastRoll);
        }
    }
 
    /**
     * metodo per muovere in avanti il giocatore di tot steps
     * @param player
     * @param steps
     */
    public void moveForwardBy(Player player, int steps) {
        walk(player, steps, true, board.isMandatoryCheckpointEnabled(), NO_POSITION);
    }

    /**
     * metodo per muovere indietro il giocatore di tot steps
     * @param player
     * @param steps
     */
    public void moveBackwardBy(Player player, int steps) {
        walk(player, steps, false, board.isMandatoryCheckpointEnabled(), NO_POSITION);
    }

    private void walk(Player player, int steps, boolean forward, boolean respectCheckpoint, int cameFrom) {
        int remaining = steps;
        int previousPosition = cameFrom;

        while (remaining > 0) {
            List<Integer> exits = availableExits(player, forward, previousPosition);

            if (exits.isEmpty()) {
                break;
            }
            if (exits.size() > 1) {
                requestBivioChoice(player, exits, remaining, respectCheckpoint);
                return;
            }

            int currentPosition = player.getPosition();
            int targetPosition = exits.get(0);
            boolean stepForward = board.getNextPositions(currentPosition).contains(targetPosition);

            if (stepForward && targetPosition > board.getFinishPosition()) {
                // rimbalzo: il passo si riflette, si muove indietro di 1 invece che avanti,
                // senza rispettare il checkpoint (stesso comportamento aritmetico di sempre)
                movePlayerTo(player, currentPosition - 1);
                walk(player, remaining - 1, false, false, currentPosition);
                return;
            }

            movePlayerTo(player, targetPosition);
            previousPosition = currentPosition;
            remaining--;

            boolean respect = stepForward ? board.isMandatoryCheckpointEnabled() : respectCheckpoint;
            if (respect && board.getSquares().get(targetPosition).isMandatoryStop()) {
                if (!stepForward && remaining > 0) {
                    addMessage(STOPPED_AT_CHECKPOINT.replace("{player}", player.getName()));
                }
                remaining = 0;
            }
        }

        doLandOnCurrentSquare(player);
    }

    private List<Integer> availableExits(Player player, boolean forward, int cameFrom) {
        int position = player.getPosition();
        List<Integer> next = board.getNextPositions(position);
        List<Integer> previous = board.getPreviousPositions(position);
        boolean backwardBlocked = isBackwardBlocked(player);

        Stream<Integer> candidates = cameFrom == NO_POSITION
                ? (forward ? next : previous).stream()
                : Stream.concat(next.stream(), previous.stream());

        return candidates
                .filter(candidate -> candidate != cameFrom)
                .filter(candidate -> !(backwardBlocked && previous.contains(candidate) && !next.contains(candidate)))
                .distinct()
                .sorted()
                .toList();
    }
 
    /**
     * metodo per richiedere la scelta del giocatore al bivio
     * @param player
     * @param options
     * @param remainingSteps
     * @param forward
     * @param respectCheckpoint // TODO capisci bene
     */
    private void requestBivioChoice(Player player, List<Integer> options, int remainingSteps, boolean respectCheckpoint) {
        awaitingPlayer = player;
        awaitingOptions = options;
        awaitingRemainingSteps = remainingSteps;
        awaitingRespectCheckpoint = respectCheckpoint;
        awaitingContinueAfterChoice = true;
    }
 
    /**
     * metodo per richiedere la scelta del giocatore quando ha finito gli steps
     * @param player
     * @param options
     */
    public void requestChoice(Player player, List<Integer> options) {
        // usato dalle caselle che si attivano solo su atterraggio esatto (es. Portale):
        // nessuna prosecuzione dopo la scelta, "niente di niente" come il teletrasporto normale
        awaitingPlayer = player;
        awaitingOptions = options;
        awaitingRemainingSteps = 0;
        awaitingRespectCheckpoint = false;
        awaitingContinueAfterChoice = false;
    }

    
    /**
     * metodo che restituisce true se c'è la board rules sulla scelta del senso di marcia
     * @return
     */
    public boolean isAwaitingChoice() {
        return awaitingOptions != null;
    }
 
    /**
     * metodo che restituisce le opzioni per quando il giocatore deve scegliere in quale parte avventurarsi
     * @return
     */
    public List<Integer> getChoiceOptions() {
        return awaitingOptions;
    }
 
    /**
     * metodo per far scegliere la posizione al giocatore~
     * @param player
     * @param chosenPosition
    */
    public void chooseDestination(Player player, int chosenPosition) {
        // controlla che ci siano effettivamente le opzioni
        if (awaitingOptions == null) {
            throw new IllegalStateException("No choice is pending");
        }
        // controlla che il giocatore sia quello che sta aspettando di scegliere
        if (player != awaitingPlayer) {
            throw new IllegalArgumentException("This player has no pending choice");
        }
        // controlla che tra le opzioni ci sia quella scelta dal giocatore
        if (!awaitingOptions.contains(chosenPosition)) {
            throw new IllegalArgumentException("Chosen position is not among the available options");
        }
        
        // prende tutte le informazioni per la scelta
        Player waitingPlayer = awaitingPlayer;
        int remainingSteps = awaitingRemainingSteps;
        boolean respectCheckpoint = awaitingRespectCheckpoint;
        boolean continueAfterChoice = awaitingContinueAfterChoice;
        int choicePosition = waitingPlayer.getPosition();

        // resetta le opzioni e il giocatore in attesa
        awaitingPlayer = null;
        awaitingOptions = null;
        
        // sposta il giocatore nella posizione scelta
        movePlayerTo(player, chosenPosition);
 
        // controlla se può continuare a proseguire
        if (!continueAfterChoice) {
            return;
        }
 
        // prosegue il movimento con gli steps rimanenti nella direzione scelta
        boolean forward = board.getNextPositions(choicePosition).contains(chosenPosition);
        walk(waitingPlayer, remainingSteps - 1, forward, respectCheckpoint, choicePosition);
    }
 
    /**
     * metodo che richiama il metodo sotto, e basta (sinceramente non ho capito perchè...)  
     * @param player
    */
    public void landOnCurrentSquare(Player player) {
       doLandOnCurrentSquare(player);
    }
    
    /**
     * metodo che attiva gli effetti della casella su cui si finisce
     * @param player
     */
    private void doLandOnCurrentSquare(Player player) {
        // quando la catena inizia, chainInProgress = false, isChainStart = true, poi si invertono...
        boolean isChainStart = !chainInProgress;
        // quindi se è appena iniziata...
        if (isChainStart) {
            // diciamo che ora è in progress e svuotiamo le caselle che sono state attivate
            chainInProgress = true;
            activatedPositions.clear();
        }

        // proviamo ad attivare gli effetti ma senza catch, che non ci serve
        try {
            // controlliamo se ha vinto
            if (player.getPosition() == board.getFinishPosition()) {
                winner = player;
                return;
            }

            // prendiamo la posizione del giocatore e controlliamo che quella casella non è già stata attivata
            int position = player.getPosition();
            if (!activatedPositions.add(position)) {
                // se è gia' attivata in questo turno di movimento: niente effetto, si comporta da casella base, quindi return
                return;
            }

            // prendiamo la casella nella posizione del giocatore e attiviamo l'effetto
            Square square = board.getSquares().get(position);
            square.applyEffect(player, this);
        } finally {
            if (isChainStart) {
                chainInProgress = false;
            }
            // pulisce il booleano anche nei casi di return

            // Senza finally dovrei ripetere chainInProgress = false prima di ogni return — facile dimenticarne uno in futuro, lasciando il flag bloccato a true per sempre (il guardrail smetterebbe di azzerare activatedPositions ad ogni nuovo turno). 
            // C'è anche il caso di un'eccezione che scappa da square.applyEffect(...): senza finally il flag resterebbe true per sempre anche lì. Col finally il reset è garantito qualunque sia la via d'uscita.
        }
    }
    
    /**
     * metodo che sposta il giocatore nella casella destinataria
     * @param player
     * @param position
    */
   public void teleportTo(Player player, int position) {
       movePlayerTo(player, position, MovementType.JUMP, null);
    }
    
    /**
     * metodo che riporta il giocatore all'ultimo cp raggiunto (oppure all'inizio)
     * @param player
     */
    public void resetToStart(Player player, String message) {
        movePlayerTo(player, player.getCheckpoint(), MovementType.RESET, message);
    }
    
    /**
     * metodo che salva l'ultimo cp raggiunto dal giocatore
     * @param player
    */
   public void updateCheckpoint(Player player) {
       player.setCheckpoint(player.getPosition());
    }
    
    /**
     * metodo che sposta il giocatore e tiene traccia del movimento
     * @param player
     * @param position
     */
    private void movePlayerTo(Player player, int position) {
        movePlayerTo(player, position, MovementType.STEP, null);
    }
    private void movePlayerTo(Player player, int position, MovementType type, String message) {
        player.setPosition(position);
        movementTrace.add(new Movement(position, type, message));
    }
    
    /**
     * metodo per pulire la cronologia di spostamenti
     */
    public void clearMovementTrace() {
        movementTrace.clear();
    }

    private boolean isBackwardBlocked(Player player) {
        // pavimento fisso a 0, sempre attivo indipendentemente dalla regola del cp
        if (player.getPosition() <= 0) {
            return true;
        }
        // se c'è la regola che non si può andare indietro oltre al cp
        // e se il giocatore ha superato il cp, non può andare oltre
        return !board.isBackwardAllowedAfterCheckpoint() && player.getPosition() <= player.getCheckpoint();
    }

    // helper

    public void addSnack(Player player, int amount) {
        player.setSnack(Math.max(0, player.getSnack() + amount));
    }

    public void addDrink(Player player, int amount) {
        player.setDrink(Math.max(0, player.getDrink() + amount));
    }
    
    // getter

    public int getChoiceRemainingSteps() {
        return awaitingRemainingSteps;
    }
    
    public synchronized void addMessage(String message) {
        turnMessages.add(message);
    }

    public synchronized List<String> getTurnMessages() {
        return List.copyOf(turnMessages);
    }

    public Player getCurrentPlayer() {
        return players.get(currentPlayerIndex);
    }

    public TurnPhase getTurnPhase() {
        return turnPhase;
    }

    public List<Movement> getMovementTrace() {
        return List.copyOf(movementTrace);
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