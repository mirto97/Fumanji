# FUMANJI — Dump di Progetto

> Archivio esteso, non riassunto. Contiene lo **stato corrente** del progetto: tutto ciò che è stato deciso e il codice così com'è oggi. Non contiene passaggi intermedi superati (es. il vecchio clamp del checkpoint backward, rimosso e sostituito dal fermo simmetrico; le vecchie `GameDemo2/3/4`, superate da `GameDemo5`). Da caricare a inizio di ogni nuova conversazione per riprendere il lavoro da qui.

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
- **Quando si modifica codice già scritto, non riscrivere mai la classe intera** — mostrare solo i metodi/le righe esatte cambiate, per non sovrascrivere i commenti personali che Salame aggiunge via IDE. Le classi **nuove di zecca** si possono mostrare per intero alla prima stesura (usato questa sessione per `Board.java`, rifatta da zero col builder, e `GameDemo5.java`, nuova demo).
- Esiste sempre un file di progetto con **"...dump..."** nel nome, contenente lo stato corrente del progetto. Se Claude non lo trova in una nuova chat, deve segnalarlo. Ogni nuova conversazione riparte da lì. A fine conversazione, Claude produce un dump aggiornato: stato corrente, non log storico.
- Prassi consolidata: quando possibile, Claude compila ed esegue il codice nel proprio ambiente sandbox per verificarne la correttezza **prima** di presentarlo, e ripete la regressione su tutto il comportamento già documentato dopo ogni modifica strutturale.
- Quando una richiesta rivela una portata strutturale più ampia di quanto sembrasse, Claude deve fermarsi e spiegare chiaramente l'impatto reale prima di procedere, piuttosto che minimizzare o iniziare a scrivere codice parziale.
- **Le `GameDemoN` sono checkpoint usa-e-getta**: devono funzionare solo al momento in cui vengono create, per verificare che il codice giri. Quando si riprende a modificare il motore/le caselle, non importa se una `GameDemo` precedente smette di funzionare o di compilare — l'obiettivo è arrivare alla prossima `GameDemo` funzionante, non mantenere le vecchie retrocompatibili. **Questa sessione ne è un esempio concreto**: il refactor di `Board` con il builder ha reso `getSquares()` immutabile, quindi `GameDemo2.java`, `GameDemo3.java` e `GameDemo4.java` (che piazzavano le caselle con `board.getSquares().set(...)`) non compilano/funzionano più. Non sono state aggiornate né rimosse dal repository: semplicemente non fanno più parte dello stato "corrente" del progetto, sostituite da `GameDemo5.java`.

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
│       │   └── GameDemo5.java      (demo corrente — vedi nota sotto su GameDemo2/3/4)
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
- `GameDemo1.java` non fa più parte del progetto (superato da `GameDemo2`, sessioni precedenti).
- **`GameDemo2.java`, `GameDemo3.java`, `GameDemo4.java` non compilano/funzionano più**: usavano `board.getSquares().set(position, square)` per piazzare le caselle manualmente, e `getSquares()` ora ritorna una lista immutabile (vedi sezione 5, `Board`). Non sono stati aggiornati — coerente con la filosofia "usa e getta" delle `GameDemoN` (sezione 2). `GameDemo5.java` è l'unica demo corrente, costruita con `Board.builder(...)`.
- I metodi "di orchestrazione" di `Game` sono **public** per convenzione consolidata: preferire scelte di visibilità/design più ampie e future-proof, dato che il progetto crescerà molto. Eccezioni mirate quando serve vero incapsulamento (`Player.setPosition`/`setCheckpoint`, package-private).

---

## 5. Decisioni di design e logica di dominio

### Board — ora costruita con builder, completamente immutabile

- Campi `finishPosition`, `squares`, `directionChoiceEnabled`, `mandatoryCheckpointEnabled`: tutti **final**. `Board` è completamente immutabile una volta costruita (**questa sessione**: rimossi i vecchi setter `setDirectionChoiceEnabled`/`setMandatoryCheckpointEnabled`, erano segnati come "provvisori, in attesa del vero metodo di creazione board" — arrivato quello, non avevano più motivo di esistere).
- **Due modalità di costruzione**:
  1. **`Board(int size)`** — costruttore pubblico invariato: board "vuota" (tutte `Square` di default), regole disattivate. **Deciso esplicitamente di lasciarlo pubblico** anche ora che esiste il builder: il costo di renderlo privato più avanti (quando si integra Spring, o anche oltre) è basso — un cambio di modificatore più l'eventuale aggiornamento dei punti che chiamano `new Board(...)` direttamente. Non è una priorità finché non se ne sente il bisogno.
  2. **`Board.builder(size)`** — nuovo, pattern Builder fluente, sostituisce il piazzamento manuale via `.set()`:
     ```java
     Board board = Board.builder(55)
         .square(3, new RollAgainSquare(3))
         .square(5, new SnackSquare(5, 3))
         .directionChoiceEnabled(true)
         .mandatoryCheckpointEnabled(true)
         .build();
     ```
     - `.square(position, square)`: valida `1 <= position <= finishPosition - 1` (esclude 0 e la casella di arrivo), altrimenti `IllegalArgumentException`.
     - `.directionChoiceEnabled(boolean)` / `.mandatoryCheckpointEnabled(boolean)`: impostano le regole, uniche vie per farlo ora che i setter sono stati rimossi.
     - `.build()`: crea la `Board` con `List.copyOf(squares)` (immutabile).
- `getSquares()` ora ritorna una lista **immutabile** (`List.copyOf`) — prima era mutabile e le demo la modificavano con `.set(...)` direttamente; questo non funziona più (vedi sezione 4).
- Un solo tabellone fisso per ora, pensato per essere estendibile. Invariato.

### Game — `players` ora chiusa

- Il costruttore di `Game` fa `this.players = List.copyOf(players)` invece di tenere il riferimento alla lista passata dal chiamante — stessa filosofia di chiusura applicata a `Board.getSquares()`, per coerenza (evita che il chiamante modifichi la lista di giocatori dopo la creazione della partita).

### Dado
- 1 dado per turno, valori 1-6, `ThreadLocalRandom`. Invariato.

### Movimento e vittoria — motore a loop diretto, invariato nella struttura, con due aggiunte

Il motore resta quello a loop diretto (senza coda `Runnable`) descritto nelle sessioni precedenti: `moveForwardBy`/`moveBackwardBy`/`moveBackwardByInternal`, rimbalzo classico su overshoot, vittoria solo su atterraggio esatto, movimento passo-passo. Non ripetuto qui nel dettaglio (vedi codice sorgente, sezione 6). Due aggiunte:

**Movement trace** (esisteva già come modifica di Salame all'inizio di questa sessione, non ancora documentata nel dump — la si documenta ora):
- Ogni cambio di posizione del giocatore passa per un helper privato `movePlayerTo(player, position)`, che oltre a spostare il giocatore registra la posizione in `movementTrace` (`List<Integer>`).
- `Game.clearMovementTrace()` / `Game.getMovementTrace()` (quest'ultima ritorna una copia immutabile via `List.copyOf`): il **chiamante è responsabile di svuotare la traccia** prima di ogni mossa che vuole tracciare — nessuno svuotamento automatico. `GameDemo4`/`GameDemo5` chiamano `clearMovementTrace()` prima di ogni `moveByChosenDirection`.
- Sostituisce i precedenti `player.setPosition(...)` diretti in `moveForwardBy`, `moveBackwardByInternal`, `teleportTo`, `chooseDestination`.

**Guardrail anti-loop** (nuovo, questa sessione):
- Motivazione: caselle a catena che si rimandano a vicenda all'infinito (es. una `MoveForwardSquare` che atterra su una `MoveBackSquare` che torna sulla prima) saturerebbero lo stack (Java non ottimizza la tail-recursion).
- Regola decisa da Salame: **entro un singolo turno di movimento di un giocatore, la stessa casella non può attivare il proprio effetto più di una volta**. Se ci si ricapita sopra nella stessa catena, si comporta come una casella base (nessun effetto, nessuna ulteriore catena) — il giocatore resta fermo lì.
- Implementazione in `Game`:
  ```java
  private final Set<Integer> activatedPositions = new HashSet<>();
  private boolean chainInProgress;
  ```
  In `doLandOnCurrentSquare`: `chainInProgress` distingue la chiamata che "apre" la catena (quella con `chainInProgress` ancora `false`) dalle chiamate innescate a cascata; solo la prima azzera `activatedPositions`. Uso di `try/finally`: il metodo ha tre punti di uscita (vittoria, casella già attivata, fine normale di `applyEffect`, più un'eventuale eccezione da `applyEffect`), e in tutti serve resettare `chainInProgress` se era stata quella chiamata ad aprire la catena — senza `finally` andrebbe ripetuto prima di ogni `return`, con rischio concreto di dimenticanze che lascerebbero il flag bloccato a `true` per sempre.
- **Limite noto e accettato**: se una catena viene interrotta da una pausa Bivio a metà (richiede che una casella a catena porti dritti su un bivio con passi residui — scenario raro), `chainInProgress` si resetta durante la pausa (il return per la pausa fa srotolare normalmente la pila); alla ripresa dopo la scelta il set si riazzera, "dimenticando" le caselle attivate prima della pausa. Nel peggiore dei casi concede una riattivazione extra subito dopo la ripresa, ma il guardrail torna comunque a proteggere da quel punto in poi — nessun loop infinito realmente possibile. Testato esplicitamente in sandbox (non crasha). Non ulteriormente blindato per scelta esplicita (accettabile per un guardrail "di sicurezza" da backlog).
- Testato: ping-pong a 2 caselle, ciclo a 3 caselle, reset corretto tra un turno di movimento e l'altro (la stessa casella si riattiva normalmente in un turno successivo), catena normale senza ripetizioni invariata, catena che sfocia in un bivio a metà non crasha, più 30 run randomici su board completa.

### Checkpoint — fermo obbligatorio ora simmetrico avanti/indietro

- **Forward invariato**: fermo su qualsiasi casella `isMandatoryStop()`, controllo sulla casella **appena raggiunta**, non su quella di partenza (altrimenti un giocatore che inizia il turno già fermo su un checkpoint resterebbe bloccato per sempre).
- **Backward, riscritto questa sessione**: prima era un *clamp* al proprio checkpoint salvato (`player.getPosition() <= player.getCheckpoint()`, si fermava solo lì). Ora è **lo stesso identico meccanismo del forward**: se la regola è attiva, ci si ferma sulla **prima casella con `isMandatoryStop()` incontrata**, non necessariamente il proprio checkpoint salvato — se sul percorso all'indietro c'è un checkpoint diverso dal proprio (es. un checkpoint intermedio più vicino), ci si ferma lì.
  ```java
  private void moveBackwardByInternal(Player player, int steps, boolean respectCheckpoint) {
      int remaining = steps;
      while (remaining > 0) {
          if (player.getPosition() <= 0) {          // pavimento fisso, sempre attivo
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
          movePlayerTo(player, nextPosition);
          remaining--;
          Square nextSquare = board.getSquares().get(nextPosition);
          if (respectCheckpoint && nextSquare.isMandatoryStop()) {   // simmetrico al forward
              remaining = 0;
          }
      }
      doLandOnCurrentSquare(player);
  }
  ```
- Motivazione (Salame, testuale): "se la regola della board c'è, sia che stai andando avanti o indietro, quando arrivi ad un checkpoint ti devi fermare, mentre invece se non è attiva la regola non ti fermi e prosegui". Il proprio checkpoint si aggiorna comunque sempre all'ultimo su cui si è atterrati, a prescindere dalla direzione (già garantito da `CheckpointSquare.applyEffect`, invariato).
- Pavimento fisso a 0 invariato: sempre attivo, indipendente dalla regola.
- Testato: fermo sul primo checkpoint incontrato anche se non è il proprio salvato (non salta fino a un checkpoint più lontano); nessun fermo/clamp con regola disattivata (solo pavimento a 0); nessun blocco partendo già fermi su un checkpoint; regressione a 30 run su board multi-checkpoint con direzione libera.
- **TODO chiuso**: "fermo obbligatorio sul checkpoint per il backward" (era in backlog dalle sessioni precedenti).

### Direction choice (scelta di direzione) — invariato
- `Game.roll(player)`, `Game.rollAndMove(player)`, `Game.moveByChosenDirection(player, forward)` — invariati, fallback silenzioso ad avanti se la regola non è attiva.

### Caselle interattive (`InteractiveSquare`, Bivio, Portale) — invariato
- Meccanismo di innesco (Bivio a metà mossa, Portale solo su atterraggio esatto) e risoluzione unificata in `Game` (`isAwaitingChoice`/`getChoiceOptions`/`chooseDestination`/`requestChoice`) invariati rispetto alle sessioni precedenti. **Confermato questa sessione** (Salame): le "limitazioni" del Portale erano già risolte — ogni istanza ha la propria lista esplicita di collegamenti (`linkedPositions`); un portale "ristretto" ha una lista corta, uno che "va ovunque" ha semplicemente una lista più ampia. Nessun meccanismo aggiuntivo necessario, tolto dal backlog.
- **Nota**: attraversare un Bivio mentre si è dentro una catena innescata da un'altra casella (es. una `MoveForwardSquare` che porta esattamente su un punto intermedio di un Bivio con passi residui) è supportato e non crasha, ma interagisce col guardrail anti-loop come descritto sopra (limite noto, accettato).

### Meccanica "a catena" — invariata
- Dopo ogni spostamento si ricontrolla la casella di arrivo: vittoria se è l'ultima, altrimenti si applica l'effetto lì. Ora soggetta anche al guardrail anti-loop (sopra). Eccezioni alla catena invariate: `StartOverSquare` (commentata di proposito), `SnackSquare`/`DrinkSquare`/`ObligationSquare`/`CheckpointSquare` (non spostano il giocatore), `PortalSquare` (esplicito, "niente di niente").

### Obbligo — `ObligationSquare`, ridisegnata questa sessione

- **Prima**: placeholder che stampava una classifica di tutti i giocatori (mai stato coerente col nome "obbligo", segnalato esplicitamente come mockup).
- **Ora**: messaggio generico, **non nomina un bersaglio specifico** — deciso esplicitamente da Salame: "per ora resta generico, in futuro probabilmente non lo sarà più".
  ```java
  public String buildMessage(Player player, Game game) {
      return player.getName() + " deve far fare un obbligo ad un altro giocatore"
              + " (es. togliergli o dargli uno snack o un drink)";
  }
  ```
- Firma con **`Game` oltre a `Player`** — voluto esplicitamente da Salame in vista del futuro (quando l'obbligo dovrà scegliere un bersaglio specifico tra i giocatori in partita servirà accesso a `game.getPlayers()`); `game` non ancora usato nel corpo, riservato.
- `CheckpointSquare.buildMessage(Player player)`: **nessuna modifica** — confermato che il formato attuale (nome + "deve fare 3 tiri di creazione") era già quello voluto.

### addSnack / addDrink — nuovi metodi generici su `Game`

- Motivazione: l'obbligo (e in futuro il frontend, con un pulsante/sezione dedicata) deve poter modificare snack/drink di **un giocatore qualsiasi, in qualsiasi momento della partita**, non solo tramite l'atterraggio su una `SnackSquare`/`DrinkSquare`. Discusso esplicitamente: se un obbligo dice "dai 2 drink a tutti i giocatori", serve un modo di applicare l'effetto fuori dal flusso normale di movimento.
  ```java
  public void addSnack(Player player, int amount) {
      player.setSnack(Math.max(0, player.getSnack() + amount));
  }

  public void addDrink(Player player, int amount) {
      player.setDrink(Math.max(0, player.getDrink() + amount));
  }
  ```
- Stesso amount con segno già usato ovunque nel progetto (positivo = aggiungi, negativo = togli, clamp a 0) — coerenza con l'idioma esistente.
- `SnackSquare`/`DrinkSquare` **ora delegano** a questi metodi invece di duplicare la logica di clamp:
  ```java
  // SnackSquare.applyEffect
  game.addSnack(player, amount);
  // DrinkSquare.applyEffect
  game.addDrink(player, amount);
  ```
- **Decisione esplicita**: niente variante "per tutti i giocatori" dentro `Game` — chi chiama (es. il futuro FE) itera su `game.getPlayers()` e richiama il metodo per ognuno, se serve applicare l'effetto a tutti. `Game` sa solo modificare in modo sicuro un giocatore alla volta.
- Pensato per essere richiamato direttamente dal FE quando ci sarà (Salame: "ci sarà un pulsante o una sezione che aggiunge o toglie snack o drink dal/dai giocatore/i").
- Testato: accumulo positivo, clamp a 0 su amount negativo, richiamabile fuori da qualsiasi turno/movimento (su un player qualsiasi, in qualsiasi momento), più regressione completa (le caselle Snack/Drink via board si comportano esattamente come prima, ora passando dai metodi condivisi).

### Debiti tecnici noti (aggiornato)
- `playTurn()` non gestisce la pausa di `isAwaitingChoice()` (Bivio o Portale). **Confermato questa sessione** (Salame): è per design, `playTurn()` è pensato solo per board "semplici" senza elementi interattivi; le board con Bivio/Portale vanno pilotate con un ciclo custom che controlla `isAwaitingChoice()` (come fa `GameDemo5`). Resta aperto solo per quando si deciderà il flusso "vero" lato Spring/API (dove probabilmente servirà esporre lo stato di attesa al frontend) — non è un bug da correggere ora.
- Guardrail anti-loop: limite noto con pausa Bivio a metà catena (vedi sopra) — accettato.
- `Game.getLastRoll()` mostra solo l'ultimo tiro di dado, non gli step intermedi di una catena. **Deciso questa sessione** (Salame): "non penso servirà mai" — non verrà esteso, tolto dal backlog attivo.
- **Probabile commit consistente da fare**: il lavoro di questa sessione (builder di `Board`, chiusura `Game.players`, guardrail anti-loop, fermo obbligatorio backward simmetrico, `ObligationSquare`/`CheckpointSquare.buildMessage()`, `addSnack`/`addDrink`, `GameDemo5`) si aggiunge al lavoro delle sessioni precedenti (mai committato secondo l'ultimo dump) — ancora più probabile che serva un commit ora.

---

## 6. Codice sorgente — versione aggiornata e verificata

Tutti i file sottostanti sono stati compilati ed eseguiti con successo (Java 21) in sandbox. Regressione verificata questa sessione: builder (validazione posizioni, immutabilità liste, regole impostate correttamente, costruttore semplice ancora funzionante), `Game.players` chiusa (copia difensiva + immutabile), guardrail anti-loop (ping-pong 2 caselle, ciclo a 3 caselle, reset tra turni, catena normale invariata, catena+bivio non crasha), checkpoint backward simmetrico (fermo sul primo checkpoint incontrato, nessun clamp con regola disattivata, nessun blocco partendo da un checkpoint), `addSnack`/`addDrink` (accumulo, clamp, richiamabili in autonomia), `ObligationSquare` col nuovo messaggio — il tutto anche con 30 run randomici ripetuti su più board (inclusa la board completa di `GameDemo5`, con doppio checkpoint, bivio con ramo alternativo, portale singolo e con scelta, trappola ping-pong per il guardrail).

### `entity/Board.java`
```java
package entity;

import java.util.ArrayList;
import java.util.List;

public class Board {

    private final int finishPosition;
    private final List<Square> squares;
    private final boolean directionChoiceEnabled;
    private final boolean mandatoryCheckpointEnabled;

    public Board(int size) {
        if (size <= 0) {
            throw new IllegalArgumentException("Board size must be positive");
        }
        this.finishPosition = size;
        this.squares = defaultSquares(size);
        this.directionChoiceEnabled = false;
        this.mandatoryCheckpointEnabled = false;
    }

    private Board(int finishPosition, List<Square> squares,
            boolean directionChoiceEnabled, boolean mandatoryCheckpointEnabled) {
        this.finishPosition = finishPosition;
        this.squares = squares;
        this.directionChoiceEnabled = directionChoiceEnabled;
        this.mandatoryCheckpointEnabled = mandatoryCheckpointEnabled;
    }

    private static List<Square> defaultSquares(int size) {
        List<Square> result = new ArrayList<>();
        for (int position = 0; position <= size; position++) {
            result.add(new Square(position));
        }
        return List.copyOf(result);
    }

    public static Builder builder(int size) {
        return new Builder(size);
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

    public boolean isMandatoryCheckpointEnabled() {
        return mandatoryCheckpointEnabled;
    }

    public static class Builder {

        private final int finishPosition;
        private final List<Square> squares;
        private boolean directionChoiceEnabled;
        private boolean mandatoryCheckpointEnabled;

        private Builder(int size) {
            if (size <= 0) {
                throw new IllegalArgumentException("Board size must be positive");
            }
            this.finishPosition = size;
            this.squares = new ArrayList<>();
            for (int position = 0; position <= size; position++) {
                this.squares.add(new Square(position));
            }
            this.directionChoiceEnabled = false;
            this.mandatoryCheckpointEnabled = false;
        }

        public Builder square(int position, Square square) {
            if (position <= 0 || position >= finishPosition) {
                throw new IllegalArgumentException(
                        "Position must be between 1 and finishPosition - 1 (got " + position + ")");
            }
            squares.set(position, square);
            return this;
        }

        public Builder directionChoiceEnabled(boolean enabled) {
            this.directionChoiceEnabled = enabled;
            return this;
        }

        public Builder mandatoryCheckpointEnabled(boolean enabled) {
            this.mandatoryCheckpointEnabled = enabled;
            return this;
        }

        public Board build() {
            return new Board(finishPosition, List.copyOf(squares),
                    directionChoiceEnabled, mandatoryCheckpointEnabled);
        }
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
    private Player lastPlayerToMove;
    private Player awaitingPlayer;
    private List<Integer> awaitingOptions;
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
                movePlayerTo(player, currentPosition - 1);
                moveBackwardByInternal(player, remaining - 1, false);
                return;
            }

            movePlayerTo(player, nextPosition);
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

            int currentPosition = player.getPosition();
            Square currentSquare = board.getSquares().get(currentPosition);
            List<Integer> options = currentSquare.getPreviousPositions();

            if (options.size() > 1) {
                requestBivioChoice(player, options, remaining, false, respectCheckpoint);
                return;
            }

            int nextPosition = options.get(0);
            movePlayerTo(player, nextPosition);
            remaining--;

            // fermo obbligatorio simmetrico al forward: casella appena raggiunta, non quella di partenza
            Square nextSquare = board.getSquares().get(nextPosition);
            if (respectCheckpoint && nextSquare.isMandatoryStop()) {
                remaining = 0;
            }
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

        movePlayerTo(waitingPlayer, chosenPosition);

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
        boolean isChainStart = !chainInProgress;
        if (isChainStart) {
            chainInProgress = true;
            activatedPositions.clear();
        }

        try {
            if (player.getPosition() == board.getFinishPosition()) {
                winner = player;
                return;
            }

            int position = player.getPosition();
            if (!activatedPositions.add(position)) {
                // casella gia' attivata in questo turno di movimento: niente effetto, si comporta da casella base
                return;
            }

            Square square = board.getSquares().get(position);
            square.applyEffect(player, this);
        } finally {
            if (isChainStart) {
                chainInProgress = false;
            }
        }
    }

    public void teleportTo(Player player, int position) {
        movePlayerTo(player, position);
    }

    public void resetToStart(Player player) {
        movePlayerTo(player, player.getCheckpoint());
    }

    private void movePlayerTo(Player player, int position) {
        player.setPosition(position);
        movementTrace.add(position);
    }

    public void clearMovementTrace() {
        movementTrace.clear();
    }

    public List<Integer> getMovementTrace() {
        return List.copyOf(movementTrace);
    }

    public void updateCheckpoint(Player player) {
        player.setCheckpoint(player.getPosition());
    }

    public void addSnack(Player player, int amount) {
        player.setSnack(Math.max(0, player.getSnack() + amount));
    }

    public void addDrink(Player player, int amount) {
        player.setDrink(Math.max(0, player.getDrink() + amount));
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
        game.addSnack(player, amount);
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
        game.addDrink(player, amount);
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
        System.out.println(buildMessage(player, game));
    }

    public String buildMessage(Player player, Game game) {
        return player.getName() + " deve far fare un obbligo ad un altro giocatore"
                + " (es. togliergli o dargli uno snack o un drink)";
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

### `demo/GameDemo5.java`
*(nessun package. Demo corrente — sostituisce GameDemo2/3/4, non più compatibili con la Board immutabile. Interattiva da console: scelta di direzione ad ogni turno, risoluzione di Bivio/Portale con scelta. Board con 70 caselle, tutte le regole attive, pensata per mostrare insieme tutto il lavoro di questa sessione.)*
```java
package demo;

import java.util.List;
import java.util.Scanner;

import entity.Board;
import entity.Dice;
import entity.Game;
import entity.Player;
import entity.Square;
import entity.squares.BivioSquare;
import entity.squares.CheckpointSquare;
import entity.squares.DrinkSquare;
import entity.squares.MoveBackSquare;
import entity.squares.MoveForwardSquare;
import entity.squares.ObligationSquare;
import entity.squares.PortalSquare;
import entity.squares.RollAgainSquare;
import entity.squares.SnackSquare;
import entity.squares.StartOverSquare;

public class GameDemo5 {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        Board board = Board.builder(70)
                .directionChoiceEnabled(true)
                .mandatoryCheckpointEnabled(true)
                // tronco iniziale
                .square(3, new RollAgainSquare(3))
                .square(5, new SnackSquare(5, 3))
                .square(7, new DrinkSquare(7, 2))
                .square(9, new CheckpointSquare(9))        // checkpoint 1
                .square(11, new StartOverSquare(11))
                .square(13, new ObligationSquare(13))
                // primo bivio: ramo principale (15-19) o alternativo (30-34)
                .square(14, new BivioSquare(14, List.of(15, 30), List.of(13)))
                // ramo principale
                .square(15, new MoveForwardSquare(15, 3))  // atterra su 18 -> concatenazione
                .square(18, new CheckpointSquare(18))      // checkpoint 2
                .square(19, new BivioSquare(19, List.of(45), List.of(18)))
                // ramo alternativo (numeri liberi, il principale non ci passa mai)
                .square(30, new MoveBackSquare(30, 5))     // atterra su 25
                .square(32, new PortalSquare(32, List.of(60, 63)))
                .square(34, new BivioSquare(34, List.of(45), List.of(33)))
                // ricongiungimento
                .square(45, new BivioSquare(45, List.of(46), List.of(19, 34)))
                // tronco finale
                .square(48, new PortalSquare(48, List.of(53)))
                .square(53, new SnackSquare(53, -10))      // clamp a 0
                .square(55, new DrinkSquare(55, -10))      // clamp a 0
                .square(57, new MoveForwardSquare(57, 4))  // atterra su 61
                .square(61, new MoveBackSquare(61, 4))     // torna su 57 -> guardrail anti-loop
                .build();

        Player player1 = new Player("Gigi");
        Player player2 = new Player("Dodo");
        List<Player> players = List.of(player1, player2);

        Game game = new Game(board, players, new Dice());

        System.out.println("=== Demo rapida: addSnack/addDrink richiamabili in qualsiasi momento ===");
        System.out.println(player1.getName() + " iniziali -> snack: " + player1.getSnack()
                + " drink: " + player1.getDrink());
        game.addSnack(player1, 2);
        game.addDrink(player1, 1);
        System.out.println(player1.getName() + " dopo addSnack(+2)/addDrink(+1) -> snack: " + player1.getSnack()
                + " drink: " + player1.getDrink());
        game.addDrink(player1, -10);
        System.out.println(player1.getName() + " dopo addDrink(-10), clamp a 0 -> snack: " + player1.getSnack()
                + " drink: " + player1.getDrink());
        System.out.println();

        int currentPlayerIndex = 0;
        int turnCount = 0;
        int maxTurns = 500;

        while (game.getWinner() == null && turnCount < maxTurns) {
            if (currentPlayerIndex == 0) {
                turnCount++;
            }

            Player currentPlayer = players.get(currentPlayerIndex);

            System.out.println();
            System.out.println("=== Turno " + turnCount + " - " + currentPlayer.getName() + " ===");
            System.out.println("Posizione attuale: " + currentPlayer.getPosition()
                    + " (" + describeSquare(board, currentPlayer.getPosition()) + ")"
                    + " - snack: " + currentPlayer.getSnack()
                    + " drink: " + currentPlayer.getDrink()
                    + " checkpoint: " + currentPlayer.getCheckpoint());

            int startPosition = currentPlayer.getPosition();
            int rollValue = game.roll(currentPlayer);
            System.out.println("Tiro: " + rollValue);

            boolean forward = askDirection(scanner, currentPlayer);

            game.clearMovementTrace();
            game.moveByChosenDirection(currentPlayer, forward);

            while (game.isAwaitingChoice()) {
                resolveChoiceInteractively(game, currentPlayer, scanner);
            }

            printMoveSummary(game, board, currentPlayer, startPosition, forward);

            if (game.getWinner() == null) {
                currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
            }
        }

        scanner.close();

        System.out.println();
        if (game.getWinner() != null) {
            System.out.println("Winner: " + game.getWinner().getName());
        } else {
            System.out.println("No winner after " + turnCount + " turns");
        }
    }

    private static boolean askDirection(Scanner scanner, Player player) {
        while (true) {
            System.out.println(player.getName() + ", vuoi andare avanti (a) o indietro (i)? ");
            String line = scanner.nextLine().trim().toLowerCase();
            if (line.equals("a") || line.equals("avanti")) {
                return true;
            }
            if (line.equals("i") || line.equals("indietro")) {
                return false;
            }
            System.out.println("Risposta non valida, scrivi 'a' o 'i'.");
        }
    }

    private static void resolveChoiceInteractively(Game game, Player player, Scanner scanner) {
        List<Integer> options = game.getChoiceOptions();
        System.out.println(player.getName() + " deve scegliere una destinazione! Opzioni: " + options);

        Integer chosen = null;
        while (chosen == null) {
            System.out.println("Scegli una posizione tra " + options + ": ");
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

    private static void printMoveSummary(Game game, Board board, Player player, int startPosition, boolean forward) {
        List<Integer> trace = game.getMovementTrace();

        System.out.print("Direzione: " + (forward ? "avanti" : "indietro") + " | Percorso: " + startPosition);
        for (int position : trace) {
            System.out.print(" -> " + position + " (" + describeSquare(board, position) + ")");
        }
        System.out.println();

        System.out.println("Posizione finale: " + player.getPosition()
                + " - snack: " + player.getSnack()
                + " drink: " + player.getDrink()
                + " checkpoint: " + player.getCheckpoint());
    }

    private static String describeSquare(Board board, int position) {
        if (position == board.getFinishPosition()) {
            return "ARRIVO";
        }
        Square square = board.getSquares().get(position);
        return square.getClass().getSimpleName();
    }
}
```

**Nota sulla board di `GameDemo5`** (70 caselle, `directionChoiceEnabled` e `mandatoryCheckpointEnabled` entrambe attive):
- **9 / 18**: doppio checkpoint, per dimostrare il fermo backward simmetrico (ci si ferma al primo checkpoint incontrato, non necessariamente il proprio salvato).
- **14 → 15-19 (principale) / 30-34 (alternativo) → 45**: bivio con ramo alternativo su numeri liberi mai attraversati normalmente, ricongiungimento condiviso — stesso pattern delle sessioni precedenti.
- **32**: portale con scelta (uscite 60 o 63); **48**: portale singolo (teleport diretto su 53, nessuna catena).
- **57 ↔ 61**: coppia `MoveForwardSquare`/`MoveBackSquare` che si rimanderebbero all'infinito senza il guardrail — dimostra dal vivo il blocco della seconda attivazione.
- Un blocco iniziale nel `main` dimostra `addSnack`/`addDrink` richiamati direttamente su un player, fuori da qualsiasi turno.

**Artefatto extra consegnato questa sessione (non fa parte del codice sorgente)**: uno schema SVG della board di `GameDemo5` — schema logico dei collegamenti (nodi = caselle speciali, frecce = flusso reale, inclusa la trappola anti-loop e il ricongiungimento del bivio), non una griglia 1:1 delle 71 posizioni. Consegnato come file a parte, non ricostruito qui.

---

## 7. TODO / Prossimi passi (aggiornato)

### Prossimo passo pianificato
- **Integrazione Spring Boot 4.1.0** — include al suo interno il **factory pattern per le caselle** (JSON con attributo `type` → istanza `Square`), che non è più tracciato come voce standalone: fa parte del lavoro di integrazione.

### Rimandato a dopo la base BE+FE funzionante
- **Bivio con percorsi non riconvergenti** — per ora tutti i percorsi si ricongiungono prima del traguardo; in futuro percorsi che non si ricongiungono mai, col rimbalzo di fine tabellone che in quel caso cambierebbe solo il senso di marcia.
- **Board con più caselle di arrivo** — conseguenza diretta del punto sopra.

### Backlog aperto
- **Autenticazione (JWT vs sessione)** — ancora da decidere.
- **Valutare Electron** per le fasi successive, quando BE e FE saranno pronti.

### Deciso di non fare / rimosso dal backlog (per chiarezza storica — non voci attive)
- Altri tipi di caselle speciali oltre ai 10 attuali — non in programma per ora ("per ora non faremo altri tipi").
- `Game.getLastRoll()` mostra solo l'ultimo tiro di una catena — deciso che probabilmente non servirà mai, non verrà esteso.
- Portale con limitazioni oltre alla lista esplicita — già risolto: ogni istanza ha la propria lista esplicita di collegamenti, nessun meccanismo aggiuntivo necessario.
- Frontend Angular come voce di backlog a sé — ovviamente verrà fatto, non serve tracciarlo come promemoria separato (resta nel piano generale, sezione 3).

### Fatto in questa sessione (non più TODO)
- ~~Metodo di creazione board~~ → `Board.builder(size)`, pattern fluente, sostituisce il piazzamento manuale via `.set()`.
- ~~Chiusura liste mutabili~~ → `Board.getSquares()` e `Game.getPlayers()` ora entrambe immutabili.
- ~~Guardrail anti-loop~~ → `activatedPositions` + `chainInProgress` in `Game`, con limite noto sulla pausa Bivio a metà catena (accettato).
- ~~Fermo obbligatorio sul checkpoint per il backward~~ → ora simmetrico al forward (si ferma sulla prima casella `isMandatoryStop()` incontrata, non solo sul proprio checkpoint salvato).
- ~~`ObligationSquare.buildMessage()` / `CheckpointSquare.buildMessage()`~~ → Obligation ridisegnata (messaggio generico, firma con `Player` + `Game` per il futuro); Checkpoint confermata invariata.
- ~~Metodo per aggiungere/togliere snack o drink richiamabile in ogni momento~~ → `Game.addSnack`/`addDrink`, riusati anche da `SnackSquare`/`DrinkSquare`.
- ~~GameDemo5~~ → nuova demo interattiva, board da 70 caselle che dimostra tutto il lavoro di questa sessione insieme.

---

## 8. Decisioni "minori" ma da ricordare

- Traduzione "obbligo" → `ObligationSquare` (nome definitivo, invariato).
- Package reale: **`entity`** e **`entity.squares`**.
- Convenzione di visibilità: preferire scelte più ampie/future-proof di default, tranne quando serve vero incapsulamento.
- `resetToStart` ha mantenuto il nome anche se torna al checkpoint (non sempre a 0).
- `moveByChosenDirection` non lancia eccezione se la regola di direzione non è attiva — fallback silenzioso ad "avanti".
- Atterrare esattamente su un `BivioSquare` con 0 passi rimasti non forza una scelta — vale anche per il Portale (si attiva solo su atterraggio esatto, non attraversandolo).
- Il rimbalzo di fine tabellone non rispetta mai il checkpoint — asimmetria voluta, invariata.
- Le caselle di ricongiungimento di un bivio sono istanze della stessa classe `BivioSquare`.
- Costruendo board con bivi, attenzione alla numerazione: un percorso alternativo con caselle proprie deve usare numeri che il percorso principale non attraverserebbe mai di default, altrimenti loop di gameplay silenzioso (non un crash).
- **`Board(int size)` resta pubblico deliberatamente** anche dopo l'introduzione del builder — costo basso di renderlo privato più avanti (Spring o oltre), non prioritario finché non se ne sente il bisogno.
- **Board completamente immutabile dopo la costruzione**: niente setter per le regole, si impostano solo via builder.
- **`.square(position, square)`** valida `1 <= position <= finishPosition - 1`, lancia `IllegalArgumentException` fuori range.
- **`Game.players`** chiusa con `List.copyOf` nel costruttore, stessa filosofia di `Board.getSquares()`.
- **GameDemo2/GameDemo3/GameDemo4 non compilano più** (usavano `.set()` su una lista ora immutabile) — superate da `GameDemo5`, coerente con la filosofia "usa e getta" delle demo (sezione 2).
- **`activatedPositions`/`chainInProgress`** in `Game`: guardrail anti-loop, con limite noto e accettato sulla pausa Bivio a metà catena (sezione 5).
- **`ObligationSquare.buildMessage(Player, Game)`**: `game` non ancora usato, riservato per quando l'obbligo dovrà scegliere un bersaglio specifico.
- **`Game.addSnack`/`addDrink`**: nessuna variante "per tutti i giocatori" — il chiamante itera su `game.getPlayers()` se serve applicare l'effetto a tutti.
- SVG di schema della board creato per `GameDemo5`, consegnato come artefatto separato (non fa parte del codice sorgente del progetto) — schema logico dei collegamenti, non griglia 1:1 delle posizioni.
- **Le `GameDemoN` sono checkpoint usa-e-getta** (vedi sezione 2) — salvato anche nella memoria persistente di Claude per questo progetto, da non perdere tra una conversazione e l'altra.
