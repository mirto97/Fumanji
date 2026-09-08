package entity;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

public class Game {

    // TODO ristudiatelo tutto e commentalo, in caso ripassalo su chatgpt

    // final perchè le settiamo nel costruttore e non cambieranno mai più
    private final Board board;
    private final List<Player> players;
    private final Dice dice;
    private int currentPlayerIndex;     // è l'indice (posizione nell'array/lista, da 0) del giocatore di cui è il turno in players
    private Player winner;
    private int lastRoll;
    private Player lastPlayerToMove;
    // per creare la coda di azioni
    private final Deque<Runnable> pendingActions = new ArrayDeque<>();
    private boolean processingQueue;

    // perché serve processingQueue (il booleano). Ripercorriamo il DeepChainTest di prima passo-passo:

    // Chiami game.moveForwardBy(player, 1) dall'esterno.
    // Questo fa enqueue(() -> doMoveForwardBy(player, 1)) — la coda ora contiene [doMoveForwardBy(1)].
    // Chiama processQueue(). processingQueue è false → lo metto a true → entro nel while.
    // Iterazione 1: tolgo doMoveForwardBy(1) dalla coda e lo eseguo → sposta il giocatore da 0 a 1, poi accoda doLandOnCurrentSquare. Coda: [doLandOnCurrentSquare].
    // Iterazione 2: tolgo ed eseguo doLandOnCurrentSquare → trova la MoveForwardSquare in posizione 1, chiama square.applyEffect(player, game), 
    // che a sua volta chiama di nuovo game.moveForwardBy(player, 2) (il metodo pubblico) → questo fa enqueue(doMoveForwardBy(2)) e poi prova a chiamare processQueue() — ma processingQueue è già true! 
    // Quindi quella chiamata non fa nulla, ritorna subito. Non parte un secondo ciclo, non si accumula un'altra chiamata sulla pila.
    // Il controllo torna su, fino al while di prima (quello dell'iterazione 4-5, ancora vivo), che trova la coda non vuota e continua da solo con doMoveForwardBy(2).
    // Si ripete finché la coda non si svuota (quando ObligationSquare stampa il messaggio e non accoda più nulla).


    private Player awaitingPlayer;
    private int awaitingRemainingSteps;
    private boolean awaitingForward;
    private boolean awaitingRespectCheckpoint;
    private List<Integer> awaitingOptions;
    private Player awaitingPortalPlayer;
    private List<Integer> awaitingPortalOptions;



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

    // sono tre pezzi diversi che si combinano:

    // Runnable è un'interfaccia di Java con un solo metodo: void run(). Rappresenta "un pezzetto di codice da eseguire dopo", non subito. 
    // Quando scrivi () -> doMoveForwardBy(player, steps), non stai chiamando doMoveForwardBy — stai creando un oggetto che, se e quando qualcuno chiamerà il suo .run(), eseguirà quella chiamata. Fino ad allora è "congelato", inerte.

    // Deque (si legge "deck", double-ended queue) è un'interfaccia che rappresenta una lista da cui puoi aggiungere/togliere elementi sia in testa che in coda. 
    // Noi la usiamo solo come una coda semplice (FIFO — first in, first out, il primo che entra è il primo che esce): addLast(...) mette in fondo, pollFirst() toglie e restituisce quello davanti. Stessa logica della fila alle poste.

    // ArrayDeque è semplicemente l'implementazione concreta di Deque (basata su un array che si ridimensiona da solo) — è la scelta standard quando serve una coda in Java, più efficiente di LinkedList per questo uso.
    
    // Messi insieme: pendingActions è una lista di "cosette da fare dopo", ognuna è un Runnable (un pezzo di logica non ancora eseguito).

    private void enqueue(Runnable action) {
        pendingActions.addLast(action);
    }

    private void processQueue() {
        if (processingQueue) {
            return;
        }
        processingQueue = true;
        try {
            while (!pendingActions.isEmpty()) {
                pendingActions.pollFirst().run();
            }
        } finally {
            processingQueue = false;
        }
    }

    public void moveForwardBy(Player player, int steps) {
        enqueue(() -> stepForward(player, steps));
        processQueue();
    }

    private void stepForward(Player player, int remainingSteps) {
        if (remainingSteps == 0) {
            enqueue(() -> doLandOnCurrentSquare(player));
            return;
        }

        int currentPosition = player.getPosition();
        Square currentSquare = board.getSquares().get(currentPosition);
        List<Integer> options = currentSquare.getNextPositions();

        if (options.size() > 1) {
            awaitingPlayer = player;
            awaitingRemainingSteps = remainingSteps;
            awaitingForward = true;
            awaitingOptions = options;
            return;
        }

        int nextPosition = options.get(0);

        if (nextPosition > board.getFinishPosition()) {
            // rimbalzo: il passo si riflette, si muove indietro di 1 invece che avanti,
            // senza rispettare il checkpoint (stesso comportamento aritmetico di prima)
            player.setPosition(currentPosition - 1);
            int nextRemaining = remainingSteps - 1;
            enqueue(() -> stepBackwardInternal(player, nextRemaining, false));
            return;
        }

        player.setPosition(nextPosition);
        
        // fermo obbligatorio: se la regola e' attiva e la casella appena raggiunta e' un checkpoint,
        // ci si ferma li' anche se restano passi (scartati), stesso principio del clamp esistente
        Square nextSquare = board.getSquares().get(nextPosition);
        if (board.isMandatoryCheckpointEnabled() && nextSquare.isMandatoryStop()) {
            enqueue(() -> doLandOnCurrentSquare(player));
            return;
    }

    int nextRemaining = remainingSteps - 1;
    enqueue(() -> stepForward(player, nextRemaining));
    }

    public void moveBackwardBy(Player player, int steps) {
        boolean respectCheckpoint = board.isMandatoryCheckpointEnabled();
        enqueue(() -> stepBackwardInternal(player, steps, respectCheckpoint));
        processQueue();
    }

    private void stepBackwardInternal(Player player, int remainingSteps, boolean respectCheckpoint) {
        if (remainingSteps == 0) {
            enqueue(() -> doLandOnCurrentSquare(player));
            return;
        }
        // pavimento fisso a 0, sempre attivo indipendentemente dalla regola del checkpoint
        if (player.getPosition() <= 0) {
            enqueue(() -> doLandOnCurrentSquare(player));
            return;
        }
        if (respectCheckpoint && player.getPosition() <= player.getCheckpoint()) {
            enqueue(() -> doLandOnCurrentSquare(player));
            return;
        }

        int currentPosition = player.getPosition();
        Square currentSquare = board.getSquares().get(currentPosition);
        List<Integer> options = currentSquare.getPreviousPositions();

        if (options.size() > 1) {
            awaitingPlayer = player;
            awaitingRemainingSteps = remainingSteps;
            awaitingForward = false;
            awaitingRespectCheckpoint = respectCheckpoint;
            awaitingOptions = options;
            return;
        }

        int nextPosition = options.get(0);
        player.setPosition(nextPosition);
        int nextRemaining = remainingSteps - 1;
        enqueue(() -> stepBackwardInternal(player, nextRemaining, respectCheckpoint));
    }

    public void landOnCurrentSquare(Player player) {
        enqueue(() -> doLandOnCurrentSquare(player));
        processQueue();
    }

    private void doLandOnCurrentSquare(Player player) {
        if (player.getPosition() == board.getFinishPosition()) {
            winner = player;
            return;
        }

        Square square = board.getSquares().get(player.getPosition());
        square.applyEffect(player, this);
    }



    public boolean isAwaitingBivioChoice() {
        return awaitingOptions != null;
    }

    public List<Integer> getBivioOptions() {
        return awaitingOptions;
    }

    public void chooseBivioPath(Player player, int chosenPosition) {
        if (awaitingOptions == null) {
            throw new IllegalStateException("No bivio choice is pending");
        }
        if (player != awaitingPlayer) {
            throw new IllegalArgumentException("This player has no pending bivio choice");
        }
        if (!awaitingOptions.contains(chosenPosition)) {
            throw new IllegalArgumentException("Chosen position is not among the available options");
        }

        Player waitingPlayer = awaitingPlayer;
        int remainingSteps = awaitingRemainingSteps;
        boolean forward = awaitingForward;
        boolean respectCheckpoint = awaitingRespectCheckpoint;

        awaitingPlayer = null;
        awaitingOptions = null;

        waitingPlayer.setPosition(chosenPosition);
        int nextRemaining = remainingSteps - 1;

        if (forward) {
            enqueue(() -> stepForward(waitingPlayer, nextRemaining));
        } else {
            enqueue(() -> stepBackwardInternal(waitingPlayer, nextRemaining, respectCheckpoint));
        }
        processQueue();
    }


    public void requestPortalChoice(Player player, List<Integer> options) {
        awaitingPortalPlayer = player;
        awaitingPortalOptions = options;
    }

    public boolean isAwaitingPortalChoice() {
        return awaitingPortalOptions != null;
    }

    public List<Integer> getPortalOptions() {
        return awaitingPortalOptions;
    }

    public void choosePortalDestination(Player player, int chosenPosition) {
        if (awaitingPortalOptions == null) {
            throw new IllegalStateException("No portal choice is pending");
        }
        if (player != awaitingPortalPlayer) {
            throw new IllegalArgumentException("This player has no pending portal choice");
        }
        if (!awaitingPortalOptions.contains(chosenPosition)) {
            throw new IllegalArgumentException("Chosen position is not among the available options");
        }

        awaitingPortalPlayer = null;
        awaitingPortalOptions = null;

        teleportTo(player, chosenPosition);
    }


    /**
     * (va bene solo per board senza bivi e senza portali) metodo per giocare 1 turno di 1 giocatore 
     */
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
    
    
    
    // vecchio codice
    
    // /**
    //  * metodo solo per muovere di lastroll steps nella direzione scelta all'input
    //  * @param player
    //  * @param forward
    //  */
    // public void moveByChosenDirection(Player player, boolean forward) {
    //     // se non specificato, va avanti 
    //     if (forward || !board.isDirectionChoiceEnabled()) {
    //         moveForwardBy(player, lastRoll);
    //     } else {
    //         moveBackwardBy(player, lastRoll);
    //     }
    // }

    // /**
    //  * metodo che muove avanti il giocatore di STEPS in input
    //  * @param player
    //  * @param steps
    //  */
    // public void moveForwardBy(Player player, int steps) {

    //     // controllo in caso di rimbalzo overshoot sull'arrivo
    //     int newPosition = player.getPosition() + steps;
    //     int finishPosition = board.getFinishPosition();
    //     if (newPosition > finishPosition) {
    //         int overshoot = newPosition - finishPosition;
    //         newPosition = finishPosition - overshoot;
    //     }

    //     player.setPosition(newPosition);
    //     landOnCurrentSquare(player);
    // }

    // /**
    //  *  " indietro, con clamp al checkpoint del giocatore
    //  * @param player
    //  * @param steps
    //  */
    // public void moveBackwardBy(Player player, int steps) {
    //     // per controllare che non vada a meno di 0, la partenza
    //     // Math.max returna il più grande tra i 2 valori in input { (a >= b) ? a : b; }      
    //     int newPosition = Math.max(player.getCheckpoint(), player.getPosition() - steps);
    //     player.setPosition(newPosition);
    //     landOnCurrentSquare(player);
    // }

    // /**
    //  * metodo per applicare l'effetto delle caselle sul giocatore
    //  * @param player
    //  */
    // public void landOnCurrentSquare(Player player) {
    //     // controllo se il giocatore è arrivato al traguardo
    //     if (player.getPosition() == board.getFinishPosition()) {
    //         winner = player;
    //         return;
    //     }

    //     Square square = board.getSquares().get(player.getPosition());
    //     square.applyEffect(player, this);
    // }