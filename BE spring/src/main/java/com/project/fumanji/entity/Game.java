package com.project.fumanji.entity;
 
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
 
public class Game {

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
    private boolean awaitingForward;
    private boolean awaitingRespectCheckpoint;
    private boolean awaitingContinueAfterChoice;
    private final List<Integer> movementTrace = new ArrayList<>();
    private final Set<Integer> activatedPositions = new HashSet<>();
    private boolean chainInProgress;
 
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
        // conta quanti steps sono rimasti nel movimento, infatti inizia da steps pieno e lo scaliamo
        int remaining = steps;
 
        // ciclo per muovere il giocatore fino alla fine degli steps
        while (remaining > 0) {
            // prende il giocatore, la sua attuale posizione e le opzioni che ha per avanzare da lì
            int currentPosition = player.getPosition();
            Square currentSquare = board.getSquares().get(currentPosition);
            List<Integer> options = currentSquare.getNextPositions();
 
            // se ha più di una opzione...
            if (options.size() > 1) {
                // ...bivio: si ferma qui, in attesa della scelta del giocatore; si prosegue dopo la scelta
                // mando anche le opzioni, gli steps rimasti, si sta muovendo in avanti e non è MandatoryStop
                requestBivioChoice(player, options, remaining, true, false);
                // e returna al chiamante perchè dopo richiamerà di nuovo il metodo con gli steps restanti
                return;
            }

            // prende la prima (e ora unica) casella successiva
            int nextPosition = options.get(0);
 
            // controllo del rimbalzo oltre l'arrivo
            if (nextPosition > board.getFinishPosition()) {
                // rimbalzo: il passo si riflette, si muove indietro di 1 invece che avanti,
                // senza rispettare il checkpoint (stesso comportamento aritmetico di prima)
                movePlayerTo(player, currentPosition - 1);
                // muove all'indietro dei rimanenti steps, ma - 1 che è l'arrivo, clean
                moveBackwardByInternal(player, remaining - 1, false);
                return;
            }
 
            // posiziona il player nella casella successiva e scala 1 passo dai rimanenti
            movePlayerTo(player, nextPosition);
            remaining--;
 
            // fermo obbligatorio: se la regola e' attiva e la casella appena raggiunta e' un checkpoint,
            // ci si ferma li' anche se restano passi (scartati), stesso principio del pavimento a 0 sotto
            Square nextSquare = board.getSquares().get(nextPosition);
            // controlla che ci sia la regola attiva nella board
            if (board.isMandatoryCheckpointEnabled() && nextSquare.isMandatoryStop()) {
                remaining = 0;
            }
        }
 
        // attiva gli effetti della casella su cui è finito il giocatore
        doLandOnCurrentSquare(player);
    }

    /**
     * metodo per muovere indietro il giocatore di tot steps
     * @param player
     * @param steps
     */
    public void moveBackwardBy(Player player, int steps) {
        // controlla se c'è il blocco al cp nelle board rules...
        boolean respectCheckpoint = board.isMandatoryCheckpointEnabled();
        // ... e muove indietro di steps (in caso clamp al cp)
        moveBackwardByInternal(player, steps, respectCheckpoint);
    }
 
    /**
     * metodo per muovere il giocatore indietro
     * @param player
     * @param steps
     * @param respectCheckpoint
     */
    private void moveBackwardByInternal(Player player, int steps, boolean respectCheckpoint) {
        // steps rimasti
        int remaining = steps;

        // ciclo per muovere finchè rimangono steps
        while (remaining > 0) {
            // pavimento fisso a 0, sempre attivo indipendentemente dalla regola del checkpoint
            if (player.getPosition() <= 0) {
                break;
            }

            // prende la posizione del giocatore, la casella su cui è e tutte le caselle precedenti
            int currentPosition = player.getPosition();
            Square currentSquare = board.getSquares().get(currentPosition);
            List<Integer> options = currentSquare.getPreviousPositions();

            // se ce n'è più di 1, è un bivio
            if (options.size() > 1) {
                requestBivioChoice(player, options, remaining, false, respectCheckpoint);
                return;
            }

            // prende la prima (e ora unica) casella tra le possibilità, muove il giocatore e toglie uno step
            int nextPosition = options.get(0);
            movePlayerTo(player, nextPosition);
            remaining--;

            // fermo obbligatorio simmetrico al forward: casella appena raggiunta, non quella di partenza
            Square nextSquare = board.getSquares().get(nextPosition);
            // se c'è la regola, azzera tutti gli steps rimanenti, costringendo il giocatore a fermarsi
            if (respectCheckpoint && nextSquare.isMandatoryStop()) {
                remaining = 0;
            }
        }

        // attiva gli effetti della casella su cui si trova il player
        doLandOnCurrentSquare(player);
    }
 
    /**
     * metodo per richiedere la scelta del giocatore al bivio
     * @param player
     * @param options
     * @param remainingSteps
     * @param forward
     * @param respectCheckpoint // TODO capisci bene
     */
    private void requestBivioChoice(Player player, List<Integer> options, int remainingSteps,
            boolean forward, boolean respectCheckpoint) {
        awaitingPlayer = player;
        awaitingOptions = options;
        awaitingRemainingSteps = remainingSteps;
        awaitingForward = forward;
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
        awaitingForward = true;
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
        boolean forward = awaitingForward;
        boolean respectCheckpoint = awaitingRespectCheckpoint;
        boolean continueAfterChoice = awaitingContinueAfterChoice;
        
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
        int nextRemaining = remainingSteps - 1;
        if (forward) {
            moveForwardBy(waitingPlayer, nextRemaining);
        } else {
            moveBackwardByInternal(waitingPlayer, nextRemaining, respectCheckpoint);
        }
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
       movePlayerTo(player, position);
    }
    
    /**
     * metodo che riporta il giocatore all'ultimo cp raggiunto (oppure all'inizio)
     * @param player
     */
    public void resetToStart(Player player) {
        player.setPosition(player.getCheckpoint());
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
        player.setPosition(position);
        movementTrace.add(position);
    }
    
    /**
     * metodo per pulire la cronologia di spostamenti
     */
    public void clearMovementTrace() {
        movementTrace.clear();
    }

    // helper

    public void addSnack(Player player, int amount) {
        player.setSnack(Math.max(0, player.getSnack() + amount));
    }

    public void addDrink(Player player, int amount) {
        player.setDrink(Math.max(0, player.getDrink() + amount));
    }
    
    // getter

    public List<Integer> getMovementTrace() {
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