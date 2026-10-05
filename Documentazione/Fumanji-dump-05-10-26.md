# FUMANJI — Dump di sessione (delta)

> **Data**: 05/10/2026 (Europe/Rome). La sessione è a cavallo tra il 04/10 e il 05/10.
> **Natura del file**: contiene **solo** ciò che è stato fatto/deciso in questa conversazione, con la **versione finale** di ogni file toccato. Va applicato **sopra** il main dump (`Fumanji-main-dump_31-09-26_0443.md`, che arriva fino al 30-09-26), il delta del 01-10 (`Fumanji-dump-01-10-26.md`) e il delta del 03-10 (`Fumanji-dump-03-10-26.md`): resta valido tutto ciò che qui non compare, **tranne** quanto elencato nella sezione 2 ("Correzioni"). L'unione la fa l'utente a parte.
> Dump snello: passaggi superati e cose annullate nella sessione non compaiono (es. le due letture iniziali dei bivi come direzionali, la strada A scartata, le strade B e C del click sulle caselle evidenziate). Archivio esteso, non riassunto: il codice è quello **finale**.
> **Come sono scritti i file**: per `Game.java`, `GameControllerIT.java` e `StartOverSquare.java` sono riportati solo i **membri** toccati (il resto è INVARIATO). Gli altri file sono riportati per intero; quelli che in chat erano stati dati a frammenti (`Board`, i file del tabellone, `game-state-response.ts`, `GameStateAssembler`, `GameStateResponse`, `BoardResponseAssembler`, `BoardAssemblerIT`, `game-screen.ts`) sono **ricostruiti applicando i frammenti alla versione dei dump precedenti**: eventuali commenti personali aggiunti in locale possono differire. `game-screen.html` è invece quello incollato dall'utente.
> **Verifiche**: il dominio (motore) è stato verificato in sandbox (sezione 9); la parte Spring, i test di integrazione e il frontend li ha provati l'utente in locale.

## Legenda degli stati
Ogni elemento è marcato con uno di questi stati rispetto al main dump + delta 01-10 + delta 03-10:
- **NUOVO**: non esisteva.
- **MODIFICATO**: esisteva ed è cambiato; è riportata la versione finale.
- **RIMOSSO**: non esiste più.
- **INVARIATO**: esiste ancora così com'era, riportato solo quando potrebbe sembrare toccato.

---

## 1. Cosa è stato fatto (indice)

- **Scelte di bivio e portale sul tabellone**: le caselle tra cui scegliere si evidenziano; cliccandone una compare il dettaglio con il pulsante **"Conferma direzione"**, che fa la scelta (strada C).
- **Passi rimasti al bivio**: l'API espone `choiceRemainingSteps` (esclusi i portali) e la schermata di gioco mostra "Passi rimasti: N".
- **Bug dei bivi, risolto**: andando indietro verso un bivio si perdevano passi e non si poteva scegliere; il bivio 70 non faceva scegliere. Il motore passa da un modello direzionale a un modello **a incrocio**: arrivando su un bivio a metà mossa si può andare in tutte le sue strade tranne quella da cui si arriva. I `previous` ora si ricavano dal grafo del tabellone.
- **Messaggio dello StartOver**: modificato dall'utente (testo più breve); test aggiornato.
- **Verifica del motore in sandbox**: regressione su 3000 partite casuali (vecchio vs nuovo motore) e 47 controlli di scenario sulla topologia della board OG.
- **Annotati nei TODO**: finestrella per aumentare/diminuire snack e drink di ogni giocatore; design della pagina di gioco (layout provvisorio).

---

## 2. Correzioni ai dump precedenti (cosa oggi è sbagliato lì)

1. **Meccanismo del bivio (main dump, sezione 5, "Caselle interattive")**: la scelta non scatta più perché la lista direzionale `next`/`previous` del `BivioSquare` ha più di un elemento. Scatta per **qualsiasi casella** che, tolta la casella da cui si arriva, ha più di un'uscita. Il motore non guarda più `InteractiveSquare`/`BivioSquare` per decidere se c'è una scelta. Il Portale è **INVARIATO** (solo atterraggio esatto, `requestChoice`).
2. **Bivio "di instradamento" (main dump, sezione 5: "un solo elemento in `nextPositions` non fa scattare nessuna scelta")**: non vale più in assoluto. Il **70** ha un solo `next` ma due `previous`: arrivandoci da 17 si sceglie tra 47 e 71, da 71 indietro tra 17 e 47, da 47 tra 17 e 71. Restano di solo instradamento (due sole connessioni in tutto, nessuna scelta) 17, 33, 47, 63 e 73.
3. **`Game` (main dump sezioni 5 e 6, delta 03-10 sezione 6)**:
   - **RIMOSSO** il metodo privato `moveBackwardByInternal`.
   - **RIMOSSO** il campo `awaitingForward` (e le righe che lo impostavano in `requestBivioChoice` e `requestChoice`, e la lettura in `chooseDestination`).
   - `moveForwardBy` e `moveBackwardBy` ora delegano al nuovo `walk`; `requestBivioChoice` ha una firma diversa (senza `forward`); il muro all'indietro (`isBackwardBlocked`, delta 03-10) si applica filtrando le uscite in `availableExits`, e resta in `moveByChosenDirection` per il "turno sprecato".
   - Il verso dei passi dopo una scelta non è più memorizzato: si ricava da quale lista (`next` o `previous`) della casella del bivio contiene la casella scelta.
4. **`Square.getPreviousPositions()` e i `previous` espliciti di `BivioSquare`/`CheckpointSquare` (main dump sezioni 5 e 6, delta 03-10 sezione 5.2)**: il motore e l'API **non li usano più**; contano solo i `previous` calcolati da `Board`. Restano nel codice, nel seed (tabelle `bivio_square_previous` e `checkpoint_square_previous`) e in `BoardAssemblerIT.assertBivio`, da togliere in seguito. I valori scritti coincidono con quelli calcolati (controllato sul seed).
5. **Causa del bug (main dump, "default `position - 1`")**: la prima casella di ogni ramo (10, 40, 16, 22, 46, 52) aveva come `previous` il valore di default `position - 1`, cioè 9, 39, 15, 21, 45, 51: numeri vuoti senza coordinate. Andando indietro la pedina finiva su un buco, perdeva passi e spariva dal tabellone.
6. **`BoardResponseAssembler` (delta 03-10, sezione 6: "INVARIATO, usa `getPreviousPositions()`")**: ora è **MODIFICATO**: legge i `previous` da `board.getPreviousPositions(...)`. L'81 espone ancora [33, 63, 73]. Le caselle non raggiungibili dalla partenza (buchi di numerazione: 8, 9, 13-15, 18-21…) hanno `previousPositions` **vuoto** (prima `position - 1`); il frontend non lo usa.
7. **`GameStateResponse` (delta 03-10, sezione 2 punto 10: "due campi in più")**: ora ne ha **tre**: `choiceRemainingSteps` (subito dopo `choiceOptions`), `movedPlayerIndex`, `movements`. Omessi dal JSON quando nulli.
8. **Ordine delle opzioni di scelta (main dump, `choiceOptions`)**: ora sono sempre in ordine **crescente** (es. `[6, 40]`, `[47, 71]`). Prima erano nell'ordine della lista `next`/`previous`. I portali (`requestChoice`) restano nell'ordine della lista `linkedPositions`.
9. **`StartOverSquare` (delta 03-10, sezione 5.5)**: il testo del messaggio non è più "{player} è finito su uno start over: torna all'ultimo checkpoint (o alla partenza)": ora è quello della sezione 6.
10. **Test (delta 03-10, sezione 8)**: `choiceMovesPlayerAndContinuesRemainingSteps` ha un'asserzione in più (`choiceRemainingSteps` = 1); `returnsResetMovementWithMessageOnStartOver` aspetta il nuovo testo; `GameControllerIT` ha due test e un helper in più; `BoardAssemblerIT` ha un test in più.
11. **`game-screen.html` (delta 03-10, sezione 9)**: `<app-board>` è stato spostato **in alto** (subito dopo il blocco errori), in modo **provvisorio** (l'utente: "poi ci lavoreremo meglio sul design della pagina"); è in più il blocco "Passi rimasti"; il resto è invariato. Il `game-screen.ts` ha il nuovo `computed` `choicePositions`.
12. **TODO del delta 03-10 (sezione 11)**:
    - Punto 1 ("scelte di bivio e portale: rivedere come arrivano al giocatore") → **fatto in parte**: evidenziazione sul tabellone e conferma sono fatte (sezione 5.1); i pulsanti "Vai alla casella N" sono **INVARIATI** e non è stato deciso se tenerli o cambiarli.
    - Punto 2 ("passi rimasti al bivio") → **chiuso** (sezione 5.2).

---

## 3. Modalità di lavoro — novità e note tecniche emerse

- **Frammenti di codice**: quando si dà un frammento va indicato **file e posizione esatta** (dopo/prima di quale riga o blocco, cosa eliminare, cosa sostituire). L'utente lo ha chiesto ("più precisamente").
- **Cambi che toccano il motore**: prima si spiega il piano in poche righe e si chiede conferma, poi si scrive il codice (fatto per i bivi: il modello a incrocio è stato confermato dall'utente prima di scrivere).
- **Sandbox**: stavolta il dominio Java puro è stato ricreato nel sandbox dai dump (compilato con `java -m jdk.compiler/com.sun.tools.javac.Main`) e provato con due harness usa-e-getta, non parte del progetto: una regressione vecchio vs nuovo motore su 3000 partite casuali e una serie di scenari sulla topologia della board OG. Il codice Spring/JPA e i test di integrazione restano a carico dell'utente (niente Maven Central nel sandbox).
- **Errori incontrati e risolti**:
  - `BoardAssemblerIT`: errori di compilazione ("boardRepository cannot be resolved", "assertThat undefined") perché il test nuovo era stato messo in un file/classe a sé, senza l'import statico di `assertThat` e senza i campi `@Autowired`. Il test va dentro la classe originale; risolto dall'utente.
  - `returnsResetMovementWithMessageOnStartOver` falliva: l'utente aveva cambiato **di proposito** il messaggio dello StartOver; il test è stato aggiornato.
- Il codice modificato si mostra a frammenti, i file nuovi per intero; per Java niente commenti salvo richiesta, per TypeScript commenti in italiano sempre (regola invariata).

---

## 4. Struttura cartelle

**Nessun file nuovo, nessun file rimosso, nessun package rinominato** in questa sessione.

### File **MODIFICATI** (backend)
`model/Game.java`, `model/Board.java`, `model/squares/StartOverSquare.java` (solo la costante del messaggio), `mapper/BoardResponseAssembler.java`, `mapper/GameStateAssembler.java`, `dto/GameStateResponse.java`, test: `entity/BoardAssemblerIT.java`, `api/GameControllerIT.java`.

### File **MODIFICATI** (frontend)
`models/game-state-response.ts`, `board/board.ts`, `board/board.html`, `board/board.css`, `game-screen/game-screen.ts`, `game-screen/game-screen.html`.

### **INVARIATI**
Tutto il resto, in particolare: `BoardService`, `BoardController`, `GameService`, `GameController`, `Square`, `BivioSquare`, `CheckpointSquare` e le altre caselle, le entity JPA, `SquareMapper`, `BoardAssembler`, il seed e `seed-og-board.sql`, `pom.xml`, `application.properties`; lato frontend `board-layout.ts`, `board-labels.ts`, `pawn/`, `services/`, gli altri modelli, `welcome/`, `player-setup/`, `winner/`, `app.*`, `proxy.conf.json`.

---

## 5. Decisioni di design

### 5.1 Scelte di bivio e portale sul tabellone (strada C)

- **Evidenziazione**: le caselle tra cui scegliere (`choiceOptions`) si colorano sul tabellone (riempimento `#c8e6c9`, bordo `#2e7d32` spesso 3; colori scelti da Claude, modificabili). La casella selezionata mantiene il bordo verde e prende il riempimento giallo della selezione.
- **Click su una casella evidenziata**: non fa la scelta subito. Mostra il dettaglio (tipo e descrizione, come per ogni casella) con un pulsante **"Conferma direzione"**, che fa la scelta. Motivo: durante la scelta l'unico posto dove vedere cosa c'è sulle destinazioni è il tabellone; con la scelta immediata non si potrebbe guardare. Scartate: A (scelta immediata) e B (solo dettaglio, scelta sui pulsanti).
- **I pulsanti "Vai alla casella N" restano** (INVARIATI): la scelta si può fare dai pulsanti o dalla conferma sul tabellone.
- **Come arriva l'informazione al `Board`**: input `choicePositions` (numeri), output `choiceConfirmed` (numero). La schermata di gioco passa `choicePositions` e ascolta `(choiceConfirmed)="choose($event)"`.
- **Durante un'azione in corso** (`isBusy`, richiesta e riproduzione del movimento) `choicePositions` è **vuoto**: sparisce l'evidenziazione e il pulsante di conferma, quindi non si può confermare due volte. Se la richiesta fallisce, `isBusy` torna falso e l'evidenziazione riappare.
- Una destinazione senza coordinate (non disegnata) non si può cliccare sul tabellone; resta il pulsante.

### 5.2 Passi rimasti al bivio

- `GameStateResponse.choiceRemainingSteps` (`Integer`, omesso se nullo): è `awaitingRemainingSteps` di `Game`, contato **dalla casella del bivio**, quindi **compreso il passo della scelta**: con tiro 8 dalla partenza al bivio 7 ne resta 1.
- **Non esposto per i portali**: `requestChoice` imposta 0 (nessuna prosecuzione), quindi l'assembler restituisce `null` quando il valore non è maggiore di 0 (un bivio ha sempre almeno 1 passo rimasto).
- Frontend: sotto "Ultimo tiro", "Passi rimasti: N" se il campo c'è (testo provvisorio).

### 5.3 Bug dei bivi e modello a incrocio

- **Regola dell'utente**: un bivio deve permettere di spostarsi in tutte le uscite che offre, **tranne quella da cui si arriva**, anche andando indietro (esempio dell'utente: dal 73 indietro fino al 70 si sceglie tra 47 e 17).
- **Strade valutate per i `previous` sbagliati**: A = far diventare le sei caselle iniziali dei rami (10, 16, 22, 40, 46, 52) bivi di instradamento con il loro effetto (solo dati, seed da cambiare); B = cambiare il modello. L'utente ha scelto la **B** ("la A non ha senso per come voglio impostarlo").
- **Piano confermato dall'utente** ("mi sembra giusto"):
  1. **`previous` ricavati dal grafo**: `Board` li calcola alla costruzione, visitando dalla partenza (casella 0) tutte le caselle raggiungibili lungo i `next` e registrando per ogni `next` la casella da cui parte. I buchi di numerazione non sono raggiungibili e restano fuori. Liste ordinate in modo crescente, immutabili. I `next` fuori dal tabellone (es. 111 dell'arrivo) sono ignorati.
  2. **Arrivando su un bivio a metà mossa** le uscite sono tutte le strade (`next` e `previous`) tranne quella da cui si arriva.
  3. **Dopo la scelta si prosegue lungo la strada scelta**, senza tornare indietro: il verso dei passi successivi è "avanti" se la casella scelta è nei `next` della casella del bivio, altrimenti "indietro".
  4. **Fermo su un bivio a inizio mossa** (nessuna casella di provenienza) vale la direzione scelta: avanti usa i `next`, indietro i `previous`.
- **Regole che restano come prima**: rimbalzo all'arrivo (aritmetico, `currentPosition - 1`, poi indietro senza rispettare il checkpoint), fermo obbligatorio sul checkpoint nei due versi con il messaggio "il movimento si ferma qui" solo all'indietro e con passi residui, "turno sprecato" in `moveByChosenDirection`, atterraggio e catena degli effetti (`doLandOnCurrentSquare`) a fine movimento (anche con 0 passi rimasti dopo una scelta).
- **`respectCheckpoint`** vuol dire "i passi all'indietro si fermano sul checkpoint"; i passi in avanti usano sempre il flag della board. Dopo un rimbalzo è falso.
- **Muro all'indietro** (`isBackwardBlocked`: sulla partenza, o sul checkpoint o sotto con `backwardAllowedAfterCheckpoint = false`): `availableExits` toglie le uscite che sono solo "indietro"; se non ne restano, il movimento si ferma e si atterra.
- **Esempi verificati** (sezione 9): dal 10 indietro di 3 si arriva al 7 con 2 passi e si sceglie tra 6 e 40 (scelta 40 → 41, scelta 6 → 5); dal 16 indietro sul 12 tra 11 e 22; dal 40 indietro sul 7 tra 6 e 10; dal 73 indietro di 6 sul 70 con 3 passi tra 17 e 47 (47 → 42, 17 → 12); dal 16 avanti di 3 sul 70 tra 47 e 71; tiro di 14 da 0: tre scelte di fila (10/40, 16/22, 47/71); fermo sul 7: indietro 2 → 5 senza scelta, avanti 2 → scelta 10/40; fermo sul 70: avanti 2 → 72 senza scelta, indietro 2 → scelta 17/47.
- **Limiti noti / non fatti**:
  - I `previous` espliciti di `BivioSquare`/`CheckpointSquare`, le tabelle del seed e le asserzioni relative sono ormai ridondanti: da togliere in seguito.
  - Una casella non raggiungibile dalla partenza ha `previous` vuoto: una pedina messa lì da dati sbagliati non avrebbe uscite all'indietro e il movimento si fermerebbe.
  - Sul checkpoint 81 (4 connessioni) con `mandatoryCheckpointEnabled` disattivato su un'altra board il motore offrirebbe una scelta tra le sue uscite, per la regola generale; sulla board OG non capita mai a metà mossa (fermo obbligatorio).
  - Il 70 resta una casella con effetto StartOver (invariato).

### 5.4 Messaggio dello StartOver

- L'utente ha cambiato il testo del messaggio dell'evento `RESET` (modifica voluta): ora il messaggio del test è "Mario torna all'ultimo checkpoint", cioè il template `{player} torna all'ultimo checkpoint` (testo ricavato dall'output del test: controllare la costante nel file). Il `getDescription()` dello StartOver non risulta toccato in questa sessione.

---

## 6. Codice backend — versioni finali

### `model/Game.java` — **MODIFICATO** (solo questi membri; il resto della classe è **INVARIATO**)

Import nuovo:
```java
import java.util.stream.Stream;
```

Costante nuova, subito dopo `STOPPED_AT_CHECKPOINT`:
```java
private static final int NO_POSITION = -1;
```

Campo **RIMOSSO**: `private boolean awaitingForward;`

Blocco che **sostituisce** `moveForwardBy`, `moveBackwardBy`, `moveBackwardByInternal` (**RIMOSSO**) e il vecchio `requestBivioChoice`, prima di `requestChoice`:
```java
    public void moveForwardBy(Player player, int steps) {
        walk(player, steps, true, board.isMandatoryCheckpointEnabled(), NO_POSITION);
    }

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

    private void requestBivioChoice(Player player, List<Integer> options, int remainingSteps,
            boolean respectCheckpoint) {
        awaitingPlayer = player;
        awaitingOptions = options;
        awaitingRemainingSteps = remainingSteps;
        awaitingRespectCheckpoint = respectCheckpoint;
        awaitingContinueAfterChoice = true;
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

    private void requestBivioChoice(Player player, List<Integer> options, int remainingSteps,
            boolean respectCheckpoint) {
        awaitingPlayer = player;
        awaitingOptions = options;
        awaitingRemainingSteps = remainingSteps;
        awaitingRespectCheckpoint = respectCheckpoint;
        awaitingContinueAfterChoice = true;
    }
```

`requestChoice` (la riga `awaitingForward = true;` è **RIMOSSA**):
```java
public void requestChoice(Player player, List<Integer> options) {
    // usato dalle caselle che si attivano solo su atterraggio esatto (es. Portale):
    // nessuna prosecuzione dopo la scelta, "niente di niente" come il teletrasporto normale
    awaitingPlayer = player;
    awaitingOptions = options;
    awaitingRemainingSteps = 0;
    awaitingRespectCheckpoint = false;
    awaitingContinueAfterChoice = false;
}
```

Getter **NUOVO**, accanto a `getChoiceOptions`:
```java
public int getChoiceRemainingSteps() {
    return awaitingRemainingSteps;
}
```

`chooseDestination` (versione finale completa):
```java
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
        boolean respectCheckpoint = awaitingRespectCheckpoint;
        boolean continueAfterChoice = awaitingContinueAfterChoice;
        int choicePosition = waitingPlayer.getPosition();

        awaitingPlayer = null;
        awaitingOptions = null;

        movePlayerTo(waitingPlayer, chosenPosition, continueAfterChoice ? MovementType.STEP : MovementType.JUMP, null);

        if (!continueAfterChoice) {
            return;
        }

        boolean forward = board.getNextPositions(choicePosition).contains(chosenPosition);
        walk(waitingPlayer, remainingSteps - 1, forward, respectCheckpoint, choicePosition);
    }
```

### `model/Board.java` — **MODIFICATO** (versione finale completa)
Nuovi: campo `previousPositions`, `derivePreviousPositions`, `getNextPositions(int)`, `getPreviousPositions(int)`, import di `ArrayDeque` e `Deque`. Il resto è invariato; `Board` resta completamente immutabile.
```java
package com.project.fumanji.model;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class Board {

    private final int finishPosition;
    private final List<Square> squares;
    private final boolean directionChoiceEnabled;
    private final boolean mandatoryCheckpointEnabled;
    private final boolean backwardAllowedAfterCheckpoint;
    private final List<List<Integer>> previousPositions;

    public Board(int size) {
        if (size <= 0) {
            throw new IllegalArgumentException("Board size must be positive");
        }
        this.finishPosition = size;
        this.squares = defaultSquares(size);
        this.previousPositions = derivePreviousPositions(this.squares, size);
        this.directionChoiceEnabled = false;
        this.mandatoryCheckpointEnabled = false;
        this.backwardAllowedAfterCheckpoint = true;
    }

    private Board(int finishPosition, List<Square> squares,
            boolean directionChoiceEnabled, boolean mandatoryCheckpointEnabled,
            boolean backwardAllowedAfterCheckpoint) {
        this.finishPosition = finishPosition;
        this.squares = squares;
        this.directionChoiceEnabled = directionChoiceEnabled;
        this.mandatoryCheckpointEnabled = mandatoryCheckpointEnabled;
        this.backwardAllowedAfterCheckpoint = backwardAllowedAfterCheckpoint;
        this.previousPositions = derivePreviousPositions(squares, finishPosition);
    }

    private static List<Square> defaultSquares(int size) {
        List<Square> result = new ArrayList<>();
        for (int position = 0; position <= size; position++) {
            result.add(new Square(position));
        }
        return List.copyOf(result);
    }

    private static List<List<Integer>> derivePreviousPositions(List<Square> squares, int finishPosition) {
        List<List<Integer>> previous = new ArrayList<>();
        for (int position = 0; position <= finishPosition; position++) {
            previous.add(new ArrayList<>());
        }

        boolean[] reached = new boolean[finishPosition + 1];
        Deque<Integer> toVisit = new ArrayDeque<>();
        reached[0] = true;
        toVisit.add(0);

        while (!toVisit.isEmpty()) {
            int position = toVisit.poll();
            for (int next : squares.get(position).getNextPositions()) {
                if (next < 0 || next > finishPosition) {
                    continue;
                }
                previous.get(next).add(position);
                if (!reached[next]) {
                    reached[next] = true;
                    toVisit.add(next);
                }
            }
        }

        return previous.stream()
                .map(positions -> positions.stream().sorted().toList())
                .toList();
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

    public List<Integer> getNextPositions(int position) {
        return squares.get(position).getNextPositions();
    }

    public List<Integer> getPreviousPositions(int position) {
        return previousPositions.get(position);
    }

    public boolean isDirectionChoiceEnabled() {
        return directionChoiceEnabled;
    }

    public boolean isMandatoryCheckpointEnabled() {
        return mandatoryCheckpointEnabled;
    }

    public boolean isBackwardAllowedAfterCheckpoint() {
        return backwardAllowedAfterCheckpoint;
    }

    public static class Builder {

        private final int finishPosition;
        private final List<Square> squares;
        private boolean directionChoiceEnabled;
        private boolean mandatoryCheckpointEnabled;
        private boolean backwardAllowedAfterCheckpoint = true;

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

        public Builder backwardAllowedAfterCheckpoint(boolean enabled) {
            this.backwardAllowedAfterCheckpoint = enabled;
            return this;
        }

        public Board build() {
            return new Board(finishPosition, List.copyOf(squares),
                    directionChoiceEnabled, mandatoryCheckpointEnabled,
                    backwardAllowedAfterCheckpoint);
        }
    }
}
```

### `model/squares/StartOverSquare.java` — **MODIFICATO** (solo la costante; il resto è **INVARIATO**)
```java
private static final String MESSAGE_TEMPLATE =
        "{player} torna all'ultimo checkpoint";
```

### `mapper/BoardResponseAssembler.java` — **MODIFICATO** (versione finale completa; package aggiornati ai nomi del delta 03-10)
```java
package com.project.fumanji.mapper;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.project.fumanji.dto.BoardResponse;
import com.project.fumanji.dto.SquareResponse;
import com.project.fumanji.dto.SquareType;
import com.project.fumanji.model.Board;
import com.project.fumanji.model.Square;
import com.project.fumanji.model.squares.BivioSquare;
import com.project.fumanji.model.squares.CheckpointSquare;
import com.project.fumanji.model.squares.DrinkSquare;
import com.project.fumanji.model.squares.MoveBackSquare;
import com.project.fumanji.model.squares.MoveForwardSquare;
import com.project.fumanji.model.squares.ObligationSquare;
import com.project.fumanji.model.squares.PortalSquare;
import com.project.fumanji.model.squares.RollAgainSquare;
import com.project.fumanji.model.squares.SnackSquare;
import com.project.fumanji.model.squares.StartOverSquare;

@Component
public class BoardResponseAssembler {

    private static final Map<Class<? extends Square>, SquareType> TYPES = Map.ofEntries(
            Map.entry(StartOverSquare.class, SquareType.START_OVER),
            Map.entry(MoveBackSquare.class, SquareType.MOVE_BACK),
            Map.entry(MoveForwardSquare.class, SquareType.MOVE_FORWARD),
            Map.entry(RollAgainSquare.class, SquareType.ROLL_AGAIN),
            Map.entry(SnackSquare.class, SquareType.SNACK),
            Map.entry(DrinkSquare.class, SquareType.DRINK),
            Map.entry(ObligationSquare.class, SquareType.OBLIGATION),
            Map.entry(CheckpointSquare.class, SquareType.CHECKPOINT),
            Map.entry(PortalSquare.class, SquareType.PORTAL),
            Map.entry(BivioSquare.class, SquareType.BIVIO));

    public BoardResponse toResponse(Board board) {
        int finishPosition = board.getFinishPosition();
        List<SquareResponse> squares = board.getSquares().stream()
                .map(square -> toSquareResponse(board, square))
                .toList();
        return new BoardResponse(finishPosition, squares);
    }

    private SquareResponse toSquareResponse(Board board, Square square) {
        List<Integer> linkedPositions = square instanceof PortalSquare portal
                ? portal.getLinkedPositions()
                : null;

        return new SquareResponse(
                square.getPosition(),
                TYPES.getOrDefault(square.getClass(), SquareType.BASE),
                square.getDescription(),
                withinBoard(square.getNextPositions(), board.getFinishPosition()),
                board.getPreviousPositions(square.getPosition()),
                linkedPositions);
    }

    private List<Integer> withinBoard(List<Integer> positions, int finishPosition) {
        return positions.stream()
                .filter(position -> position >= 0 && position <= finishPosition)
                .toList();
    }
}
```

### `dto/GameStateResponse.java` — **MODIFICATO** (versione finale completa)
```java
package com.project.fumanji.dto;

import java.util.List;

import com.project.fumanji.model.TurnPhase;

public record GameStateResponse(
        String currentPlayerName,
        TurnPhase turnPhase,
        int lastRoll,
        List<PlayerStateResponse> players,
        List<Integer> choiceOptions,
        Integer choiceRemainingSteps,
        String winnerName,
        List<String> messages,
        Integer movedPlayerIndex,
        List<MovementResponse> movements) {
}
```

### `mapper/GameStateAssembler.java` — **MODIFICATO** (versione finale completa)
```java
package com.project.fumanji.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import com.project.fumanji.dto.GameStateResponse;
import com.project.fumanji.dto.MovementResponse;
import com.project.fumanji.dto.PlayerStateResponse;
import com.project.fumanji.model.Game;

@Component
public class GameStateAssembler {

    public GameStateResponse toResponse(Game game) {
        List<PlayerStateResponse> players = game.getPlayers().stream()
                .map(player -> new PlayerStateResponse(player.getName(), player.getPosition(),
                        player.getSnack(), player.getDrink(), player.getCheckpoint()))
                .toList();

        List<Integer> choiceOptions = game.isAwaitingChoice() ? List.copyOf(game.getChoiceOptions()) : null;
        Integer choiceRemainingSteps = game.isAwaitingChoice() && game.getChoiceRemainingSteps() > 0
                ? game.getChoiceRemainingSteps()
                : null;
        String winnerName = game.getWinner() != null ? game.getWinner().getName() : null;
        List<String> turnMessages = game.getTurnMessages();
        List<MovementResponse> movements = game.getMovementTrace().stream()
                .map(movement -> new MovementResponse(movement.position(), movement.type(), movement.message()))
                .toList();
        Integer movedPlayerIndex = movements.isEmpty()
                ? null
                : game.getPlayers().indexOf(game.getLastPlayerToMove());

        return new GameStateResponse(game.getCurrentPlayer().getName(), game.getTurnPhase(),
                game.getLastRoll(), players, choiceOptions, choiceRemainingSteps, winnerName,
                turnMessages.isEmpty() ? null : turnMessages,
                movedPlayerIndex, movements.isEmpty() ? null : movements);
    }
}
```

---

## 7. Test

### `entity/BoardAssemblerIT.java` — **MODIFICATO** (versione finale completa; un test in più: `derivesPreviousPositionsFromGraph`)
Il nuovo test sta **dentro** la classe, prima degli helper. Gli altri due test e gli helper sono **INVARIATI**.
```java
package com.project.fumanji.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import com.project.fumanji.model.Board;
import com.project.fumanji.model.Square;
import com.project.fumanji.model.squares.BivioSquare;
import com.project.fumanji.model.squares.CheckpointSquare;
import com.project.fumanji.model.squares.DrinkSquare;
import com.project.fumanji.model.squares.ObligationSquare;
import com.project.fumanji.model.squares.PortalSquare;
import com.project.fumanji.model.squares.SnackSquare;
import com.project.fumanji.model.squares.StartOverSquare;
import com.project.fumanji.repository.BoardRepository;

@SpringBootTest
@ActiveProfiles("test")
@Sql(scripts = "/seed-og-board.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
class BoardAssemblerIT {

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private BoardAssembler boardAssembler;

    @Test
    void reconstructsOgBoardFromDatabase() {
        BoardEntity entity = boardRepository.findById(1L).orElseThrow();
        Board board = boardAssembler.toDomain(entity);

        assertThat(board.getFinishPosition()).isEqualTo(110);
        assertThat(board.isDirectionChoiceEnabled()).isTrue();
        assertThat(board.isMandatoryCheckpointEnabled()).isTrue();
        assertThat(board.isBackwardAllowedAfterCheckpoint()).isFalse();

        Square square7 = board.getSquares().get(7);
        assertThat(square7).isInstanceOf(BivioSquare.class);
        assertThat(square7.getNextPositions()).containsExactly(10, 40);

        Square square81 = board.getSquares().get(81);
        assertThat(square81).isInstanceOf(CheckpointSquare.class);
        assertThat(square81.isMandatoryStop()).isTrue();
        assertThat(square81.getPreviousPositions()).containsExactlyInAnyOrder(33, 63, 73);
    }

    @Test
    void reconstructsAllOgBoardSquaresCorrectly() {
        BoardEntity entity = boardRepository.findById(1L).orElseThrow();
        Board board = boardAssembler.toDomain(entity);
        List<Square> squares = board.getSquares();

        // caselle base: partenza, arrivo, i 6 gap "no smoke"
        List<Integer> plainPositions = List.of(0, 6, 24, 54, 85, 97, 101, 110);
        for (int position : plainPositions) {
            assertThat(squares.get(position)).as("position %d", position).isExactlyInstanceOf(Square.class);
        }

        // tipi delle 68 caselle piazzate
        Map<Integer, Class<? extends Square>> expectedTypes = new HashMap<>();
        expectedTypes.put(1, ObligationSquare.class);
        expectedTypes.put(2, ObligationSquare.class);
        expectedTypes.put(3, ObligationSquare.class);
        expectedTypes.put(4, DrinkSquare.class);
        expectedTypes.put(5, StartOverSquare.class);
        expectedTypes.put(7, BivioSquare.class);
        expectedTypes.put(10, ObligationSquare.class);
        expectedTypes.put(11, ObligationSquare.class);
        expectedTypes.put(12, BivioSquare.class);
        expectedTypes.put(16, StartOverSquare.class);
        expectedTypes.put(17, BivioSquare.class);
        expectedTypes.put(22, DrinkSquare.class);
        expectedTypes.put(23, PortalSquare.class);
        expectedTypes.put(25, ObligationSquare.class);
        expectedTypes.put(26, ObligationSquare.class);
        expectedTypes.put(27, PortalSquare.class);
        expectedTypes.put(28, SnackSquare.class);
        expectedTypes.put(29, ObligationSquare.class);
        expectedTypes.put(30, ObligationSquare.class);
        expectedTypes.put(31, DrinkSquare.class);
        expectedTypes.put(32, PortalSquare.class);
        expectedTypes.put(33, BivioSquare.class);
        expectedTypes.put(40, ObligationSquare.class);
        expectedTypes.put(41, ObligationSquare.class);
        expectedTypes.put(42, BivioSquare.class);
        expectedTypes.put(46, StartOverSquare.class);
        expectedTypes.put(47, BivioSquare.class);
        expectedTypes.put(52, DrinkSquare.class);
        expectedTypes.put(53, PortalSquare.class);
        expectedTypes.put(55, ObligationSquare.class);
        expectedTypes.put(56, SnackSquare.class);
        expectedTypes.put(57, ObligationSquare.class);
        expectedTypes.put(58, PortalSquare.class);
        expectedTypes.put(59, ObligationSquare.class);
        expectedTypes.put(60, DrinkSquare.class);
        expectedTypes.put(61, ObligationSquare.class);
        expectedTypes.put(62, PortalSquare.class);
        expectedTypes.put(63, BivioSquare.class);
        expectedTypes.put(70, BivioSquare.class);
        expectedTypes.put(71, SnackSquare.class);
        expectedTypes.put(72, StartOverSquare.class);
        expectedTypes.put(73, BivioSquare.class);
        expectedTypes.put(81, CheckpointSquare.class);
        expectedTypes.put(82, ObligationSquare.class);
        expectedTypes.put(83, DrinkSquare.class);
        expectedTypes.put(84, SnackSquare.class);
        expectedTypes.put(86, ObligationSquare.class);
        expectedTypes.put(87, DrinkSquare.class);
        expectedTypes.put(88, StartOverSquare.class);
        expectedTypes.put(89, ObligationSquare.class);
        expectedTypes.put(90, ObligationSquare.class);
        expectedTypes.put(91, ObligationSquare.class);
        expectedTypes.put(92, ObligationSquare.class);
        expectedTypes.put(93, StartOverSquare.class);
        expectedTypes.put(94, DrinkSquare.class);
        expectedTypes.put(95, SnackSquare.class);
        expectedTypes.put(96, StartOverSquare.class);
        expectedTypes.put(98, ObligationSquare.class);
        expectedTypes.put(99, ObligationSquare.class);
        expectedTypes.put(100, ObligationSquare.class);
        expectedTypes.put(102, SnackSquare.class);
        expectedTypes.put(103, StartOverSquare.class);
        expectedTypes.put(104, DrinkSquare.class);
        expectedTypes.put(105, ObligationSquare.class);
        expectedTypes.put(106, StartOverSquare.class);
        expectedTypes.put(107, ObligationSquare.class);
        expectedTypes.put(108, ObligationSquare.class);
        expectedTypes.put(109, StartOverSquare.class);

        expectedTypes.forEach((position, type) ->
                assertThat(squares.get(position)).as("position %d", position).isExactlyInstanceOf(type));

        // next/previous dei 9 bivi
        assertBivio(squares, 7, List.of(10, 40), List.of(6));
        assertBivio(squares, 12, List.of(16, 22), List.of(11));
        assertBivio(squares, 17, List.of(70), List.of(16));
        assertBivio(squares, 33, List.of(81), List.of(32));
        assertBivio(squares, 42, List.of(46, 52), List.of(41));
        assertBivio(squares, 47, List.of(70), List.of(46));
        assertBivio(squares, 63, List.of(81), List.of(62));
        assertBivio(squares, 70, List.of(71), List.of(17, 47));
        assertBivio(squares, 73, List.of(81), List.of(72));

        // collegamenti dei 6 portali: ognuno verso le altre 5 posizioni
        assertPortalLinks(squares, 53, List.of(23, 32, 62, 27, 58));
        assertPortalLinks(squares, 23, List.of(53, 32, 62, 27, 58));
        assertPortalLinks(squares, 32, List.of(53, 23, 62, 27, 58));
        assertPortalLinks(squares, 62, List.of(53, 23, 32, 27, 58));
        assertPortalLinks(squares, 27, List.of(53, 23, 32, 62, 58));
        assertPortalLinks(squares, 58, List.of(53, 23, 32, 62, 27));

        // amount di Snack/Drink (default 1, x2 dove segnato "x2" sul tabellone)
        assertSnackAmount(squares, 28, 1);
        assertSnackAmount(squares, 56, 1);
        assertSnackAmount(squares, 71, 1);
        assertSnackAmount(squares, 84, 1);
        assertSnackAmount(squares, 95, 1);
        assertSnackAmount(squares, 102, 1);

        assertDrinkAmount(squares, 4, 1);
        assertDrinkAmount(squares, 22, 1);
        assertDrinkAmount(squares, 31, 1);
        assertDrinkAmount(squares, 52, 1);
        assertDrinkAmount(squares, 60, 1);
        assertDrinkAmount(squares, 83, 2);
        assertDrinkAmount(squares, 87, 1);
        assertDrinkAmount(squares, 94, 2);
        assertDrinkAmount(squares, 104, 2);
    }

    @Test
    void derivesPreviousPositionsFromGraph() {
        BoardEntity entity = boardRepository.findById(1L).orElseThrow();
        Board board = boardAssembler.toDomain(entity);

        assertThat(board.getPreviousPositions(0)).isEmpty();
        assertThat(board.getPreviousPositions(10)).containsExactly(7);
        assertThat(board.getPreviousPositions(40)).containsExactly(7);
        assertThat(board.getPreviousPositions(16)).containsExactly(12);
        assertThat(board.getPreviousPositions(22)).containsExactly(12);
        assertThat(board.getPreviousPositions(46)).containsExactly(42);
        assertThat(board.getPreviousPositions(52)).containsExactly(42);
        assertThat(board.getPreviousPositions(70)).containsExactly(17, 47);
        assertThat(board.getPreviousPositions(81)).containsExactly(33, 63, 73);
        assertThat(board.getPreviousPositions(15)).isEmpty();
    }

    private void assertBivio(List<Square> squares, int position, List<Integer> next, List<Integer> previous) {
        Square square = squares.get(position);
        assertThat(square).as("position %d", position).isExactlyInstanceOf(BivioSquare.class);
        assertThat(square.getNextPositions()).as("next of %d", position).containsExactlyInAnyOrderElementsOf(next);
        assertThat(square.getPreviousPositions()).as("previous of %d", position).containsExactlyInAnyOrderElementsOf(previous);
    }

    private void assertPortalLinks(List<Square> squares, int position, List<Integer> otherPortals) {
        PortalSquare portal = (PortalSquare) squares.get(position);
        assertThat(portal.getLinkedPositions()).as("portal %d links", position)
                .containsExactlyInAnyOrderElementsOf(otherPortals);
    }

    private void assertSnackAmount(List<Square> squares, int position, int expectedAmount) {
        SnackSquare snack = (SnackSquare) squares.get(position);
        assertThat(snack.getAmount()).as("snack amount at %d", position).isEqualTo(expectedAmount);
    }

    private void assertDrinkAmount(List<Square> squares, int position, int expectedAmount) {
        DrinkSquare drink = (DrinkSquare) squares.get(position);
        assertThat(drink.getAmount()).as("drink amount at %d", position).isEqualTo(expectedAmount);
    }
}
```

### `api/GameControllerIT.java` — **MODIFICATO** (solo questi membri; il resto è **INVARIATO**)

**Helper nuovo**, accanto agli altri (`registerGame`, `roll`, `move`):
```java
private ResultActions choose(UUID gameId, int position) throws Exception {
    return mockMvc.perform(post("/api/games/{gameId}/choice", gameId)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"position\": " + position + "}"));
}
```

**Test modificato** `choiceMovesPlayerAndContinuesRemainingSteps`: nella prima chiamata a `move` (quella che si ferma su `WAITING_CHOICE`) la catena finisce ora con:
```java
            .andExpect(jsonPath("$.turnPhase", is("WAITING_CHOICE")))
            .andExpect(jsonPath("$.choiceOptions", containsInAnyOrder(10, 40)))
            .andExpect(jsonPath("$.choiceRemainingSteps", is(1)));
```

**Test modificato** `returnsResetMovementWithMessageOnStartOver`: l'asserzione sul messaggio è
```java
.andExpect(jsonPath("$.movements[5].message",
        is("Mario torna all'ultimo checkpoint")))
```

**Test nuovi** (**NUOVO**, 2):
```java
@Test
void offersOtherBranchWhenGoingBackToBivio() throws Exception {
    UUID gameId = UUID.fromString(createGame());

    dice.setNextRoll(8);
    roll(gameId);
    move(gameId, true).andExpect(jsonPath("$.turnPhase", is("WAITING_CHOICE")));
    choose(gameId, 10)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.players[0].position", is(10)));

    dice.setNextRoll(1);
    roll(gameId);
    move(gameId, true).andExpect(status().isOk());

    dice.setNextRoll(3);
    roll(gameId);
    move(gameId, false)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.turnPhase", is("WAITING_CHOICE")))
            .andExpect(jsonPath("$.choiceOptions", containsInAnyOrder(6, 40)))
            .andExpect(jsonPath("$.choiceRemainingSteps", is(2)));

    choose(gameId, 40)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.players[0].position", is(41)));
}

@Test
void offersBothOtherExitsWhenPassingMergeBivio() throws Exception {
    UUID gameId = UUID.fromString(createGame());
    dice.setNextRoll(14);

    roll(gameId);
    move(gameId, true).andExpect(jsonPath("$.choiceOptions", containsInAnyOrder(10, 40)));
    choose(gameId, 10).andExpect(jsonPath("$.choiceOptions", containsInAnyOrder(16, 22)));
    choose(gameId, 16)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.turnPhase", is("WAITING_CHOICE")))
            .andExpect(jsonPath("$.choiceOptions", containsInAnyOrder(47, 71)))
            .andExpect(jsonPath("$.choiceRemainingSteps", is(1)));
    choose(gameId, 47)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.players[0].position", is(47)))
            .andExpect(jsonPath("$.currentPlayerName", is("Luigi")));
}
```
Nota: il tiro 14 è un artificio di test (fuori dal range reale 1-6), come il 7 e l'8 usati prima, per attraversare tre bivi in un solo tiro.

---

## 8. Codice frontend — versioni finali

### `models/game-state-response.ts` — **MODIFICATO** (versione finale completa)
```typescript
import { MovementResponse } from './movement-response';
import { PlayerStateResponse } from './player-state-response';
import { TurnPhase } from './turn-phase';

export interface GameStateResponse {
  currentPlayerName: string;
  turnPhase: TurnPhase;
  lastRoll: number;
  players: PlayerStateResponse[];
  // Assenti dalla risposta quando non ci sono (il backend omette i valori null)
  choiceOptions?: number[];
  // Passi ancora da fare partendo dal bivio (compreso quello della scelta); assente per i portali
  choiceRemainingSteps?: number;
  winnerName?: string;
  // Messaggi generati dalle caselle durante il turno; assenti dalla risposta quando non ce ne sono
  messages?: string[];
  // Indice del giocatore che si è mosso e posizioni attraversate durante l'azione; assenti se non c'è stato movimento
  movedPlayerIndex?: number;
  movements?: MovementResponse[];
}
```

### `board/board.ts` — **MODIFICATO** (versione finale completa)
Nuovi: import di `output`, input `choicePositions`, output `choiceConfirmed`, `choiceSet`, metodo `confirmChoice`. *(l'ordine degli import è indicativo)*
```typescript
import { Component, OnInit, computed, inject, input, output, signal } from '@angular/core';

import { BoardResponse } from '../models/board-response';
import { PlayerStateResponse } from '../models/player-state-response';
import { SquareResponse } from '../models/square-response';
import { Pawn } from '../pawn/pawn';
import { BoardApi } from '../services/board-api';
import { SQUARE_LABELS } from './board-labels';
import {
  BOARD_COLUMNS,
  BOARD_LAYOUT,
  BOARD_ROWS,
  CELL_SIZE,
  DEFAULT_FOOTPRINT,
  SQUARE_FOOTPRINTS,
  SquareShape,
} from './board-layout';

// Id della board OG: per ora una sola board fissa, stesso id usato dal backend
const OG_BOARD_ID = 1;

// Distanza verticale tra le righe di un'etichetta, in unità SVG
const LABEL_LINE_HEIGHT = 12;

// Raggio massimo di una pedina; con più pedine sulla stessa casella si restringe per farle stare affiancate
const PAWN_RADIUS = 8;

// Fascia delle pedine in una casella tonda, in proporzione al lato: più stretta e più in alto, per restare dentro il cerchio
const CIRCLE_PAWN_ROW_WIDTH_RATIO = 0.8;
const CIRCLE_PAWN_ROW_HEIGHT_RATIO = 0.75;

// Colori delle pedine, assegnati in base all'ordine di gioco (10 = massimo dei giocatori)
const PLAYER_COLORS = [
  '#d32f2f', '#1976d2', '#388e3c', '#f57c00', '#7b1fa2',
  '#00838f', '#5d4037', '#c2185b', '#455a64', '#827717',
];

// Riga di un'etichetta con la sua ordinata SVG (centro della riga)
interface LabelLine {
  text: string;
  y: number;
}

// Casella insieme alle coordinate SVG del suo angolo in alto a sinistra, al lato, alla forma e alle righe dell'etichetta
interface PlacedSquare {
  square: SquareResponse;
  x: number;
  y: number;
  size: number;
  shape: SquareShape;
  lines: LabelLine[];
}

// Pedina con le coordinate SVG del suo centro
interface PlacedPawn {
  index: number;
  x: number;
  y: number;
  radius: number;
  color: string;
  initial: string;
}

@Component({
  selector: 'app-board',
  imports: [Pawn],
  templateUrl: './board.html',
  styleUrl: './board.css',
})
export class Board implements OnInit {

  private readonly boardApi = inject(BoardApi);

  readonly width = BOARD_COLUMNS * CELL_SIZE;
  readonly height = BOARD_ROWS * CELL_SIZE;
  readonly viewBox = `0 0 ${this.width} ${this.height}`;

  // Tabellone ricevuto dal backend; resta null finché non arriva la risposta
  readonly board = signal<BoardResponse | null>(null);

  // Messaggio mostrato se il tabellone non si riesce a caricare
  readonly errorMessage = signal<string | null>(null);

  // Posizione della casella cliccata, di cui si mostra il dettaglio
  readonly selectedPosition = signal<number | null>(null);

  // Stato dei giocatori, da cui si ricavano le pedine; lo fornisce la schermata di gioco
  readonly players = input<PlayerStateResponse[]>([]);

  // Posizioni tra cui scegliere (bivio o portale): si evidenziano e, selezionandole, si può confermare la direzione
  readonly choicePositions = input<number[]>([]);

  // Emesso quando il giocatore conferma la casella selezionata come destinazione
  readonly choiceConfirmed = output<number>();

  readonly choiceSet = computed(() => new Set(this.choicePositions()));

  // Si disegnano solo le caselle che hanno già una coordinata
  readonly placedSquares = computed<PlacedSquare[]>(() =>
    (this.board()?.squares ?? []).flatMap(square => {
      const cell = BOARD_LAYOUT.get(square.position);
      if (!cell) {
        return [];
      }
      const { span, shape } = SQUARE_FOOTPRINTS[square.position] ?? DEFAULT_FOOTPRINT;
      const size = span * CELL_SIZE;
      // Da cella della griglia a coordinate SVG
      const x = cell.x * CELL_SIZE;
      const y = cell.y * CELL_SIZE;
      // Senza etichetta si mostra il numero di posizione; ogni elemento è una riga
      const rows = SQUARE_LABELS[square.position] ?? [String(square.position)];
      return [{
        square,
        x,
        y,
        size,
        shape,
        // Righe centrate verticalmente nella casella
        lines: rows.map((text, index) => ({
          text,
          y: y + size / 2 + (index - (rows.length - 1) / 2) * LABEL_LINE_HEIGHT,
        })),
      }];
    }));

  readonly pawns = computed<PlacedPawn[]>(() => {
    const players = this.players();

    // Quante pedine ci sono su ogni casella, per dividere la casella fra loro
    const countByPosition = new Map<number, number>();
    for (const player of players) {
      countByPosition.set(player.position, (countByPosition.get(player.position) ?? 0) + 1);
    }

    // Quante pedine di ogni casella sono già state sistemate
    const placedByPosition = new Map<number, number>();

    return players.flatMap((player, index) => {
      const position = player.position;
      const cell = BOARD_LAYOUT.get(position);
      // Posizione senza coordinate: la pedina non si disegna
      if (!cell) {
        return [];
      }
      const { span, shape } = SQUARE_FOOTPRINTS[position] ?? DEFAULT_FOOTPRINT;
      const size = span * CELL_SIZE;
      const originX = cell.x * CELL_SIZE;
      const originY = cell.y * CELL_SIZE;
      const slot = placedByPosition.get(position) ?? 0;
      placedByPosition.set(position, slot + 1);

      // Pedine affiancate in una fascia: in basso nelle caselle quadrate, più stretta e più in alto in quelle tonde
      const isCircle = shape === 'circle';
      const rowWidth = isCircle ? size * CIRCLE_PAWN_ROW_WIDTH_RATIO : size;
      const slotWidth = rowWidth / countByPosition.get(position)!;
      return [{
        index,
        x: originX + (size - rowWidth) / 2 + slotWidth * (slot + 0.5),
        y: isCircle ? originY + size * CIRCLE_PAWN_ROW_HEIGHT_RATIO : originY + size - PAWN_RADIUS - 2,
        radius: Math.min(PAWN_RADIUS, slotWidth / 2),
        color: PLAYER_COLORS[index % PLAYER_COLORS.length],
        initial: player.name.charAt(0).toUpperCase(),
      }];
    });
  });

  // Casella selezionata, ricavata dalla posizione cliccata
  readonly selectedSquare = computed(() =>
    this.board()?.squares.find(square => square.position === this.selectedPosition()) ?? null);

  ngOnInit(): void {
    this.boardApi.getBoard(OG_BOARD_ID).subscribe({
      next: board => this.board.set(board),
      error: () => this.errorMessage.set('Impossibile caricare il tabellone.'),
    });
  }

  select(position: number): void {
    this.selectedPosition.set(position);
  }

  confirmChoice(): void {
    const position = this.selectedPosition();
    // Si conferma solo una casella che è tra le destinazioni possibili
    if (position === null || !this.choiceSet().has(position)) {
      return;
    }
    this.choiceConfirmed.emit(position);
  }
}
```

### `board/board.html` — **MODIFICATO** (versione finale completa)
```html
<div class="board">
  @if (errorMessage()) {
    <p class="error">{{ errorMessage() }}</p>
  }

  <div class="board-scroll">
    <svg class="board-svg" [attr.viewBox]="viewBox" [attr.width]="width" [attr.height]="height">
      @for (placed of placedSquares(); track placed.square.position) {
        <g
          class="square"
          [class.selected]="placed.square.position === selectedPosition()"
          [class.choice]="choiceSet().has(placed.square.position)"
          (click)="select(placed.square.position)"
        >
          @if (placed.shape === 'circle') {
            <circle
              [attr.cx]="placed.x + placed.size / 2"
              [attr.cy]="placed.y + placed.size / 2"
              [attr.r]="placed.size / 2"
            />
          } @else {
            <rect [attr.x]="placed.x" [attr.y]="placed.y" [attr.width]="placed.size" [attr.height]="placed.size" />
          }
          @for (line of placed.lines; track $index) {
            <text
              [attr.x]="placed.x + placed.size / 2"
              [attr.y]="line.y"
              text-anchor="middle"
              dominant-baseline="central"
            >{{ line.text }}</text>
          }
        </g>
      }

      @for (pawn of pawns(); track pawn.index) {
        <g
          appPawn
          [x]="pawn.x"
          [y]="pawn.y"
          [radius]="pawn.radius"
          [color]="pawn.color"
          [initial]="pawn.initial"
        ></g>
      }
    </svg>
  </div>

  @let selected = selectedSquare();
  @if (selected) {
    <div class="square-detail">
      <h3>Casella {{ selected.position }}</h3>
      <p>Tipo: {{ selected.type }}</p>
      @if (selected.description) {
        <p>{{ selected.description }}</p>
      }
      @if (choiceSet().has(selected.position)) {
        <button type="button" (click)="confirmChoice()">Conferma direzione</button>
      }
    </div>
  }
</div>
```

### `board/board.css` — **MODIFICATO** (versione finale completa)
La regola `.square.choice` sta prima di `.square.selected`, così il riempimento della selezione vince.
```css
.board-scroll {
  overflow-x: auto;
}

.board-svg {
  display: block;
  border: 1px solid #ccc;
}

.square {
  cursor: pointer;
}

.square rect,
.square circle {
  fill: #fff;
  stroke: #333;
  stroke-width: 2;
}

.square.choice rect,
.square.choice circle {
  fill: #c8e6c9;
  stroke: #2e7d32;
  stroke-width: 3;
}

.square.selected rect,
.square.selected circle {
  fill: #ffe9a8;
}

.square text {
  font-size: 10px;
  pointer-events: none;
  user-select: none;
}

.square-detail {
  border: 1px solid #ccc;
  padding: 0.5rem 1rem;
  margin: 1rem 0;
}

.error {
  color: red;
}
```

### `game-screen/game-screen.ts` — **MODIFICATO** (versione finale completa; nuovo il `computed` `choicePositions`, il resto è **INVARIATO**)
```typescript
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Observable } from 'rxjs';

import { Board } from '../board/board';
import { GameStateResponse } from '../models/game-state-response';
import { PlayerStateResponse } from '../models/player-state-response';
import { GameApi } from '../services/game-api';

// Pausa tra una posizione e la successiva durante il movimento: poco più della transizione CSS della pedina (0,4 s)
const STEP_DELAY_MS = 450;

// Attesa asincrona di `ms` millisecondi
const sleep = (ms: number) => new Promise<void>(resolve => setTimeout(resolve, ms));

// Pedina in movimento durante la riproduzione: indice del giocatore e posizione mostrata
interface AnimatedPawn {
  playerIndex: number;
  position: number;
}

@Component({
  selector: 'app-game-screen',
  imports: [Board],
  templateUrl: './game-screen.html',
  styleUrl: './game-screen.css',
})
export class GameScreen implements OnInit {

  private readonly route = inject(ActivatedRoute);
  private readonly gameApi = inject(GameApi);
  private readonly router = inject(Router);

  // Identificativo della partita, preso dall'URL (/game/:gameId); la rotta lo garantisce sempre presente
  private readonly gameId = this.route.snapshot.paramMap.get('gameId')!;

  // Ultimo stato ricevuto dal backend; resta null finché non arriva la prima risposta
  readonly state = signal<GameStateResponse | null>(null);

  // Messaggio mostrato se lo stato non si riesce a caricare o un'azione fallisce
  readonly errorMessage = signal<string | null>(null);

  // True mentre un'azione è in corso (richiesta e riproduzione del movimento): disabilita i pulsanti ed evita richieste doppie con click ripetuti
  readonly isBusy = signal(false);

  // Pedina in movimento durante la riproduzione dell'azione; null quando non c'è nulla da animare
  readonly animatedPawn = signal<AnimatedPawn | null>(null);

  // Messaggio dello start over mostrato durante la pausa, in attesa del "Continua"
  readonly pauseMessage = signal<string | null>(null);

  // Riprende la riproduzione dopo la pausa; è null quando non c'è nessuna pausa
  private resumeReplay: (() => void) | null = null;

  // Giocatori come li vede il tabellone: la pedina in movimento mostra la posizione intermedia al posto di quella finale
  readonly boardPlayers = computed<PlayerStateResponse[]>(() => {
    const players = this.state()?.players ?? [];
    const animated = this.animatedPawn();
    if (!animated) {
      return players;
    }
    return players.map((player, index) =>
      index === animated.playerIndex ? { ...player, position: animated.position } : player);
  });

  // Posizioni da evidenziare sul tabellone: solo con una scelta in attesa e nessuna azione in corso
  readonly choicePositions = computed<number[]>(() =>
    this.isBusy() ? [] : (this.state()?.choiceOptions ?? []));

  ngOnInit(): void {
    // Lo stato si legge dal backend a ogni apertura della pagina, anche dopo un refresh
    this.gameApi.getGame(this.gameId).subscribe({
      next: state => this.state.set(state),
      error: (error: HttpErrorResponse) => this.errorMessage.set(this.toErrorMessage(error)),
    });
  }

  roll(): void {
    this.runAction(this.gameApi.roll(this.gameId));
  }

  move(forward: boolean): void {
    this.runAction(this.gameApi.move(this.gameId, forward));
  }

  choose(position: number): void {
    this.runAction(this.gameApi.choose(this.gameId, position));
  }

  // Esegue un'azione del turno: la richiesta parte solo con subscribe() e, se ne è già in corso una, la nuova viene ignorata
  private runAction(action: Observable<GameStateResponse>): void {
    if (this.isBusy()) {
      return;
    }
    this.isBusy.set(true);
    this.errorMessage.set(null);

    action.subscribe({
      // La risposta si applica a movimento concluso: isBusy resta attivo fino ad allora
      next: state => {
        void this.play(state).finally(() => this.isBusy.set(false));
      },
      error: (error: HttpErrorResponse) => {
        this.errorMessage.set(this.toErrorMessage(error));
        this.isBusy.set(false);
      },
    });
  }

  // Riproduce il movimento dell'azione cella per cella e poi applica lo stato finale della risposta
  private async play(state: GameStateResponse): Promise<void> {
    const playerIndex = state.movedPlayerIndex;
    if (playerIndex !== undefined) {
      for (const movement of state.movements ?? []) {
        // Il ritorno dello start over parte solo dopo il "Continua" del giocatore
        if (movement.type === 'RESET' && movement.message) {
          await this.waitForContinue(movement.message);
        }
        this.animatedPawn.set({ playerIndex, position: movement.position });
        await sleep(STEP_DELAY_MS);
      }
      this.animatedPawn.set(null);
    }

    this.state.set(state);
    // Alla vittoria si passa alla schermata del vincitore, a movimento concluso: la partita non è più rileggibile dal backend
    if (state.winnerName) {
      this.router.navigate(['/winner'], { state: { winnerName: state.winnerName } });
    }
  }

  // Mostra il messaggio e resta in attesa del "Continua"
  private waitForContinue(message: string): Promise<void> {
    return new Promise<void>(resolve => {
      this.pauseMessage.set(message);
      this.resumeReplay = () => {
        this.pauseMessage.set(null);
        this.resumeReplay = null;
        resolve();
      };
    });
  }

  continueReplay(): void {
    this.resumeReplay?.();
  }

  // Traduce lo status HTTP dell'errore in un messaggio per il giocatore
  private toErrorMessage(error: HttpErrorResponse): string {
    switch (error.status) {
      case 404:
        return 'Partita non trovata.';
      case 409:
        return 'Azione non consentita in questa fase del turno.';
      case 400:
        return 'Richiesta non valida.';
      default:
        return 'Errore di comunicazione con il server.';
    }
  }
}
```

### `game-screen/game-screen.html` — **MODIFICATO** (versione dell'utente)
`<app-board>` è stato spostato in alto in modo provvisorio e c'è il blocco "Passi rimasti"; il resto è invariato.
```html
<div class="game-screen">
  @if (errorMessage()) {
    <p class="error">{{ errorMessage() }}</p>
  }

  <app-board
    [players]="boardPlayers()"
    [choicePositions]="choicePositions()"
    (choiceConfirmed)="choose($event)"
  />
  
  @let game = state();
  @if (game) {
    <h2>Turno di {{ game.currentPlayerName }}</h2>
    <p>Fase: {{ game.turnPhase }}</p>
    @if (game.lastRoll > 0) {
      <p>Ultimo tiro: {{ game.lastRoll }}</p>
    }
    @if (game.choiceRemainingSteps) {
      <p>Passi rimasti: {{ game.choiceRemainingSteps }}</p>
    }

    <div class="actions">
      @switch (game.turnPhase) {
        @case ('WAITING_ROLL') {
          <button type="button" (click)="roll()" [disabled]="isBusy()">Tira il dado</button>
        }
        @case ('WAITING_DIRECTION') {
          <button type="button" (click)="move(true)" [disabled]="isBusy()">Avanti</button>
          <button type="button" (click)="move(false)" [disabled]="isBusy()">Indietro</button>
        }
        @case ('WAITING_CHOICE') {
          @for (option of game.choiceOptions ?? []; track option) {
            <button type="button" (click)="choose(option)" [disabled]="isBusy()">
              Vai alla casella {{ option }}
            </button>
          }
        }
      }
    </div>

    @if (game.messages; as messages) {
      <div class="messages">
        @for (message of messages; track $index) {
          <p>{{ message }}</p>
        }
      </div>
    }

    @if (pauseMessage(); as message) {
      <div class="messages">
        <p>{{ message }}</p>
        <button type="button" (click)="continueReplay()">Continua</button>
      </div>
    }

    <table>
      <thead>
        <tr>
          <th>Giocatore</th>
          <th>Posizione</th>
          <th>Snack</th>
          <th>Drink</th>
          <th>Checkpoint</th>
        </tr>
      </thead>
      <tbody>
        @for (player of game.players; track $index) {
          <tr>
            <td>{{ player.name }}</td>
            <td>{{ player.position }}</td>
            <td>{{ player.snack }}</td>
            <td>{{ player.drink }}</td>
            <td>{{ player.checkpoint }}</td>
          </tr>
        }
      </tbody>
    </table>
  }
</div>
```

---

## 9. Stato delle verifiche

- **Sandbox (motore, 05-10-26)**: dominio Java puro ricreato dai dump e compilato con `java -m jdk.compiler/com.sun.tools.javac.Main`, versione vecchia e nuova di `Board`/`Game` a confronto.
  - **Regressione**: 3000 partite casuali su tabelloni lineari (dimensioni, caselle speciali, portali, checkpoint, flag della board, giocatori, tiri e direzioni casuali, anche tiri fuori range) → tracce **identiche** tra vecchio e nuovo motore (circa 311.000 righe: fasi, posizioni, checkpoint, snack, drink, messaggi, percorsi). Coperti tra l'altro 5138 stati `WAITING_CHOICE` (portali), 10074 `RESET`, 7483 `JUMP`, 25293 messaggi di turno sprecato e 2731 di arresto sul checkpoint.
  - **Bivi**: 47 controlli sulla topologia della board OG (bivi 7, 12, 17, 33, 42, 47, 63, 70, 73 e checkpoint 81 con i suoi `previous`), tutti ok: gli esempi della sezione 5.3, i tre crea che entrano nel checkpoint in un passo, l'arresto sul checkpoint all'indietro con i due messaggi, il muro sul checkpoint, il rimbalzo all'arrivo, la vittoria esatta e i `previous` calcolati (incluse le liste vuote per la partenza e per i buchi 8, 9, 15, 21).
- **Utente (in locale)**: dopo gli errori della sezione 3 ("tutto apposto ora") i test di integrazione risultano a posto. L'esito dei due test nuovi di `GameControllerIT` e di `derivesPreviousPositionsFromGraph` non è stato riportato uno per uno.
- **Frontend**: l'utente ha applicato le modifiche (ha incollato il `game-screen.html` aggiornato), ma **non ha riportato** una verifica esplicita dell'evidenziazione, del pulsante "Conferma direzione" e dei "Passi rimasti".
- **Non verificato**: `ng test` e gli spec generati dalla CLI; il comportamento della conferma durante la riproduzione del movimento; una partita giocata a mano attraverso i bivi dopo il fix (il bug era stato trovato giocando).

---

## 10. TODO / prossimi passi (aggiornati)

### Aperti (nuovi in questa sessione)
1. **Finestrella per aumentare o diminuire snack e drink di ogni giocatore** (da fare appena possibile): un pulsante apre una finestra in cui si sceglie il giocatore, snack o drink, e si aumenta o diminuisce. Nel dominio esistono già `Game.addSnack` e `Game.addDrink` (clamp a 0); non esistono endpoint, DTO e interfaccia.
2. **Design della pagina di gioco**: oggi il tabellone sta in alto in modo provvisorio; l'utente vuole lavorarci meglio più avanti.
3. **Pulsanti "Vai alla casella N"**: ora che c'è la conferma sul tabellone, da decidere se tenerli, cambiarli o toglierli.
4. **Togliere i `previous` espliciti ora inutilizzati**: `BivioSquare` e `CheckpointSquare` (costruttori, getter), le relative entity e il mapper, le tabelle del seed `bivio_square_previous` e `checkpoint_square_previous`, `Square.getPreviousPositions()` e le asserzioni in `BoardAssemblerIT`.
5. **Provare a mano una partita attraverso i bivi** (7, 12, 42, 70) dopo il fix, in particolare all'indietro.

### Aperti (dai dump precedenti, **INVARIATI**)
- **Etichette di 81 e 110**: ancora senza etichetta (mostrano il numero); nella foto si legge "JUMANJI" e "Fine", da confermare.
- **Regole di board future** (annotate, non fatte): non chiedere la direzione a chi è sul checkpoint e mandarlo avanti; checkpoint come muro con rimbalzo o permanenza a scelta di board; vie senza uscita senza traguardo.
- **Estetica**: StartOver e portali come buchi neri (oggi `0` e `0 N`), fase del turno ancora valore grezzo, testi provvisori.
- **Tabella e stato durante la riproduzione**: si aggiornano solo a fine movimento (strada A); da rivedere se dà fastidio.
- Etichette e coordinate nel backend/DB (l'utente ha detto che cambieranno sicuramente) e dati nuovi per istanza (etichetta dell'obbligo, numero del portale).
- `BoardControllerIT` per `GET /api/boards/{boardId}` (offerto, da decidere).
- Spec generati da `ng g c` per `board`, `pawn`, `player-setup`, `game-screen`, `winner`, `GameApi`: eseguire (`ng test`) e adattare o rimuovere.
- Id della board fisso (`OG_BOARD_ID` nel backend e in `board.ts`), da rivedere con le board multiple.
- Pedine con molti giocatori sulla stessa casella (quadrate: con 10 giocatori il raggio scende a 2,5).
- Tutti gli altri aperti e rimandati del main dump (TTL delle partite, storico/statistiche, board multiple e factory delle caselle, autenticazione, Electron, ordine dei turni lato frontend, proxy solo per sviluppo, nomi doppi, rivalutare EAGER, bivio con percorsi non riconvergenti, schermata del vincitore, test Postman).

### Chiusi in questa sessione
- ~~Scelte di bivio e portale: come arrivano al giocatore~~ → evidenziazione sul tabellone e "Conferma direzione" (restano i pulsanti, vedi punto 3 degli aperti).
- ~~Passi rimasti al bivio~~ → `choiceRemainingSteps` e "Passi rimasti: N".
- ~~Bug dei bivi (passi persi andando indietro, bivio 70 senza scelta)~~ → modello a incrocio, `previous` calcolati dal grafo, verificato in sandbox.
