# FUMANJI — Dump di Progetto

> Archivio esteso, non riassunto. Contiene tutto ciò che è stato deciso e scritto fino a questo punto. Da caricare a inizio di ogni nuova conversazione per riprendere il lavoro da qui.

---

## 1. Scopo e contesto

Sviluppo di una web application: variante del "Gioco dell'Oca" classico, con caselle speciali che attivano effetti vari (alcuni "stupefacenti"). Stack previsto: **Backend Spring + Frontend Angular**. Si è partiti dal backend, e in particolare dalla logica di dominio in **Java puro**, prima di introdurre Spring.

**Repository**: `https://github.com/mirto97/Fumanji.git` (privato)

---

## 2. Modalità di lavoro (regole di processo — vincolanti)

- **"IO SONO LA MENTE E TU SEI IL BRACCIO"**: l'utente decide, Claude scrive codice. Claude non deve pensare/decidere al posto dell'utente a meno che non venga esplicitamente richiesto. Consigli, correzioni e accorgimenti sono sempre ben accetti e vanno discussi insieme.
- Risposte **veloci e dirette**; approfondire solo se richiesto esplicitamente; **non ripetere** cose già scritte o decise.
- **Non scrivere codice finché non si è sicuri** di cosa si sta scrivendo.
- **Non supporre o dedurre nulla** a meno che non venga detto esplicitamente; in caso di dubbio, si verifica insieme il risultato ("controlliamo se è corretto").
- **Non inventare nulla** di propria iniziativa — chiedere, piuttosto.
- Nomi di classi/metodi/variabili sempre in **inglese**. Commenti nel codice sempre in **italiano**, asciutti e in stile "codice da progetto", e solo quando esplicitamente richiesti.
- Il codice va scritto **sempre inline in chat** (mai come file scaricabile) — **eccetto il dump di fine conversazione**, che è invece sempre un file `.md` scaricabile.
- **Regola introdotta a metà conversazione**: quando si modifica codice già scritto, **non riscrivere mai la classe intera** — mostrare solo i metodi/le righe esatte cambiate. Motivo: l'utente sta aggiungendo progressivamente propri commenti nel codice via IDE, e riscrivere intere classi cancellava quel lavoro.
- Esiste sempre un file di progetto con **"...dump..."** nel nome, contenente tutto il lavoro fatto fino a quel momento. Se Claude non lo trova in una nuova chat, deve segnalarlo. Ogni nuova conversazione riparte da lì. A fine conversazione, Claude produce un dump aggiornato (questo documento), che contiene tutto il dump precedente **più** tutto ciò che è stato deciso/cambiato nella conversazione corrente.
- Prassi osservata (non regola esplicita, ma consolidata durante il lavoro): quando possibile, Claude compila ed esegue il codice nel proprio ambiente sandbox per verificarne la correttezza **prima** di presentarlo.

---

## 3. Stack tecnologico

| Aspetto | Scelta |
|---|---|
| Linguaggio/runtime | Java 21 |
| Build tool | Maven |
| Framework backend | Spring Boot **4.1.0** (ultima stabile; la linea 3.x è EOL dal 30/06/2026) — non ancora integrato, si sta completando prima il layer di dominio in Java puro |
| Database | MySQL (relazionale) |
| Stile API | REST puro (**no WebSocket**) — comunicazione BE-FE tramite chiamate/polling |
| Frontend | Angular — non ancora iniziato |
| Autenticazione | Confermato: servono account utente (login/registrazione). Meccanismo (JWT vs sessione) **non ancora deciso** — discussione messa in pausa per dare priorità alla logica di dominio |

---

## 4. Struttura cartelle e package

Struttura reale confermata da screenshot dell'utente:

```
Fumanji/
├── BE/
│   └── model/                      (cartella organizzativa, NON fa parte del nome dei package Java)
│       ├── demo/                   (nessun package dichiarato — non serve)
│       │   └── GameDemo2.java
│       └── entity/                 (package: entity)
│           ├── Board.java
│           ├── Dice.java
│           ├── Game.java
│           ├── Player.java
│           ├── Square.java
│           └── squares/            (package: entity.squares)
│               ├── DrinkSquare.java
│               ├── MoveBackSquare.java
│               ├── MoveForwardSquare.java
│               ├── ObligationSquare.java
│               ├── RollAgainSquare.java
│               ├── SnackSquare.java
│               └── StartOverSquare.java
└── Documentazione/
```

Note importanti:
- I package Java realmente usati sono **`entity`**, **`entity.squares`** e **nessun package** per la cartella `demo` (l'utente ha specificato che sulle demo non può/non serve mettere il package).
- `GameDemo1.java` è stato **volutamente dimenticato/escluso** da questo dump: è superato da `GameDemo2.java` e non va più considerato parte del progetto.
- I metodi "di orchestrazione" di `Game` (`rollAndMove`, `moveForwardBy`, `moveBackwardBy`, `landOnCurrentSquare`) sono **public**. In origine erano package-private (corretto quando tutto stava nel default package); sono stati resi public nel momento in cui si è introdotto il vero split di package (`entity` vs `entity.squares`), perché altrimenti le sottoclassi di `Square` in `entity.squares` non avrebbero potuto richiamarli.
- **Convenzione concordata per il futuro**: preferire scelte di visibilità/design più ampie e "future-proof" fin da subito, anche senza necessità immediata, dato che il progetto crescerà molto. Questo aggiorna (senza contraddirlo nel merito) il principio di minima visibilità usato inizialmente, che era comunque corretto per lo stato del progetto in quel momento.

---

## 5. Decisioni di design e logica di dominio

### Board
- Campo `finishPosition` (rinominato da `size` per chiarezza: rappresenta il numero dell'ultima casella/arrivo, **non** un conteggio — la lista `squares` ha in realtà `finishPosition + 1` elementi).
- Le caselle (`Square`) vanno da posizione **0** (casella di partenza, vera casella reale, non un valore "fuori tabellone") a `finishPosition` (arrivo) incluso.
- Per ora un **solo tabellone fisso**, ma pensato per essere estendibile in futuro a più tabelloni di difficoltà/lunghezza diverse (nessun valore hardcoded sparso nel codice).
- Metodo dedicato per la creazione dei tabelloni: **non ancora costruito** (vedi TODO).

### Dado
- **1 dado** per turno (non 2, a differenza del gioco dell'oca classico).
- Valori 1-6, generati con `ThreadLocalRandom`.

### Movimento e vittoria
- Il tiro di dado sposta in avanti. Se supera l'ultima casella, si applica il **rimbalzo classico** (si torna indietro della quantità in eccesso).
- La vittoria si ottiene **solo arrivando esattamente** sull'ultima casella (`finishPosition`).
- Quando un giocatore vince, **la partita finisce subito**: gli altri giocatori non giocano più (nessuna classifica/continuazione).
- `playTurn()` chiamato a partita già conclusa: **non fa nulla** (nessuna eccezione).

### Giocatori e turni
- Numero giocatori: minimo **1**, nessun massimo imposto nel codice (range d'uso reale atteso: 2-6).
- L'ordine dei turni segue l'**ordine di inserimento** nella lista giocatori passata a `Game`.
- `Player` ha: `name` (String), `position` (int, parte da 0 = casella di partenza), `snack` (int, counter stackabile), `drink` (int, counter stackabile).
- **`snack`/`drink` sono centrali nel gioco**: rappresentano quanti snack/drink il giocatore ha raccolto. Si possono accumulare (stack) e usare (sottrazione diretta della quantità usata dallo stack). L'effetto reale "nella vita vera" di questi counter non è rilevante per ora e non va dedotto/discusso a livello di codice. **In futuro verranno aggiunti altri counter simili.**
- Scelta esplicita: per ora restano **campi dedicati** su `Player` (niente `Map`/`enum` generico), perché saranno sempre di tipo `int`; si rifattorizza solo se in futuro servirà davvero (es. se arrivano counter non-int, se ne riparla).

### Gerarchia delle caselle speciali (Square)
- **Pattern scelto: ereditarietà** (non composizione/strategy).
- `Square` è **concreta** (non astratta), ha il campo `position` e un metodo `applyEffect(Player player, Game game)` che di default **non fa nulla** — le sottoclassi lo sovrascrivono.
- Tipi implementati finora (7, corrispondenti alla stima iniziale di "6-7 tipi"):
  1. `StartOverSquare` — riporta il giocatore a posizione 0.
  2. `MoveBackSquare(position, steps)` — indietro di `steps`, **clamp a 0** (non può andare sotto lo zero). `steps` è parametrico (i valori concreti verranno impostati dal futuro metodo di creazione board).
  3. `MoveForwardSquare(position, steps)` — avanti di `steps`, con **lo stesso rimbalzo classico** del dado se si supera `finishPosition`. `steps` parametrico.
  4. `RollAgainSquare` — il giocatore rilancia subito il dado (stesso giocatore).
  5. `SnackSquare(position, amount)` — modifica lo snack del giocatore di `amount` (vedi sotto: riusata anche per "togliere").
  6. `DrinkSquare(position, amount)` — come sopra, per drink.
  7. `ObligationSquare(position)` — genera un messaggio (vedi sezione dedicata sotto). Nome italiano originale: "obbligo". Prima ipotesi di traduzione era stata `ForfeitSquare`, **poi cambiata dall'utente in `ObligationSquare`** (nome definitivo).

### Meccanica "a catena" (concatenazione tra caselle speciali)
- Dopo **ogni** spostamento (sia il tiro iniziale di dado, sia uno spostamento generato da una casella speciale), si ricontrolla la casella di arrivo: se è la casella finale si vince, altrimenti si recupera la `Square` a quella posizione e si chiama `applyEffect`.
- Questo crea una **catena**: atterrare su una casella che sposta ulteriormente il giocatore può far scattare a sua volta un'altra casella speciale, ricorsivamente, **senza limite** per ora.
- Meccanismo concreto: `Game.moveForwardBy()` e `Game.moveBackwardBy()` chiamano sempre `landOnCurrentSquare()` alla fine — è così che la catena si propaga automaticamente per `MoveForwardSquare`, `MoveBackSquare` e `RollAgainSquare` (quest'ultimo passa da `rollAndMove()` → `moveForwardBy()`).
- **Eccezione voluta: `StartOverSquare` NON fa catena.** La chiamata a `game.landOnCurrentSquare(player)` dopo il reset a 0 è stata **commentata (non cancellata)** su decisione esplicita dell'utente, perché si ritiene remota la possibilità che la casella 0 abbia mai un effetto. Se in futuro servisse, va scommentata — ma attenzione: se la casella 0 avesse un effetto, **non scatterebbe passando da `StartOverSquare`**, mentre scatterebbe se ci si arriva tramite `MoveBackSquare` (che la catena ce l'ha).
- `SnackSquare`, `DrinkSquare`, `ObligationSquare` **non fanno catena**: non spostano il giocatore, quindi non c'è una nuova casella da ricontrollare (richiamare la catena su loro stesse creerebbe un loop infinito sulla stessa casella).
- **Guardrail anti-loop**: **esplicitamente saltato per ora**. Rischio noto: due caselle speciali che si "rimbalzano" a vicenda (es. A ti manda su B, B ti manda su A) causano ricorsione infinita. Per ora è considerata responsabilità del design del tabellone, non del codice. **Da rivedere in futuro** (segnalato esplicitamente dall'utente come cosa da recuperare).

### SnackSquare / DrinkSquare — riuso per "aggiungere" e "togliere"
- Invece di creare classi separate per "meno drink"/"meno eat", si è deciso di **riusare le stesse classi** `SnackSquare`/`DrinkSquare` passando un `amount` **negativo**.
- È stato aggiunto un **clamp a 0** (`Math.max(0, ...)`) per evitare che il counter vada sotto zero, indipendentemente dal segno di `amount`.

### ObligationSquare — dettagli
- Ha un metodo `buildMessage(Game game)` che **ritorna una String** (non stampa direttamente) — scelta esplicita per riusabilità futura (potrà essere stampato, inviato al FE, ecc.).
- Il messaggio elenca, per ogni giocatore, nome e posizione nel formato `posizione/finishPosition` (es. `"Gigi 13/25"`), una riga per giocatore.
- **Nota esplicita dell'utente**: ci saranno molte modifiche future a questo metodo, o verrà overloadato se più fattibile — è solo la prima versione.
- Per ora `applyEffect` chiama `buildMessage` e stampa il risultato su console (`System.out.println`) — **placeholder provvisorio**, quasi certamente cambierà quando sarà chiaro come/dove i messaggi andranno mostrati (Spring/FE).
- Per permettere l'accesso a `finishPosition` da dentro `ObligationSquare`, è stato aggiunto `Game.getBoard()` (espone il riferimento a `Board`).

### Tracciamento ultimo tiro (per debug/demo)
- Aggiunti a `Game`: campo `lastRoll` (int) e `lastPlayerToMove` (Player), con relativi getter `getLastRoll()` / `getLastPlayerToMove()`.
- Impostati dentro `rollAndMove()` ad ogni chiamata.
- **Limite noto**: se un turno contiene una catena (es. tramite `RollAgainSquare`, o una `MoveForwardSquare`/`MoveBackSquare` che fa scattare un'altra casella speciale), viene tracciato solo **l'ultimo** tiro della catena, non gli step intermedi. Segnalato come possibile miglioramento futuro se servirà vedere l'intera catena di un turno.

### Debiti tecnici noti (esplicitamente rimandati a quando si arriverà su Spring)
- `Board.getSquares()` e `Game.getPlayers()` espongono le liste interne **mutabili** direttamente (nessuna copia difensiva/vista non modificabile). **Nota**: questa mutabilità viene attualmente sfruttata di proposito nelle demo per piazzare le caselle speciali (es. `board.getSquares().set(pos, new XSquare(pos))`) — quindi la futura correzione andrà di pari passo con il metodo di creazione board.
- `Player.setPosition()` è public, chiunque può spostare un giocatore dall'esterno (non solo `Game` tramite `playTurn()`).
- `Game`, nel costruttore, non valida `board`/`dice` per `null` (se arrivano `null`, l'eccezione salterebbe fuori più tardi come `NullPointerException` dentro `playTurn()`, non subito alla creazione).

---

## 6. Codice sorgente — versione aggiornata e verificata

Tutti i file sottostanti sono stati compilati ed eseguiti con successo nell'ambiente di verifica (Java 21), inclusi test mirati su: rimbalzo del dado, clamp a 0 di `MoveBackSquare`, catena su `MoveBackSquare`→`Snack`, rimbalzo su `MoveForwardSquare`, clamp a 0 su `SnackSquare` con amount negativo, generazione messaggio `ObligationSquare`, ed esecuzione completa di `GameDemo2` con tutti i 7 tipi di casella e 2 concatenazioni volute.

### `entity/Board.java`
```java
package entity;

import java.util.ArrayList;
import java.util.List;

public class Board {

    private final int finishPosition;
    private final List<Square> squares;

    public Board(int size) {
        if (size <= 0) {
            throw new IllegalArgumentException("Board size must be positive");
        }
        this.finishPosition = size;
        this.squares = new ArrayList<>();
        for (int position = 0; position <= size; position++) {
            squares.add(new Square(position));
        }
    }

    public int getFinishPosition() {
        return finishPosition;
    }

    public List<Square> getSquares() {
        return squares;
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
```

### `entity/Square.java`
```java
package entity;

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

        Player currentPlayer = players.get(currentPlayerIndex);
        rollAndMove(currentPlayer);

        if (winner == null) {
            currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
        }
    }

    public void rollAndMove(Player player) {
        int roll = dice.roll();
        lastRoll = roll;
        lastPlayerToMove = player;
        moveForwardBy(player, roll);
    }

    public void moveForwardBy(Player player, int steps) {
        int newPosition = player.getPosition() + steps;
        int finishPosition = board.getFinishPosition();

        if (newPosition > finishPosition) {
            int overshoot = newPosition - finishPosition;
            newPosition = finishPosition - overshoot;
        }

        player.setPosition(newPosition);
        landOnCurrentSquare(player);
    }

    public void moveBackwardBy(Player player, int steps) {
        int newPosition = Math.max(0, player.getPosition() - steps);
        player.setPosition(newPosition);
        landOnCurrentSquare(player);
    }

    public void landOnCurrentSquare(Player player) {
        if (player.getPosition() == board.getFinishPosition()) {
            winner = player;
            return;
        }

        Square square = board.getSquares().get(player.getPosition());
        square.applyEffect(player, this);
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
        player.setPosition(0);
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

### `demo/GameDemo2.java`
*(nessun package — non serve nella cartella demo. Versione con le rifiniture dell'utente: nomi Gigi/Dodo, formattazione con `snack:`/`drink:`/contatore turno, e annotazione del tiro di dado sulla riga di chi si è mosso.)*
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

**Nota**: `GameDemo1.java` esisteva in una versione precedente ma è stato esplicitamente escluso da questo dump su richiesta dell'utente — non fa più parte del progetto.

---

## 7. TODO / Prossimi passi

- [ ] **Metodo di creazione board** — attualmente le caselle speciali vengono piazzate a mano nelle demo (`board.getSquares().set(pos, new XSquare(...))`); serve un metodo/meccanismo dedicato.
- [ ] **Altri tipi di caselle speciali** oltre ai 7 attuali (l'utente ha indicato che ne arriveranno altre).
- [ ] **Guardrail anti-loop** per le catene di caselle speciali (es. due caselle che si rimbalzano a vicenda all'infinito) — esplicitamente saltato finora, da rivedere.
- [ ] **`StartOverSquare` con checkpoint**: in futuro, invece di tornare sempre a 0, tornare a un checkpoint quando si è superata una determinata casella — non ancora progettato.
- [ ] **Pulizia incapsulamento** (rimandata a quando si arriva su Spring): liste mutabili esposte da `getSquares()`/`getPlayers()`, `Player.setPosition()` public, validazione null nel costruttore di `Game`.
- [ ] **`ObligationSquare.buildMessage()`**: attese molte modifiche/overload futuri; per ora stampa su console come placeholder provvisorio.
- [ ] **`Game.getLastRoll()`**: mostra solo l'ultimo tiro di una catena, non gli step intermedi — possibile miglioramento futuro.
- [ ] **Integrazione Spring Boot 4.1.0** — non ancora iniziata, si sta completando prima il layer di dominio in Java puro.
- [ ] **Autenticazione utenti**: confermata la necessità (login/registrazione), ma il meccanismo (JWT vs sessione) non è ancora stato deciso.
- [ ] **Frontend Angular** — non ancora iniziato.
- [ ] **GameDemo3 o successivo**: quando richiesto, seguire lo stesso approccio di `GameDemo2` (board con tutti i tipi di casella, concatenazioni di prova) aggiornato ai nuovi tipi/decisioni.

---

## 8. Decisioni "minori" ma da ricordare

- Traduzione "obbligo" → inizialmente proposto `ForfeitSquare`, **cambiato dall'utente in `ObligationSquare`** (nome definitivo da usare).
- Package reale: **`entity`** e **`entity.squares`** (non `model` come inizialmente ipotizzato da Claude prima di vedere la struttura reale).
- La cartella `model` nel repository è solo organizzativa, **non** corrisponde a un package Java.
- Convenzione di visibilità concordata: da ora **preferire scelte più ampie/future-proof di default**, pensando alla crescita del progetto, anche senza necessità immediata comprovata.
