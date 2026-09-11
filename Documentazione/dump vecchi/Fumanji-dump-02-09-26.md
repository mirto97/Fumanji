# FUMANJI — Dump di Progetto

> Archivio esteso, non riassunto. Contiene tutto ciò che è stato deciso e scritto fino a questo punto (dump precedente `Fumanji-dump-24-08-26.md` + l'intera conversazione successiva, da GameDemo2 a GameDemo3). Da caricare a inizio di ogni nuova conversazione per riprendere il lavoro da qui.

---

## 1. Scopo e contesto

Sviluppo di una web application: variante del "Gioco dell'Oca" classico, con caselle speciali che attivano effetti vari (alcuni "stupefacenti"). Stack previsto: **Backend Spring + Frontend Angular**. Si è partiti dal backend, e in particolare dalla logica di dominio in **Java puro**, prima di introdurre Spring.

**Repository**: `https://github.com/mirto97/Fumanji.git` (privato)

---

## 2. Modalità di lavoro (regole di processo — vincolanti)

- **"IO SONO LA MENTE E TU SEI IL BRACCIO"**: l'utente decide, Claude scrive codice. Claude non deve pensare/decidere al posto dell'utente a meno che non venga esplicitamente richiesto. Consigli, correzioni e accorgimenti sono sempre ben accetti e vanno discussi insieme.
- Risposte **veloci e dirette**; approfondire solo se richiesto esplicitamente; **non ripetere** cose già scritte o decise.
- **Non scrivere codice finché non si è sicuri** di cosa si sta scrivendo. Se c'è un'ambiguità reale di design, si chiede prima di scrivere (vedi tutta la sezione 5 per esempi di quante volte è successo in questa sessione, specialmente per il Bivio).
- **Non supporre o dedurre nulla** a meno che non venga detto esplicitamente; in caso di dubbio, si verifica insieme il risultato ("controlliamo se è corretto").
- **Non inventare nulla** di propria iniziativa — chiedere, piuttosto.
- Nomi di classi/metodi/variabili sempre in **inglese**. Commenti nel codice sempre in **italiano**, asciutti e in stile "codice da progetto", e solo quando esplicitamente richiesti.
- Il codice va scritto **sempre inline in chat** (mai come file scaricabile) — **eccetto il dump di fine conversazione**, che è invece sempre un file `.md` scaricabile.
- **Quando si modifica codice già scritto, non riscrivere mai la classe intera** — mostrare solo i metodi/le righe esatte cambiate. Motivo: l'utente sta aggiungendo progressivamente propri commenti nel codice via IDE, e riscrivere intere classi cancellava quel lavoro. (Le classi **nuove di zecca**, invece, si possono mostrare per intero alla prima stesura.)
- Esiste sempre un file di progetto con **"...dump..."** nel nome, contenente tutto il lavoro fatto fino a quel momento. Se Claude non lo trova in una nuova chat, deve segnalarlo. Ogni nuova conversazione riparte da lì. A fine conversazione, Claude produce un dump aggiornato (questo documento), che contiene tutto il dump precedente **più** tutto ciò che è stato deciso/cambiato nella conversazione corrente. In caso di dubbio se includere o no qualcosa nel dump, **sempre meglio includerla**.
- Prassi consolidata: quando possibile, Claude compila ed esegue il codice nel proprio ambiente sandbox per verificarne la correttezza **prima** di presentarlo — in questa sessione è stato fatto sistematicamente per ogni singola modifica, incluse le regressioni sui test precedenti dopo ogni refactoring strutturale.
- **Nuova prassi osservata in questa sessione**: quando una richiesta rivela una portata strutturale molto più ampia di quanto sembrasse (es. il Bivio), Claude deve fermarsi e spiegare chiaramente l'impatto reale prima di procedere, piuttosto che minimizzare o iniziare a scrivere codice parziale.

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
| Autenticazione | Confermato: servono account utente (login/registrazione). Meccanismo (JWT vs sessione) **non ancora deciso** — discussione ancora in pausa |

---

## 4. Struttura cartelle e package

```
Fumanji/
├── BE/
│   └── model/                      (cartella organizzativa, NON fa parte del nome dei package Java)
│       ├── demo/                   (nessun package dichiarato — non serve)
│       │   ├── GameDemo2.java
│       │   └── GameDemo3.java
│       └── entity/                 (package: entity)
│           ├── Board.java
│           ├── Dice.java
│           ├── Game.java
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
- `GameDemo1.java` è stato **volutamente dimenticato/escluso** fin dal dump precedente: è superato da `GameDemo2.java` e non fa più parte del progetto.
- I metodi "di orchestrazione" di `Game` sono **public** per convenzione ormai consolidata: preferire scelte di visibilità/design più ampie e "future-proof" fin da subito, dato che il progetto crescerà molto. Eccezioni mirate quando serve vero incapsulamento (es. `Player.setPosition`/`setCheckpoint`, vedi sotto).

---

## 5. Decisioni di design e logica di dominio

### Board
- Campo `finishPosition`: numero dell'ultima casella/arrivo (la lista `squares` ha `finishPosition + 1` elementi, posizioni da 0 a `finishPosition` incluse).
- Un solo tabellone fisso per ora, pensato per essere estendibile.
- **Nuovo campo `directionChoiceEnabled`** (boolean, default `false`): attiva/disattiva la regola "scelta di direzione" su questa board. Soluzione **provvisoria** in attesa del vero metodo di creazione board — per ora si attiva con `board.setDirectionChoiceEnabled(true)` a mano, come si piazzano le caselle.
- Metodo dedicato per la creazione dei tabelloni: **ancora non costruito** (TODO prioritario, vedi sezione 8). Nel frattempo, tutte le regole della board (scelta direzione, in futuro anche checkpoint obbligatorio, ecc.) si configurano manualmente sull'oggetto `Board` appena creato, nelle demo.

### Dado
- 1 dado per turno, valori 1-6, `ThreadLocalRandom`. Invariato.

### Movimento e vittoria
- Il tiro di dado sposta in avanti; se supera l'ultima casella, rimbalzo classico (torna indietro della quantità in eccesso).
- Vittoria solo atterrando **esattamente** su `finishPosition`; partita finisce subito al primo vincitore; `playTurn()` a partita conclusa non fa nulla.
- **Importante — dalla Fase 2 di questa sessione, il movimento non è più un salto aritmetico istantaneo**: `moveForwardBy`/`moveBackwardBy` procedono **passo-passo** internamente (necessario per supportare il Bivio, che può richiedere una scelta a metà spostamento). Il comportamento esterno per i casi già esistenti (rimbalzo, clamp al checkpoint) è stato **verificato identico** a prima con test dedicati (vedi sezione "Bivio" più sotto per i dettagli tecnici e sezione 7 per il codice).
- **Asimmetria confermata e preservata**: il rimbalzo di fine tabellone **non rispetta mai il checkpoint** (può portare la posizione anche sotto il checkpoint) — comportamento aritmetico originale, replicato fedelmente passo-passo. Il clamp al checkpoint (`moveBackwardBy`), invece, si ferma di netto quando lo raggiunge, senza "rimbalzare" — i passi rimanenti vengono scartati.

### Giocatori e turni
- `Player` ha: `name` (String, final), `position` (int, **setter ora package-private**, non più public — vedi Debiti tecnici), `snack` (int, stackabile), `drink` (int, stackabile), **`checkpoint`** (int, nuovo in questa sessione — vedi sezione dedicata).
- Ordine turni = ordine di inserimento nella lista giocatori.
- `snack`/`drink` restano campi dedicati (niente Map/enum), invariato.

### Gerarchia delle caselle speciali (Square)
- Pattern: ereditarietà. `Square` concreta, campo `position`, metodo `applyEffect(Player, Game)` di default no-op.
- **Nuovo in questa sessione**: `Square` espone anche due metodi con comportamento di default, sullo stesso principio di `applyEffect` (comportamento sovrascrivibile dalle sottoclassi):
  ```java
  public List<Integer> getNextPositions() {
      return List.of(position + 1);
  }

  public List<Integer> getPreviousPositions() {
      return List.of(position - 1);
  }
  ```
  Servono al motore di movimento per sapere "qual è la prossima casella" senza dover fare aritmetica diretta — la stragrande maggioranza delle caselle eredita il comportamento di default (posizione ±1); solo `BivioSquare` lo sovrascrive con liste di più elementi.

- **Tipi implementati (10 totali — erano 7 nel dump precedente, +3 in questa sessione: `CheckpointSquare`, `PortalSquare`, `BivioSquare`)**:
  1. `StartOverSquare` — ora chiama `game.resetToStart(player)`, che riporta al **checkpoint** del giocatore (non più sempre a 0, vedi sezione Checkpoint). Non fa catena (chiamata a `landOnCurrentSquare` commentata, invariato dal dump precedente).
  2. `MoveBackSquare(position, steps)` — indietro di `steps`, clamp al **checkpoint** (non più a 0 fisso, da quando esiste il checkpoint).
  3. `MoveForwardSquare(position, steps)` — avanti di `steps`, stesso rimbalzo classico del dado.
  4. `RollAgainSquare` — rilancio immediato dello stesso giocatore.
  5. `SnackSquare(position, amount)` — modifica snack, clamp a 0, riusata per "aggiungere"/"togliere" con amount negativo.
  6. `DrinkSquare(position, amount)` — come sopra per drink.
  7. `ObligationSquare(position)` — `buildMessage(Game)` ritorna una String (lista giocatori e posizioni), stampata su console come placeholder. Ancora mockup, in attesa di revisione futura.
  8. **`CheckpointSquare(position)`** (NUOVO) — vedi sezione dedicata.
  9. **`PortalSquare(position, linkedPosition)`** (NUOVO) — vedi sezione dedicata.
  10. **`BivioSquare(position, nextPositions, previousPositions)`** (NUOVO, il pezzo più grande di questa sessione) — vedi sezione dedicata.

### Meccanica "a catena" — **motore a coda** (rifattorizzazione strutturale di `Game`)

**Perché è cambiato**: con l'introduzione del Bivio, un movimento può dover "mettersi in pausa" a metà (in attesa che il giocatore scelga quale ramo prendere) e poi "riprendere" più tardi. In Java non esistono continuazioni: una volta che un metodo ritorna, il contesto delle chiamate annidate che lo avevano invocato è perso per sempre. La vecchia implementazione (`moveForwardBy` → `landOnCurrentSquare` → `square.applyEffect` → di nuovo `moveForwardBy`, ricorsivamente) non permetteva di "risalire" una catena di chiamate già interrotta.

**Soluzione**: sostituita la ricorsione diretta con una **coda di azioni pendenti** processata da un ciclo centrale:

```java
private final Deque<Runnable> pendingActions = new ArrayDeque<>();
private boolean processingQueue;

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
```

- `Runnable` = un pezzo di codice "congelato", non eseguito finché qualcuno non chiama `.run()`.
- `Deque`/`ArrayDeque` = coda FIFO (primo che entra, primo che esce) — qui usata solo come coda semplice (`addLast`/`pollFirst`).
- `processingQueue` = guardia anti-rientro: se il ciclo è già in corso, una chiamata annidata a `processQueue()` si limita ad accodare e ritornare subito, senza avviare un secondo ciclo — è così che la ricorsione si "appiattisce" in iterazione.
- Ogni metodo che prima chiamava direttamente il prossimo passo della catena ora **accoda** un `Runnable` per quel passo, invece di eseguirlo subito. Il ciclo centrale (`processQueue`) lo smaltisce da solo, uno alla volta, finché la coda non si svuota.
- Verificato con un test di catena profonda (5 `MoveForwardSquare` consecutive) e con tutta la regressione dei test precedenti: **comportamento esterno identico a prima**, zero cambi visibili.
- **Nota per Salame**: ha esplicitamente detto che si prenderà del tempo per studiare e commentarsi da sé questa parte (in particolare `Deque<Runnable>`, `ArrayDeque`, `processingQueue`) prima di proseguire — non dare per scontato che sia già chiara, ricontrollare insieme se necessario.

### Checkpoint — dettagli

Nato da un'esigenza pratica: rendere `Player.setPosition` package-private (invece di public) richiedeva un modo per `StartOverSquare` di riportare il giocatore all'inizio senza toccare `Player` direttamente — ma "l'inizio" doveva già prevedere il futuro checkpoint (altrimenti si sarebbe dovuto rifare la stessa modifica due volte).

- `Player.checkpoint` (int, default 0 = partenza), setter **package-private**, con **clamp a 0** (`Math.max(0, checkpoint)`), stesso pattern del clamp già usato in `SnackSquare`/`DrinkSquare`.
- **Aggiornamento sempre all'ultimo raggiunto**, non al più avanzato mai toccato (decisione esplicita, confermata con un esempio concreto: se un giocatore raggiunge un checkpoint a 10 e poi, tramite un futuro bivio, torna a un checkpoint a 5, il valore diventa 5, non resta 10).
- `Game.updateCheckpoint(player)` → `player.setCheckpoint(player.getPosition())`.
- `Game.resetToStart(player)` → `player.setPosition(player.getCheckpoint())` (il nome del metodo è stato **mantenuto** anche se ora non torna più sempre a 0 — confermato con Salame, nessun rename necessario per ora).
- `Game.moveBackwardBy` clampa a `player.getCheckpoint()` invece che a 0 fisso.
- `CheckpointSquare.applyEffect`: chiama `updateCheckpoint` poi stampa `buildMessage(player)` su console. **Non fa catena** (non sposta il giocatore).
- `buildMessage(Player player)`: messaggio **mockup**, testo fisso:
  ```java
  return player.getName() + " ha raggiunto il checkpoint! Quindi deve fare 3 tiri di creazione";
  ```
  Da rivedere quando si costruirà il metodo generale per i messaggi (stesso discorso di `ObligationSquare`).
- **TODO collegato, non ancora implementato**: regola configurabile a livello di board — "checkpoint obbligatorio", dove il giocatore deve fermarsi lì anche se dovrebbe muoversi ancora (invece di continuare il resto del tiro). Categorizzata insieme alla scelta di direzione come esempio di "regola della board" da progettare col futuro metodo di creazione board.

### Direction choice (scelta di direzione) — dettagli

Richiesta esplicita: su alcune board (quelle "più avanzate", pensate per il gioco di strategia) il giocatore, **dopo** aver visto il risultato del dado, può scegliere se applicarlo in avanti o all'indietro. Requisito vincolante ribadito più volte: **prima si mostra il risultato, poi si sceglie** — questo impone di separare "tirare il dado" da "applicare il movimento", altrimenti non si potrebbe mostrare il numero prima della scelta.

- `Game.roll(player)`: tira il dado, aggiorna `lastRoll`/`lastPlayerToMove`, **non muove nessuno**, ritorna il valore.
- `Game.rollAndMove(player)`: comportamento pubblico invariato, ma internamente ora chiama `roll(player)` e poi `moveForwardBy(player, rollValue)` (niente più duplicazione della chiamata al dado).
- `Game.moveByChosenDirection(player, forward)`:
  ```java
  public void moveByChosenDirection(Player player, boolean forward) {
      // se non specificato, va avanti
      if (forward || !board.isDirectionChoiceEnabled()) {
          moveForwardBy(player, lastRoll);
      } else {
          moveBackwardBy(player, lastRoll);
      }
  }
  ```
  Usa `lastRoll` (il valore già mostrato), **non ritira il dado**. Se la board non ha la regola attiva, **fallback silenzioso ad avanti**, qualunque sia `forward` — **nessuna eccezione**: una prima versione lanciava `IllegalStateException` in quel caso, poi sostituita esplicitamente da Salame con questo fallback.
- `playTurn()` **non tocca** questo meccanismo — resta il flusso automatico per le board classiche. Le board con la regola attiva vanno pilotate a mano con `roll()` + `moveByChosenDirection()` (vedi anche il discorso analogo per il Bivio più sotto).
- **TODO aperto**: integrare questa regola nel flusso del futuro metodo di creazione board.

### Portal — dettagli

- `PortalSquare(position, linkedPosition)`: destinazione fissa.
- Bidirezionalità ottenuta **semplicemente piazzando due istanze che si linkano a vicenda** (`PortalSquare(5, 15)` e `PortalSquare(15, 5)`) — nessun meccanismo speciale in codice.
- `Game.teleportTo(player, position)`: `player.setPosition(position)`, **nessuna catena, nessun controllo vittoria** — decisione esplicita ("niente di niente"). Motivo del "niente controllo vittoria": qualunque controllo dopo il teletrasporto passerebbe di nuovo da `landOnCurrentSquare`, che per due portali collegati a vicenda ("viceversa") creerebbe un loop infinito garantito (il guardrail anti-loop generale non è ancora costruito).
- **Limite noto e accettato**: se un portale porta esattamente su `finishPosition`, il giocatore **non vince** — responsabilità del design del tabellone evitarlo, stesso principio già usato per altri edge case del progetto (es. la catena non innescata da `StartOverSquare`).
- **TODO differito**: variante con **uscita scelta dal giocatore** (tra più portali collegati, con "determinate limitazioni" ancora da definire) — legata alle future regole della board.

### Bivio — dettagli (il pezzo più grande di questa sessione)

**Percorso di design** (per capire perché è fatto così, non solo cosa fa): inizialmente sembrava una semplice casella con teletrasporto multiplo; è emerso che Salame intendeva **percorsi alternativi veri**, con caselle proprie, non solo una destinazione scelta. Questo ha richiesto di ripensare il modello di movimento da zero. Punti chiave emersi durante la discussione (in ordine):

1. Il tabellone resta una `List<Square>` indicizzata (non un grafo vero e proprio) — le caselle dei percorsi alternativi occupano semplicemente altri numeri della stessa lista.
2. La scelta scatta **anche solo attraversando** il bivio a metà mossa, non serve atterrarci esattamente sopra (deciso esplicitamente da Salame con un esempio concreto).
3. Le caselle di **ricongiungimento** sono **anche loro** `BivioSquare` — stessa classe, semplicemente con più elementi in `getPreviousPositions()` invece che in `getNextPositions()` (confermato con un disegno fatto a mano da Salame, caricato in chat: le lettere "B" di ricongiungimento nel disegno sono strutturalmente identiche a quelle di diramazione).
4. Per ora tutti i percorsi si ricongiungono prima del traguardo (vedi TODO per le eccezioni future).
5. Il movimento non può più essere un salto aritmetico istantaneo: deve avanzare **passo-passo**, controllando ad ogni passo se la casella corrente offre più di un'opzione nella direzione di marcia.
6. La pausa/ripresa deve funzionare **anche dentro le catene** (non solo sul tiro di dado diretto) — decisione esplicita di Salame nonostante la complessità implicata, da cui è nata la necessità del motore a coda (vedi sezione dedicata sopra).

**Modello dati**:
```java
public class BivioSquare extends Square {

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
- **Non sovrascrive `applyEffect`**: la scelta del percorso è parte del motore di movimento, non un effetto di casella. **Nessun messaggio** (deciso esplicitamente — "no messaggio per ora").
- Una `BivioSquare` con una sola opzione in una lista (es. solo `next`, con `previous` a un solo elemento) si comporta come una casella normale in quella direzione — usato di proposito in `GameDemo3` come "redirect" a senso unico (vedi sotto), senza che questo forzi mai una scelta (la pausa scatta solo se `options.size() > 1`).

**Motore di movimento (`Game`)** — sostituisce l'aritmetica diretta di `moveForwardBy`/`moveBackwardBy`:
```java
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
    int nextRemaining = remainingSteps - 1;
    enqueue(() -> stepForward(player, nextRemaining));
}

public void moveBackwardBy(Player player, int steps) {
    enqueue(() -> stepBackwardInternal(player, steps, true));
    processQueue();
}

private void stepBackwardInternal(Player player, int remainingSteps, boolean respectCheckpoint) {
    if (remainingSteps == 0) {
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
```

**API di pausa/ripresa**:
```java
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
```

**Comportamenti confermati con test dedicati (non ovvi, da ricordare)**:
- **Atterrare esattamente su un bivio con 0 passi rimasti NON forza una scelta** — il controllo delle opzioni avviene solo se serve *continuare* oltre (il check `remainingSteps == 0` viene prima del check delle opzioni). La scelta si presenterà più avanti, alla prossima mossa che prova a lasciare quella casella. Interpretazione di Claude, non esplicitamente confermata parola per parola da Salame — da ricontrollare se in futuro si nota un comportamento indesiderato.
- Il **rimbalzo di fine tabellone ignora il checkpoint** anche passando per un bivio nel mezzo — stessa asimmetria di sempre, verificata esplicitamente con un test che fa rimbalzare un giocatore sotto il proprio checkpoint.
- Il **clamp al checkpoint non riflette** (si ferma di netto, niente rimbalzo all'indietro).
- **Bivi annidati** (un bivio dentro il "ramo principale" di un altro bivio) funzionano senza codice dedicato, grazie al motore a coda — testato esplicitamente con due bivi in sequenza nella stessa mossa.
- `chooseBivioPath` valida che il `player` passato corrisponda a quello effettivamente in attesa (`IllegalArgumentException` altrimenti) — bug trovato e corretto durante lo sviluppo (il parametro inizialmente non veniva controllato).

**Gotcha di numerazione scoperto testando `GameDemo3`** (importante per costruire board con bivi in futuro): dato che `Board` resta una lista indicizzata con le caselle normali che di default vanno a `posizione + 1`, **un percorso alternativo con proprie caselle deve usare numeri che il percorso principale non attraverserebbe mai per conto suo**. Se non si sta attenti, si crea una collisione silenziosa (non un crash — il gioco entra in un loop di gameplay, i giocatori rimbalzano tra due punti senza mai avanzare). Soluzione usata: il percorso principale, appena prima del ricongiungimento, ha una `BivioSquare` con **una sola opzione** in `next` (quindi non forza mai una scelta) che fa da "redirect esplicito" dritto al ricongiungimento — questo libera i numeri intermedi per l'uso esclusivo del percorso alternativo. Vedi il tabellone di `GameDemo3` in sezione 7 per l'esempio concreto, corretto proprio a partire da questo errore.

**`playTurn()` non sa nulla della pausa del bivio**: se usato su una board con bivi, farebbe avanzare il turno al giocatore successivo anche a scelta ancora in sospeso. Le board con bivi vanno pilotate con un ciclo custom (tira, muovi, controlla `isAwaitingBivioChoice()`, risolvi, **solo dopo** passa al giocatore successivo) — vedi `GameDemo3`. Segnalato come cosa da rivedere quando si integrerà tutto nel flusso reale (Spring/API).

### GameDemo3 — dimostrazione interattiva del Bivio

- Nessun package, come le altre demo.
- **Ciclo di turno custom**, non usa `playTurn()` (vedi sopra il perché).
- Bivio **interattivo**: usa `java.util.Scanner`, chiede a console quale posizione scegliere tra le opzioni disponibili (`game.getBivioOptions()`), valida l'input (numero valido e tra le opzioni), poi chiama `game.chooseBivioPath(player, scelta)`. Il controllo `while (game.isAwaitingBivioChoice())` gestisce anche bivi annidati nella stessa mossa.
- **`turnCount` conta i giri completi** (tutti i giocatori si sono mossi una volta), non le singole mosse — scelta esplicita di Salame, diversa da `GameDemo2` (dove contava ogni mossa singola). Si incrementa solo quando si ricomincia dal primo giocatore (`currentPlayerIndex == 0`).
- **Tabellone** (dopo la correzione del gotcha di numerazione descritto sopra): bivio in 5 con **due percorsi veri**, non un salto secco:
  - Percorso principale: 6, 7, 8 (`CheckpointSquare`), 9 (`BivioSquare` redirect a senso unico verso 20).
  - Percorso alternativo: 10, 11 (`ObligationSquare`), 12 (`BivioSquare` redirect a senso unico verso 20).
  - Ricongiungimento in 20 (`BivioSquare`, riceve da 9 e da 12; tornando indietro da lì si sceglie di nuovo tra i due rami).
  - Tronco comune dal ricongiungimento (21) fino al traguardo (30).
- Entrambi i percorsi verificati con simulazioni (scegliendo sempre l'opzione principale e sempre quella alternativa): checkpoint e obbligo scattano correttamente solo sul proprio percorso, nessuna collisione, la partita arriva regolarmente a un vincitore in entrambi i casi.
- **Errore corretto durante lo sviluppo**: una prima versione usava un salto diretto (5 → 15 in un solo balzo) invece di un vero percorso — Salame ha fatto notare che non era quello che intendeva ("non è una scorciatoia, è proprio un'altra strada... con altre caselle"). Corretto con il modello a due percorsi paralleli sopra descritto.
- Per provarla dal vivo serve lanciarla su un vero terminale (in chat può essere solo simulata con input pre-scritto via pipe).
- **Nota su materiale di supporto**: durante la discussione sul Bivio sono stati usati due disegni SVG generati da Claude in chat per chiarire la topologia (uno inizialmente sbagliato, con il salto secco poi corretto; uno aggiornato con i due percorsi veri) — sono solo stati di supporto alla conversazione, non fanno parte del codice del progetto e non serve conservarli.

### Meccanica "a catena" — regole generali (invariate dal dump precedente, ora attraversate anche dal motore a coda)
- Dopo ogni spostamento (tiro di dado o casella speciale), si ricontrolla la casella di arrivo: vittoria se è l'ultima, altrimenti si applica l'effetto della casella lì.
- Catena senza limite per ora (guardrail anti-loop ancora in backlog).
- **Eccezioni alla catena, tutte confermate**: `StartOverSquare` (commentata di proposito), `SnackSquare`/`DrinkSquare`/`ObligationSquare`/`CheckpointSquare` (non spostano il giocatore), `PortalSquare` (deciso esplicitamente, "niente di niente").

### Debiti tecnici noti
- **RISOLTO in questa sessione**: `Player.setPosition` è ora **package-private** (era public). L'unico punto che lo usava dall'esterno del package (`StartOverSquare`) ora passa da `game.resetToStart(player)`.
- **RISOLTO in questa sessione**: `Game`, nel costruttore, ora valida `board` e `dice` non-null (`IllegalArgumentException` se nulli), oltre alla validazione già esistente sui `players`.
- **ANCORA APERTO**: `Board.getSquares()` e `Game.getPlayers()` espongono ancora le liste interne mutabili direttamente. Deliberatamente rimandato, legato al futuro metodo di creazione board — le demo sfruttano tuttora questa mutabilità per piazzare le caselle a mano (`board.getSquares().set(pos, new XSquare(...))`).
- **NUOVO, da tenere d'occhio**: `playTurn()` non gestisce la pausa del Bivio (vedi sopra) — non è un bug in senso stretto (nessuna board attuale con bivio usa `playTurn()`), ma va risolto quando si integrerà tutto nel flusso reale.

---

## 6. Codice sorgente — versione aggiornata e verificata

Tutti i file sottostanti sono stati compilati ed eseguiti con successo (Java 21) in questa sessione, inclusi test mirati su: checkpoint (aggiornamento, clamp a 0, `resetToStart`), portale (bidirezionalità, nessuna catena, edge case sul traguardo), scelta di direzione (avanti/indietro, fallback senza eccezione), refactoring del motore a coda (catena profonda di 5 caselle, zero regressioni su tutti i test precedenti), Bivio (attraversamento a metà mossa, atterraggio esatto senza scelta forzata, ricongiungimento all'indietro, bivi annidati, rimbalzo che ignora il checkpoint, validazione del giocatore in `chooseBivioPath`), e infine `GameDemo3` completa su entrambi i percorsi.

### `entity/Board.java`
```java
package entity;

import java.util.ArrayList;
import java.util.List;

public class Board {

    private final int finishPosition;
    private final List<Square> squares;
    private boolean directionChoiceEnabled;

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
}
```

### `entity/Game.java`
```java
package entity;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

public class Game {

    private final Board board;
    private final List<Player> players;
    private final Dice dice;
    private int currentPlayerIndex;
    private Player winner;
    private int lastRoll;
    private Player lastPlayerToMove;
    private final Deque<Runnable> pendingActions = new ArrayDeque<>();
    private boolean processingQueue;
    private Player awaitingPlayer;
    private int awaitingRemainingSteps;
    private boolean awaitingForward;
    private boolean awaitingRespectCheckpoint;
    private List<Integer> awaitingOptions;

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
        int nextRemaining = remainingSteps - 1;
        enqueue(() -> stepForward(player, nextRemaining));
    }

    public void moveBackwardBy(Player player, int steps) {
        enqueue(() -> stepBackwardInternal(player, steps, true));
        processQueue();
    }

    private void stepBackwardInternal(Player player, int remainingSteps, boolean respectCheckpoint) {
        if (remainingSteps == 0) {
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
}
```

### `entity/squares/PortalSquare.java`
```java
package entity.squares;

import entity.Game;
import entity.Player;
import entity.Square;

public class PortalSquare extends Square {

    private final int linkedPosition;

    public PortalSquare(int position, int linkedPosition) {
        super(position);
        this.linkedPosition = linkedPosition;
    }

    @Override
    public void applyEffect(Player player, Game game) {
        game.teleportTo(player, linkedPosition);
    }
}
```

### `entity/squares/BivioSquare.java`
```java
package entity.squares;

import java.util.List;

import entity.Square;

public class BivioSquare extends Square {

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
*(nessun package. Invariata dal dump precedente.)*
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
*(nessun package. Nuova in questa sessione — dimostrazione interattiva del Bivio, vedi sezione 5 per tutti i dettagli.)*
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

        // Percorso alternativo: 3 caselle proprie (numeri liberi, il principale non ci passa mai), poi redirect a 20
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

            while (game.isAwaitingBivioChoice()) {
                resolveBivioChoice(game, currentPlayer, scanner);
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

    private static void resolveBivioChoice(Game game, Player player, Scanner scanner) {
        List<Integer> options = game.getBivioOptions();
        System.out.println(player.getName() + " e' a un bivio! Opzioni: " + options);

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

        game.chooseBivioPath(player, chosen);
    }
}
```

**Nota**: `GameDemo1.java` non fa più parte del progetto (escluso fin dal dump precedente, superato da `GameDemo2`).

---

## 7. TODO / Prossimi passi (aggiornato, con priorità)

### Alta priorità
1. **Metodo di creazione board** — comprende ormai: piazzamento caselle (sostituendo i `.set()` manuali delle demo), le **"regole della board"** configurabili (categoria emersa in questa sessione: finora ne fanno parte la scelta di direzione e il futuro checkpoint obbligatorio — probabilmente altre se ne aggiungeranno), e la chiusura delle liste mutabili di `getSquares()`/`getPlayers()`.
2. **Checkpoint obbligatorio** — regola della board: il giocatore si deve fermare sul checkpoint anche se dovrebbe muoversi ancora. Da progettare insieme al punto 1.
3. **Portale con uscita scelta dal giocatore** — variante avanzata del portale, con "determinate limitazioni" ancora da definire, legata alle regole della board.
4. **Bivio con percorsi non riconvergenti** — per ora tutti i percorsi si ricongiungono prima del traguardo; in futuro si vuole poter creare percorsi che non si ricongiungono mai, con il rimbalzo di fine tabellone che in quel caso cambierebbe solo il senso di marcia (non più un vero "tornare indietro").
5. **Board con più caselle di arrivo** — conseguenza diretta del punto 4 (se i percorsi non si ricongiungono, potrebbero portare a traguardi diversi).

### Backlog (nessun ordine specifico)
- Altri tipi di caselle speciali oltre ai 10 attuali.
- **Guardrail anti-loop** per catene di caselle speciali che si rimbalzano a vicenda all'infinito — esplicitamente rimandato dall'inizio del progetto.
- `ObligationSquare.buildMessage()` — ancora mockup, attese modifiche/overload futuri.
- `CheckpointSquare.buildMessage()` — ancora mockup (testo fisso "ha raggiunto il checkpoint! Quindi deve fare 3 tiri di creazione").
- `Game.getLastRoll()` — mostra solo l'ultimo tiro di una catena, non gli step intermedi.
- **`playTurn()` non gestisce la pausa del Bivio** — da rivedere quando si integra tutto nel flusso reale (probabilmente insieme al punto 1).
- Integrazione Spring Boot 4.1.0 — non ancora iniziata.
- Autenticazione utenti (JWT vs sessione) — non ancora decisa.
- Frontend Angular — non ancora iniziato.

### Fatto in questa sessione (non più TODO)
- ~~Checkpoint (nuova casella speciale)~~ → `CheckpointSquare`, `Player.checkpoint`, `Game.updateCheckpoint`, `resetToStart` aggiornato.
- ~~`Player.setPosition` public~~ → ora package-private.
- ~~Validazione null nel costruttore di `Game`~~ → fatta.
- ~~Scelta di direzione (avanti/indietro)~~ → `Board.directionChoiceEnabled`, `Game.roll()`, `Game.moveByChosenDirection()`.
- ~~Casella "Portale"~~ → `PortalSquare`, bidirezionale, destinazione fissa (variante con scelta rimane in backlog, vedi sopra).
- ~~Casella "Bivio"~~ → `BivioSquare`, motore a coda in `Game`, pausa/ripresa, `GameDemo3`.

---

## 8. Decisioni "minori" ma da ricordare

- Traduzione "obbligo" → `ObligationSquare` (nome definitivo, dal dump precedente).
- Package reale: **`entity`** e **`entity.squares`**.
- Convenzione di visibilità: preferire scelte più ampie/future-proof di default, tranne quando serve vero incapsulamento (vedi `Player.setPosition`/`setCheckpoint`, entrambi package-private).
- `resetToStart` ha **mantenuto il nome** anche se ora torna al checkpoint (non sempre a 0) — confermato esplicitamente con Salame, nessun rename necessario.
- `moveByChosenDirection` **non lancia eccezione** se chiamato su una board senza la regola di direzione attiva — fa fallback silenzioso ad "avanti". Una versione precedente lanciava `IllegalStateException`, sostituita esplicitamente da Salame.
- Atterrare esattamente su un `BivioSquare` con 0 passi rimasti **non forza una scelta** — la scelta serve solo per continuare oltre (interpretazione di Claude, non ancora messa alla prova da Salame — ricontrollare se emergono dubbi).
- Il rimbalzo di fine tabellone **non rispetta mai il checkpoint** — asimmetria voluta fin da quando esiste il checkpoint, preservata esplicitamente durante il refactoring del motore a coda.
- Le caselle di ricongiungimento di un bivio sono **istanze della stessa classe `BivioSquare`**, non una classe separata — la differenza è solo in quale lista (`next`/`previous`) ha più di un elemento.
- Costruendo board con bivi, **attenzione alla numerazione**: un percorso alternativo con caselle proprie deve usare numeri che il percorso principale non attraverserebbe mai di default — altrimenti si crea un loop di gameplay silenzioso (non un crash). Vedi il gotcha descritto per `GameDemo3` in sezione 5.
- Salame prenderà del tempo per **studiare e commentarsi personalmente via IDE** il codice del motore a coda (`Deque<Runnable>`, `ArrayDeque`, `processingQueue`) prima di continuare a lavorarci sopra — non dare per scontato che sia già tutto chiaro in una conversazione futura, ricontrollare se necessario.
- Durante questa sessione è stato dato un messaggio di commit intermedio ("Add checkpoint square and direction choice rule"), **prima** di Portale, Bivio e `GameDemo3` — probabile che serva un secondo commit per il lavoro successivo a quel punto.
