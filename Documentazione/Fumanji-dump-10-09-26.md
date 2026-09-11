# FUMANJI — Dump di Progetto

> Archivio esteso, non riassunto. Contiene lo **stato corrente** del progetto: tutto ciò che è stato deciso e il codice così com'è oggi. Non contiene passaggi intermedi superati (es. il vecchio motore a coda con `Runnable`, rimosso e sostituito). Da caricare a inizio di ogni nuova conversazione per riprendere il lavoro da qui.

---

## 1. Scopo e contesto

Sviluppo di una web application: variante del "Gioco dell'Oca" classico, con caselle speciali che attivano effetti vari (alcuni "stupefacenti"). Stack previsto: **Backend Spring + Frontend Angular**. Si è partiti dal backend, e in particolare dalla logica di dominio in **Java puro**, prima di introdurre Spring.

**Repository**: `https://github.com/mirto97/Fumanji.git` (privato)

---

## 2. Modalità di lavoro (regole di processo — vincolanti)

- **"IO SONO LA MENTE E TU SEI IL BRACCIO"**: l'utente decide, Claude scrive codice. Claude non deve pensare/decidere al posto dell'utente a meno che non venga esplicitamente richiesto. Consigli, correzioni e accorgimenti sono sempre ben accetti e vanno discussi insieme.
- Risposte **veloci e dirette**; approfondire solo se richiesto esplicitamente; **non ripetere** cose già scritte o decise.
- **Non scrivere codice finché non si è sicuri** di cosa si sta scrivendo. Se c'è un'ambiguità reale di design, si chiede prima di scrivere.
- **Non supporre o dedurre nulla** a meno che non venga detto esplicitamente; in caso di dubbio, si verifica insieme il risultato.
- **Non inventare nulla** di propria iniziativa — chiedere, piuttosto.
- Nomi di classi/metodi/variabili sempre in **inglese**. Commenti nel codice sempre in **italiano**, asciutti e in stile "codice da progetto", e solo quando esplicitamente richiesti.
- Il codice va scritto **sempre inline in chat** (mai come file scaricabile) — **eccetto il dump di fine conversazione**, che è invece sempre un file `.md` scaricabile.
- **Quando si modifica codice già scritto, non riscrivere mai la classe intera** — mostrare solo i metodi/le righe esatte cambiate, per non sovrascrivere i commenti personali che Salame aggiunge via IDE. Le classi **nuove di zecca** si possono mostrare per intero alla prima stesura. **Eccezione emersa questa sessione**: su richiesta esplicita, per un refactor che tocca quasi tutti i metodi di una classe (difficile da seguire come sequenza di frammenti), Claude può mostrare la classe intera — resta comunque un'eccezione puntuale, non il comportamento di default.
- Esiste sempre un file di progetto con **"...dump..."** nel nome, contenente lo stato corrente del progetto. Se Claude non lo trova in una nuova chat, deve segnalarlo. Ogni nuova conversazione riparte da lì. A fine conversazione, Claude produce un dump aggiornato: stato corrente, non log storico — i passaggi intermedi superati nel corso della conversazione (soluzioni poi sostituite) non vanno riportati.
- Prassi consolidata: quando possibile, Claude compila ed esegue il codice nel proprio ambiente sandbox per verificarne la correttezza **prima** di presentarlo, e ripete la regressione su tutto il comportamento già documentato dopo ogni modifica strutturale.
- Quando una richiesta rivela una portata strutturale più ampia di quanto sembrasse, Claude deve fermarsi e spiegare chiaramente l'impatto reale prima di procedere, piuttosto che minimizzare o iniziare a scrivere codice parziale.
- **Le `GameDemoN` sono checkpoint usa-e-getta**: devono funzionare solo al momento in cui vengono create, per verificare che il codice giri. Quando si riprende a modificare il motore/le caselle, non importa se una `GameDemo` precedente smette di funzionare o di compilare — l'obiettivo è arrivare alla prossima `GameDemo` funzionante, non mantenere le vecchie retrocompatibili.

---

## 3. Stack tecnologico

| Aspetto | Scelta |
|---|---|
| Linguaggio/runtime | Java 21 |
| Build tool | Maven |
| Framework backend | Spring Boot **4.1.0** — non ancora integrato, si sta completando prima il layer di dominio in Java puro |
| Database | MySQL (relazionale) |
| Stile API | REST puro (**no WebSocket**) |
| Frontend | Angular — non ancora iniziato. Quando BE e FE saranno pronti, valutare **Electron** per le fasi successive |
| Autenticazione | Servono account utente (login/registrazione). Meccanismo (JWT vs sessione) **non ancora deciso** |

---

## 4. Struttura cartelle e package

```
Fumanji/
├── BE/
│   └── model/                      (cartella organizzativa, NON fa parte del nome dei package Java)
│       ├── demo/                   (nessun package dichiarato)
│       │   ├── GameDemo2.java
│       │   └── GameDemo3.java
│       └── entity/                 (package: entity)
│           ├── Board.java
│           ├── Dice.java
│           ├── Game.java
│           ├── InteractiveSquare.java
│           ├── Player.java
│           ├── Square.java
│           └── squares/            (package: entity.squares)
│               ├── BivioSquare.java
│               ├── CheckpointSquare.java
│               ├── DrinkSquare.java
│               ├── MoveBackSquare.java
│               ├── MoveForwardSquare.java
│               ├── ObligationSquare.java
│               ├── PortalSquare.java
│               ├── RollAgainSquare.java
│               ├── SnackSquare.java
│               └── StartOverSquare.java
└── Documentazione/
```

Note:
- Package Java realmente usati: **`entity`**, **`entity.squares`**, **nessun package** per la cartella `demo`.
- `GameDemo1.java` non fa più parte del progetto (superato da `GameDemo2.java`).
- I metodi "di orchestrazione" di `Game` sono **public** per convenzione consolidata: preferire scelte di visibilità/design più ampie e future-proof, dato che il progetto crescerà molto. Eccezioni mirate quando serve vero incapsulamento (`Player.setPosition`/`setCheckpoint`, package-private).

---

## 5. Decisioni di design e logica di dominio

### Board
- Campo `finishPosition`: numero dell'ultima casella/arrivo (la lista `squares` ha `finishPosition + 1` elementi).
- Un solo tabellone fisso per ora, pensato per essere estendibile.
- `directionChoiceEnabled` (boolean, default `false`): regola "scelta di direzione" — provvisoria, si attiva a mano con `board.setDirectionChoiceEnabled(true)` in attesa del vero metodo di creazione board.
- `mandatoryCheckpointEnabled` (boolean, default `false`): regola "checkpoint obbligatorio" — stesso principio provvisorio, vedi sezione dedicata.
- Metodo dedicato per la creazione dei tabelloni: **ancora non costruito** (TODO prioritario). Nel frattempo tutte le regole/caselle si configurano a mano sull'oggetto `Board` nelle demo.

### Dado
- 1 dado per turno, valori 1-6, `ThreadLocalRandom`. Invariato.

### Movimento e vittoria — motore a loop diretto (senza coda)

**Motivazione del refactor**: il vecchio motore usava una coda di `Runnable` (`Deque<Runnable>` + `processingQueue`) per gestire pause a metà catena (necessarie per il Bivio) senza perdere lo stato tra chiamate annidate. Discutendo il refactor "caselle interattive vs autonome" è emerso un punto chiave: **nessuna casella autonoma fa mai qualcosa dopo aver scatenato un movimento a catena** (un `applyEffect` chiama il prossimo movimento e basta, non fa nient'altro dopo). Con questa proprietà garantita, un `return` in un punto qualsiasi — anche annidato — fa "srotolare" tutta la pila di chiamate senza perdere nulla, perché nessun chiamante ha lavoro in sospeso. Risultato: **la coda non serve più**, si torna a chiamate dirette (loop `while` + ricorsione semplice tra `moveForwardBy`/`moveBackwardBy` per i cambi di direzione, es. il rimbalzo).

⚠️ Nota tecnica: Java non ottimizza la tail-recursion, quindi una catena di caselle **molto** lunga potrebbe teoricamente saturare lo stack — rischio preesistente (c'era anche prima della coda), mitigabile col guardrail anti-loop già in backlog. Non urgente.

- Il tiro di dado sposta in avanti; se supera l'ultima casella, rimbalzo classico (torna indietro della quantità in eccesso). Formula: overshoot = posizione+passi − finishPosition; posizione finale = finishPosition − overshoot.
- Vittoria solo atterrando **esattamente** su `finishPosition`; partita finisce subito al primo vincitore; `playTurn()` a partita conclusa non fa nulla.
- Movimento **passo-passo** (non salto aritmetico), necessario per il Bivio e per il fermo obbligatorio sul checkpoint.
- **`moveForwardBy(player, steps)`**: loop che avanza di un passo alla volta; ad ogni passo controlla se la casella appena raggiunta impone un fermo obbligatorio (checkpoint, vedi sotto) o se la casella di partenza dello step offre più opzioni (Bivio, vedi sotto — pausa in attesa di scelta). In caso di overshoot, delega il resto dei passi a `moveBackwardByInternal` (rimbalzo, senza rispettare il checkpoint).
- **`moveBackwardBy(player, steps)`**: calcola `respectCheckpoint = board.isMandatoryCheckpointEnabled()` e delega a `moveBackwardByInternal`.
- **`moveBackwardByInternal(player, steps, respectCheckpoint)`** (privato): loop indietro. Si ferma quando: i passi finiscono, la posizione raggiunge 0 (pavimento fisso, sempre attivo), oppure — solo se `respectCheckpoint` è vero — la posizione raggiunge il checkpoint del giocatore. Gestisce anche il Bivio all'indietro (opzioni multiple in `getPreviousPositions()`).
- `RollAgainSquare` non richiede più nessuna gestione speciale: essendo il rilancio "solo un altro passo dal vivo" nella stessa logica, non c'è bisogno di precalcolare nulla.

### Checkpoint — dettagli aggiornati

- `Player.checkpoint` (int, default 0), setter package-private, clamp a 0. Aggiornamento sempre all'ultimo raggiunto (non al più avanzato mai toccato).
- `Game.updateCheckpoint(player)` → `player.setCheckpoint(player.getPosition())`.
- `Game.resetToStart(player)` → `player.setPosition(player.getCheckpoint())` (nome mantenuto, non torna più sempre a 0).
- `CheckpointSquare.applyEffect`: chiama `updateCheckpoint` poi stampa `buildMessage(player)` (ancora mockup: *"ha raggiunto il checkpoint! Quindi deve fare 3 tiri di creazione"*). Non fa catena.
- `CheckpointSquare.isMandatoryStop()` → `true` (override di `Square.isMandatoryStop()`, default `false` per tutte le altre caselle).

**Regola "checkpoint obbligatorio" (`Board.mandatoryCheckpointEnabled`)** — implementata questa sessione, **solo avanti** per ora:
- In `moveForwardBy`, dopo essersi spostati sulla casella appena raggiunta (`nextPosition`), se la regola è attiva e quella casella ha `isMandatoryStop() == true`, i passi rimanenti vengono scartati e si atterra lì immediatamente (stesso principio del clamp: fermo secco, nessun rimbalzo).
- **Punto tecnico importante**: il controllo è sulla casella **appena raggiunta**, non su quella di partenza dello step — altrimenti un giocatore che inizia il proprio turno già fermo su un checkpoint resterebbe bloccato per sempre ad ogni tentativo di mossa futura. Verificato esplicitamente con test dedicato.
- Se la regola è disattivata, o l'atterraggio è già esatto di suo (0 passi rimasti sul checkpoint), nessun cambiamento rispetto al comportamento base.

**Clamp all'indietro — ora condizionato alla stessa regola** (prima era sempre attivo, indipendente da qualsiasi flag):
- `moveBackwardBy` passa `respectCheckpoint = board.isMandatoryCheckpointEnabled()` a `moveBackwardByInternal`.
- Regola attiva → il giocatore non scende mai sotto il proprio checkpoint andando indietro (comportamento identico a prima).
- Regola disattivata → nessun clamp al checkpoint: il giocatore può scendere sotto, fino al **pavimento fisso a 0** (vedi sotto).
- Il rimbalzo di fine tabellone (`respectCheckpoint = false` sempre, passato internamente da `moveForwardBy` in caso di overshoot) **non rispetta mai il checkpoint**, comportamento invariato e voluto.

**Pavimento fisso a 0** — sempre attivo, **indipendente** dalla regola del checkpoint (aggiunto per evitare un crash reale trovato in test: senza la vecchia clamp-sempre-attiva, un `MoveBackSquare` poteva spingere la posizione sotto 0, causando `IndexOutOfBoundsException`):
```java
if (player.getPosition() <= 0) {
    break;
}
```
Protegge anche il rimbalzo come effetto collaterale positivo (prima non era garantito).

**TODO ancora aperto**: il backward per ora non ha un equivalente del "fermo su qualsiasi checkpoint incontrato" analogo al forward — solo il clamp al proprio checkpoint (ora condizionato). Non è stato richiesto di estenderlo oltre a questo.

### Direction choice (scelta di direzione) — invariato
- `Game.roll(player)`: tira il dado, aggiorna `lastRoll`/`lastPlayerToMove`, non muove nessuno.
- `Game.rollAndMove(player)`: chiama `roll` poi `moveForwardBy`.
- `Game.moveByChosenDirection(player, forward)`: usa `lastRoll`; se la board non ha la regola attiva, fallback silenzioso ad avanti (nessuna eccezione), qualunque sia `forward`.
- `playTurn()` non tocca questo meccanismo.

### Caselle interattive — interfaccia `InteractiveSquare` e meccanismo di scelta unificato

**Percorso di design**: l'obiettivo iniziale era ricategorizzare le caselle in "che chiedono qualcosa" vs "autonome" per eliminare la coda e, secondo l'idea di partenza, calcolare in anticipo tutte le destinazioni raggiungibili al lancio del dado (anche indietro). Discutendo l'approccio è emerso un problema con `RollAgainSquare` (esito non precalcolabile, essendo un nuovo tiro). La soluzione adottata, proposta e confermata insieme, è più semplice e non richiede precalcolo: **si tira, si procede passo-passo finché non si incontra una casella che chiede una scelta, ci si ferma, si chiede, e in base alla scelta si prosegue con i passi rimasti nella stessa direzione** — che è esattamente il comportamento già implementato nel motore a loop descritto sopra.

- `entity/InteractiveSquare.java`: **interfaccia marcatore, senza metodi**. Implementata da `BivioSquare` e `PortalSquare`. Serve solo a categorizzare (utile in futuro per far sapere al frontend quali caselle richiedono un'interfaccia di scelta, o per il factory pattern lato Spring) — i due meccanismi di **innesco** restano diversi:
  - **Bivio**: si attiva **attraversando**, anche a metà mossa, appena la casella offre più opzioni (`options.size() > 1`), indipendentemente dai passi rimasti.
  - **Portale con scelta**: si attiva **solo su atterraggio esatto**, via `applyEffect` (passandoci in mezzo non succede nulla).
- La **risoluzione** della scelta è invece **unificata** in `Game` (prima Bivio e Portale avevano campi/metodi separati e duplicati):
  ```java
  private Player awaitingPlayer;
  private List<Integer> awaitingOptions;
  private int awaitingRemainingSteps;
  private boolean awaitingForward;
  private boolean awaitingRespectCheckpoint;
  private boolean awaitingContinueAfterChoice;
  ```
  - `isAwaitingChoice()` / `getChoiceOptions()`: stato generico.
  - `chooseDestination(player, chosenPosition)`: valida (player corretto, posizione tra le opzioni), sposta il giocatore, poi **se `continueAfterChoice` è vero** prosegue lo stepping con i passi rimasti (caso Bivio); **se è falso**, si ferma lì, nessuna prosecuzione (caso Portale — "niente di niente": nessuna catena, nessun controllo vittoria, coerente col teletrasporto normale).
  - `requestChoice(player, options)` (pubblico): usato dalle caselle "terminali" come il Portale, chiamato da `applyEffect`. Imposta `continueAfterChoice = false`.
  - La pausa per il Bivio viene impostata internamente dal motore (dentro `moveForwardBy`/`moveBackwardByInternal`) con `continueAfterChoice = true`.
- Validazioni di `chooseDestination` (player deve corrispondere a quello in attesa, posizione deve essere tra le opzioni, deve esserci una scelta pendente) — stesso identico comportamento di prima, ora scritto una sola volta invece che duplicato.

### Portale — dettagli aggiornati

- `PortalSquare(position, List<Integer> linkedPositions)` — **prima era un singolo `int linkedPosition`**, ora una lista.
- **1 elemento** → comportamento invariato: teletrasporto immediato (`game.teleportTo`), nessuna pausa.
- **più di 1 elemento** → si attiva la scelta (`game.requestChoice`), solo su atterraggio esatto (mai attraversandolo a metà).
- **Limitazioni delle uscite**: ogni portale ha la propria lista esplicita di collegamenti — non "tutti i portali della board", ma solo quelli effettivamente linkati a quell'istanza (stesso principio già usato per il Bivio).
- Bidirezionalità: sempre ottenuta piazzando istanze che si linkano a vicenda, nessun meccanismo speciale.
- Dopo la scelta: **nessuna catena, nessun controllo vittoria** — se un'uscita coincide con `finishPosition`, il giocatore non vince (limite noto e accettato, responsabilità del design del tabellone).
- Implementa `InteractiveSquare`.

### Bivio — invariato nella logica, solo il meccanismo di risoluzione è ora condiviso col Portale (vedi sopra)

- `BivioSquare(position, List<Integer> nextPositions, List<Integer> previousPositions)`, implementa `InteractiveSquare`.
- Non sovrascrive `applyEffect` — la scelta è parte del motore di movimento, non un effetto di casella. Nessun messaggio.
- Le caselle di ricongiungimento sono istanze della stessa classe, con più elementi in `previousPositions` invece che in `nextPositions`.
- Una `BivioSquare` con una sola opzione in una direzione si comporta come casella normale in quella direzione (usata come "redirect" a senso unico per liberare numerazione tra percorsi alternativi — vedi gotcha di numerazione già noto, invariato).
- Atterrare esattamente su un Bivio con 0 passi rimasti **non forza una scelta** — la scelta si presenta solo quando si prova davvero a proseguire oltre. Bivi annidati funzionano senza codice dedicato.

### Meccanica "a catena" — regole generali (invariate)
- Dopo ogni spostamento, si ricontrolla la casella di arrivo: vittoria se è l'ultima, altrimenti si applica l'effetto della casella lì.
- Catena senza limite per ora (guardrail anti-loop ancora in backlog).
- Eccezioni alla catena: `StartOverSquare` (commentata di proposito), `SnackSquare`/`DrinkSquare`/`ObligationSquare`/`CheckpointSquare` (non spostano il giocatore), `PortalSquare` (esplicito, "niente di niente").

### Debiti tecnici noti
- `Board.getSquares()` e `Game.getPlayers()` espongono ancora le liste interne mutabili direttamente — deliberatamente rimandato, legato al futuro metodo di creazione board.
- `playTurn()` non gestisce la pausa di `isAwaitingChoice()` (Bivio o Portale) — non è un bug in senso stretto (nessuna board attuale con questi elementi usa `playTurn()`), da risolvere quando si integra tutto nel flusso reale. Le board che li usano vanno pilotate con un ciclo custom (vedi `GameDemo3`).
- `Game.getLastRoll()` mostra solo l'ultimo tiro di dado, non gli step intermedi di una catena.
- **Probabile secondo commit necessario**: nell'ultima sessione precedente era stato dato un commit intermedio ("Add checkpoint square and direction choice rule"); da allora sono stati aggiunti Portale, Bivio, `GameDemo3`, checkpoint obbligatorio, portale con scelta, e l'intero refactor del motore (rimozione coda) — probabile che serva un commit consistente per tutto questo lavoro.

---

## 6. Codice sorgente — versione aggiornata e verificata

Tutti i file sottostanti sono stati compilati ed eseguiti con successo (Java 21). Regressione completa verificata in questa sessione su: movimento base e catena, rimbalzo classico, vittoria solo su atterraggio esatto, checkpoint (aggiornamento, clamp con/senza regola, pavimento a 0, fermo obbligatorio in avanti, nessun blocco ripartendo da un checkpoint), direction choice (avanti/indietro, fallback), Portale (singolo immediato, multiplo con pausa, attraversamento senza trigger, validazioni), Bivio (a metà mossa, atterraggio esatto senza scelta forzata, annidati, ricongiungimento, validazioni) — 30 run randomici su una board completa stile `GameDemo3` più `GameDemo3` interattiva eseguita su entrambi i rami.

### `entity/Board.java`
```java
package entity;

import java.util.ArrayList;
import java.util.List;

public class Board {

    private final int finishPosition;
    private final List<Square> squares;
    private boolean directionChoiceEnabled;
    private boolean mandatoryCheckpointEnabled;

    public Board(int size) {
        if (size <= 0) {
            throw new IllegalArgumentException("Board size must be positive");
        }
        this.finishPosition = size;
        this.squares = new ArrayList<>();
        for (int position = 0; position <= size; position++) {
            squares.add(new Square(position));
        }
        this.directionChoiceEnabled = false;
        this.mandatoryCheckpointEnabled = false;
    }

    public int getFinishPosition() {
        return finishPosition;
    }

    public List<Square> getSquares() {
        return squares;
    }

    public boolean isDirectionChoiceEnabled() {
        return directionChoiceEnabled;
    }

    public void setDirectionChoiceEnabled(boolean directionChoiceEnabled) {
        this.directionChoiceEnabled = directionChoiceEnabled;
    }

    public boolean isMandatoryCheckpointEnabled() {
        return mandatoryCheckpointEnabled;
    }

    public void setMandatoryCheckpointEnabled(boolean mandatoryCheckpointEnabled) {
        this.mandatoryCheckpointEnabled = mandatoryCheckpointEnabled;
    }
}
```

### `entity/Dice.java`
```java
package entity;

import java.util.concurrent.ThreadLocalRandom;

public class Dice {

    private static final int MIN_VALUE = 1;
    private static final int MAX_VALUE = 6;

    public int roll() {
        return ThreadLocalRandom.current().nextInt(MIN_VALUE, MAX_VALUE + 1);
    }
}
```

### `entity/Player.java`
```java
package entity;

public class Player {

    private final String name;
    private int position;
    private int snack;
    private int drink;
    private int checkpoint;

    public Player(String name) {
        this.name = name;
        this.position = 0;
        this.snack = 0;
        this.drink = 0;
        this.checkpoint = 0;
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

    public int getCheckpoint() {
        return checkpoint;
    }

    void setCheckpoint(int checkpoint) {
        this.checkpoint = Math.max(0, checkpoint);
    }
}
```

### `entity/Square.java`
```java
package entity;

import java.util.List;

public class Square {

    private final int position;

    public Square(int position) {
        this.position = position;
    }

    public int getPosition() {
        return position;
    }

    public void applyEffect(Player player, Game game) {
    }

    public List<Integer> getNextPositions() {
        return List.of(position + 1);
    }

    public List<Integer> getPreviousPositions() {
        return List.of(position - 1);
    }

    public boolean isMandatoryStop() {
        return false;
    }
}
```

### `entity/InteractiveSquare.java`
```java
package entity;

/**
 * Marcatore per le caselle che richiedono una scelta del giocatore per proseguire
 * (es. Bivio, Portale con piu' uscite). Nessun metodo: i due meccanismi di innesco
 * restano diversi (a meta' step per il Bivio, solo su atterraggio esatto per il Portale),
 * la risoluzione della scelta e' invece unificata in Game (isAwaitingChoice/getChoiceOptions/chooseDestination).
 */
public interface InteractiveSquare {
}
```

### `entity/Game.java`
```java
package entity;

import java.util.List;

public class Game {

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
```

### `entity/squares/StartOverSquare.java`
```java
package entity.squares;

import entity.Game;
import entity.Player;
import entity.Square;

public class StartOverSquare extends Square {

    public StartOverSquare(int position) {
        super(position);
    }

    @Override
    public void applyEffect(Player player, Game game) {
        game.resetToStart(player);
        // game.landOnCurrentSquare(player);
    }
}
```

### `entity/squares/MoveBackSquare.java`
```java
package entity.squares;

import entity.Game;
import entity.Player;
import entity.Square;

public class MoveBackSquare extends Square {

    private final int steps;

    public MoveBackSquare(int position, int steps) {
        super(position);
        this.steps = steps;
    }

    @Override
    public void applyEffect(Player player, Game game) {
        game.moveBackwardBy(player, steps);
    }
}
```

### `entity/squares/MoveForwardSquare.java`
```java
package entity.squares;

import entity.Game;
import entity.Player;
import entity.Square;

public class MoveForwardSquare extends Square {

    private final int steps;

    public MoveForwardSquare(int position, int steps) {
        super(position);
        this.steps = steps;
    }

    @Override
    public void applyEffect(Player player, Game game) {
        game.moveForwardBy(player, steps);
    }
}
```

### `entity/squares/RollAgainSquare.java`
```java
package entity.squares;

import entity.Game;
import entity.Player;
import entity.Square;

public class RollAgainSquare extends Square {

    public RollAgainSquare(int position) {
        super(position);
    }

    @Override
    public void applyEffect(Player player, Game game) {
        game.rollAndMove(player);
    }
}
```

### `entity/squares/SnackSquare.java`
```java
package entity.squares;

import entity.Game;
import entity.Player;
import entity.Square;

public class SnackSquare extends Square {

    private final int amount;

    public SnackSquare(int position, int amount) {
        super(position);
        this.amount = amount;
    }

    @Override
    public void applyEffect(Player player, Game game) {
        int newValue = Math.max(0, player.getSnack() + amount);
        player.setSnack(newValue);
    }
}
```

### `entity/squares/DrinkSquare.java`
```java
package entity.squares;

import entity.Game;
import entity.Player;
import entity.Square;

public class DrinkSquare extends Square {

    private final int amount;

    public DrinkSquare(int position, int amount) {
        super(position);
        this.amount = amount;
    }

    @Override
    public void applyEffect(Player player, Game game) {
        int newValue = Math.max(0, player.getDrink() + amount);
        player.setDrink(newValue);
    }
}
```

### `entity/squares/ObligationSquare.java`
```java
package entity.squares;

import entity.Game;
import entity.Player;
import entity.Square;

public class ObligationSquare extends Square {

    public ObligationSquare(int position) {
        super(position);
    }

    @Override
    public void applyEffect(Player player, Game game) {
        System.out.println(buildMessage(game));
    }

    public String buildMessage(Game game) {
        StringBuilder builder = new StringBuilder();
        int finishPosition = game.getBoard().getFinishPosition();

        for (Player p : game.getPlayers()) {
            builder.append(p.getName())
                   .append(" ")
                   .append(p.getPosition())
                   .append("/")
                   .append(finishPosition)
                   .append("\n");
        }

        return builder.toString();
    }
}
```

### `entity/squares/CheckpointSquare.java`
```java
package entity.squares;

import entity.Game;
import entity.Player;
import entity.Square;

public class CheckpointSquare extends Square {

    public CheckpointSquare(int position) {
        super(position);
    }

    @Override
    public void applyEffect(Player player, Game game) {
        game.updateCheckpoint(player);
        System.out.println(buildMessage(player));
    }

    public String buildMessage(Player player) {
        return player.getName() + " ha raggiunto il checkpoint! Quindi deve fare 3 tiri di creazione";
    }

    @Override
    public boolean isMandatoryStop() {
        return true;
    }
}
```

### `entity/squares/PortalSquare.java`
```java
package entity.squares;

import java.util.List;

import entity.Game;
import entity.InteractiveSquare;
import entity.Player;
import entity.Square;

public class PortalSquare extends Square implements InteractiveSquare {

    private final List<Integer> linkedPositions;

    public PortalSquare(int position, List<Integer> linkedPositions) {
        super(position);
        this.linkedPositions = linkedPositions;
    }

    @Override
    public void applyEffect(Player player, Game game) {
        if (linkedPositions.size() > 1) {
            game.requestChoice(player, linkedPositions);
        } else {
            game.teleportTo(player, linkedPositions.get(0));
        }
    }
}
```

### `entity/squares/BivioSquare.java`
```java
package entity.squares;

import java.util.List;

import entity.InteractiveSquare;
import entity.Square;

public class BivioSquare extends Square implements InteractiveSquare {

    private final List<Integer> nextPositions;
    private final List<Integer> previousPositions;

    public BivioSquare(int position, List<Integer> nextPositions, List<Integer> previousPositions) {
        super(position);
        this.nextPositions = nextPositions;
        this.previousPositions = previousPositions;
    }

    @Override
    public List<Integer> getNextPositions() {
        return nextPositions;
    }

    @Override
    public List<Integer> getPreviousPositions() {
        return previousPositions;
    }
}
```

### `demo/GameDemo2.java`
*(nessun package. Non toccata in questa sessione — l'API pubblica di `Game` usata qui non è cambiata nella firma.)*
```java
import java.util.List;

import entity.Board;
import entity.Dice;
import entity.Game;
import entity.Player;
import entity.squares.DrinkSquare;
import entity.squares.MoveBackSquare;
import entity.squares.MoveForwardSquare;
import entity.squares.ObligationSquare;
import entity.squares.RollAgainSquare;
import entity.squares.SnackSquare;
import entity.squares.StartOverSquare;

public class GameDemo2 {

    public static void main(String[] args) {

        Board board = new Board(25);

        board.getSquares().set(2, new RollAgainSquare(2));
        board.getSquares().set(3, new SnackSquare(3, 3));
        board.getSquares().set(6, new DrinkSquare(6, 2));
        board.getSquares().set(9, new MoveForwardSquare(9, 4));   // atterra su 13 -> concatenazione
        board.getSquares().set(13, new ObligationSquare(13));
        board.getSquares().set(16, new MoveBackSquare(16, 3));    // atterra su 13 -> concatenazione
        board.getSquares().set(20, new StartOverSquare(20));
        board.getSquares().set(23, new SnackSquare(23, -5));      // testa il clamp a 0

        Player player1 = new Player("Gigi");
        Player player2 = new Player("Dodo");

        Dice dice = new Dice();
        Game game = new Game(board, List.of(player1, player2), dice);

        int maxTurns = 1000;
        int turnCount = 0;

        while (game.getWinner() == null && turnCount < maxTurns) {
            game.playTurn();
            turnCount++;

            for (Player player : game.getPlayers()) {
                String rollInfo = player == game.getLastPlayerToMove()
                        ? " (dado: " + game.getLastRoll() + ")"
                        : "";
                System.out.println(player.getName()
                        + " -> position " + player.getPosition() + rollInfo + " - "
                        + " snack: " + player.getSnack()
                        + " drink: " + player.getDrink()
                        + " - " + turnCount);
            }
        }

        if (game.getWinner() != null) {
            System.out.println("Winner: " + game.getWinner().getName());
        } else {
            System.out.println("No winner after " + turnCount + " turns");
        }
    }
}
```

### `demo/GameDemo3.java`
*(nessun package. Dimostrazione interattiva del Bivio — aggiornata questa sessione solo nei nomi dei metodi di scelta, ora unificati con quelli del Portale.)*
```java
import java.util.List;
import java.util.Scanner;

import entity.Board;
import entity.Dice;
import entity.Game;
import entity.Player;
import entity.squares.BivioSquare;
import entity.squares.CheckpointSquare;
import entity.squares.ObligationSquare;

public class GameDemo3 {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        Board board = new Board(30);

        // Bivio in 5: percorso principale (6,7,8-checkpoint,9) o percorso alternativo (10,11-obbligo,12)
        board.getSquares().set(5, new BivioSquare(5, List.of(6, 10), List.of(4)));

        // Percorso principale: 3 caselle proprie, poi redirect esplicito al ricongiungimento (20)
        board.getSquares().set(8, new CheckpointSquare(8));
        board.getSquares().set(9, new BivioSquare(9, List.of(20), List.of(8)));

        // Percorso alternativo: 3 caselle proprie (numeri liberi), poi redirect a 20
        board.getSquares().set(11, new ObligationSquare(11));
        board.getSquares().set(12, new BivioSquare(12, List.of(20), List.of(11)));

        // Ricongiungimento: riceve da entrambi i percorsi, poi prosegue normale verso l'arrivo
        board.getSquares().set(20, new BivioSquare(20, List.of(21), List.of(9, 12)));

        Player player1 = new Player("Gigi");
        Player player2 = new Player("Dodo");
        List<Player> players = List.of(player1, player2);

        Game game = new Game(board, players, new Dice());

        int currentPlayerIndex = 0;
        int turnCount = 0;
        int maxTurns = 200;

        while (game.getWinner() == null && turnCount < maxTurns) {
            if (currentPlayerIndex == 0) {
                turnCount++;
            }

            Player currentPlayer = players.get(currentPlayerIndex);
            game.rollAndMove(currentPlayer);

            while (game.isAwaitingChoice()) {
                resolveChoice(game, currentPlayer, scanner);
            }

            System.out.println(currentPlayer.getName()
                    + " -> position " + currentPlayer.getPosition()
                    + " (dado: " + game.getLastRoll() + ")"
                    + " - turno " + turnCount);

            if (game.getWinner() == null) {
                currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
            }
        }

        scanner.close();

        if (game.getWinner() != null) {
            System.out.println("Winner: " + game.getWinner().getName());
        } else {
            System.out.println("No winner after " + turnCount + " turns");
        }
    }

    private static void resolveChoice(Game game, Player player, Scanner scanner) {
        List<Integer> options = game.getChoiceOptions();
        System.out.println(player.getName() + " deve scegliere! Opzioni: " + options);

        Integer chosen = null;
        while (chosen == null) {
            System.out.print("Scegli una posizione tra " + options + ": ");
            String line = scanner.nextLine().trim();
            try {
                int candidate = Integer.parseInt(line);
                if (options.contains(candidate)) {
                    chosen = candidate;
                } else {
                    System.out.println("Scelta non valida, riprova.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Inserisci un numero valido.");
            }
        }

        game.chooseDestination(player, chosen);
    }
}
```

**Nota**: `GameDemo1.java` non fa più parte del progetto.

---

## 7. TODO / Prossimi passi (aggiornato, con priorità)

### Alta priorità
1. **Metodo di creazione board** — piazzamento caselle (sostituendo i `.set()` manuali delle demo), le **"regole della board"** configurabili (direction choice, checkpoint obbligatorio, probabilmente altre in futuro), e la chiusura delle liste mutabili di `getSquares()`/`getPlayers()`.
2. **Factory pattern per le caselle** (per quando si integra Spring Boot) — prende un JSON con attributo `type` e restituisce l'istanza `Square` corrispondente.
3. **Bivio con percorsi non riconvergenti** — per ora tutti i percorsi si ricongiungono prima del traguardo; in futuro percorsi che non si ricongiungono mai, con il rimbalzo di fine tabellone che in quel caso cambierebbe solo il senso di marcia.
4. **Board con più caselle di arrivo** — conseguenza diretta del punto 3.

### Backlog (nessun ordine specifico)
- Altri tipi di caselle speciali oltre ai 10 attuali.
- **Guardrail anti-loop** per catene di caselle speciali che si rimbalzano a vicenda all'infinito.
- `ObligationSquare.buildMessage()` / `CheckpointSquare.buildMessage()` — ancora mockup, attese modifiche future.
- `Game.getLastRoll()` — mostra solo l'ultimo tiro di una catena, non gli step intermedi.
- `playTurn()` non gestisce la pausa di `isAwaitingChoice()` — da rivedere quando si integra tutto nel flusso reale.
- Fermo obbligatorio sul checkpoint per il backward — oggi solo il clamp al proprio checkpoint (condizionato alla regola), nessun "fermo su qualsiasi checkpoint incontrato" come per il forward.
- Portale con "determinate limitazioni" oltre alla lista esplicita di collegamenti — se in futuro servirà altro.
- Integrazione Spring Boot 4.1.0 — non ancora iniziata.
- Autenticazione utenti (JWT vs sessione) — non ancora decisa.
- Frontend Angular — non ancora iniziato.
- Valutare **Electron** per le fasi successive, quando BE e FE saranno pronti.
- Probabile commit consistente da fare (vedi sezione 5, Debiti tecnici).

### Fatto in questa sessione (non più TODO)
- ~~Portale con uscita scelta dal giocatore~~ → `PortalSquare` ora accetta `List<Integer>`, scelta solo su atterraggio esatto, limitazioni per collegamenti espliciti per istanza.
- ~~Checkpoint obbligatorio~~ → `Square.isMandatoryStop()`, `Board.mandatoryCheckpointEnabled`, fermo in avanti, clamp indietro condizionato alla stessa regola, pavimento fisso a 0 sempre attivo.
- ~~Refactor caselle interattive/autonome~~ → interfaccia marcatore `InteractiveSquare` (Bivio, Portale); motore riprogettato per fermarsi al primo punto di scelta e proseguire dopo (non precalcolo completo — vedi percorso di design in sezione 5); `RollAgainSquare` risolto di conseguenza, senza bisogno di codice dedicato.
- ~~Rimozione della coda `Runnable`~~ → motore a loop diretto, meccanismo di scelta (Bivio + Portale) unificato in `Game`.

---

## 8. Decisioni "minori" ma da ricordare

- Traduzione "obbligo" → `ObligationSquare` (nome definitivo).
- Package reale: **`entity`** e **`entity.squares`**.
- Convenzione di visibilità: preferire scelte più ampie/future-proof di default, tranne quando serve vero incapsulamento.
- `resetToStart` ha mantenuto il nome anche se torna al checkpoint (non sempre a 0).
- `moveByChosenDirection` non lancia eccezione se la regola di direzione non è attiva — fallback silenzioso ad "avanti".
- Atterrare esattamente su un `BivioSquare` con 0 passi rimasti non forza una scelta — vale anche per il Portale (si attiva solo su atterraggio esatto, non attraversandolo).
- Il rimbalzo di fine tabellone non rispetta mai il checkpoint — asimmetria voluta, preservata anche nel nuovo motore a loop e protetta in più dal pavimento fisso a 0.
- Le caselle di ricongiungimento di un bivio sono istanze della stessa classe `BivioSquare`.
- Costruendo board con bivi, attenzione alla numerazione: un percorso alternativo con caselle proprie deve usare numeri che il percorso principale non attraverserebbe mai di default, altrimenti loop di gameplay silenzioso (non un crash).
- **Le `GameDemoN` sono checkpoint usa-e-getta** (vedi sezione 2) — salvato anche nella memoria persistente di Claude per questo progetto, da non perdere tra una conversazione e l'altra.
- **Eccezione "classe intera"**: questa sessione Claude ha mostrato `Game.java` per intero su richiesta esplicita, data la portata del refactor (quasi ogni metodo toccato, difficile da seguire come sequenza di frammenti) — resta un'eccezione puntuale, non il comportamento di default per modifiche normali.
