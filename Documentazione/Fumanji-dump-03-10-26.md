# FUMANJI — Dump di sessione (delta)

> **Data**: 03/10/2026 (Europe/Rome). La sessione è a cavallo tra il 02/10 e il 03/10.
> **Natura del file**: contiene **solo** ciò che è stato fatto/deciso in questa conversazione, con la **versione finale** di ogni file toccato. Va applicato **sopra** il main dump (`Fumanji-main-dump_31-09-26_0443.md`, che arriva fino al 30-09-26) e **sopra** il delta del 01-10 (`Fumanji-dump-01-10-26.md`): resta valido tutto ciò che qui non compare, **tranne** quanto elencato nella sezione 2 ("Correzioni"). L'unione la fa l'utente a parte.
> Dump snello: passaggi superati e cose annullate nella sessione non compaiono (es. l'alias 80→81 della pedina, aggiunto e poi tolto; il costruttore a 1 argomento di `CheckpointSquare`, aggiunto e poi tolto; la lista di righe SQL da correggere). Archivio esteso, non riassunto: il codice è quello **finale**.
> **Verifiche**: nessuna verifica in sandbox in questa sessione. Tutto il codice Spring e Angular è stato provato dall'utente in locale.

## Legenda degli stati (richiesta dell'utente)
Ogni elemento è marcato con uno di questi stati rispetto al main dump + delta 01-10:
- **NUOVO**: non esisteva.
- **MODIFICATO**: esisteva ed è cambiato; è riportata la versione finale.
- **RIMOSSO**: non esiste più.
- **INVARIATO**: esiste ancora così com'era, riportato solo quando potrebbe sembrare toccato.

---

## 1. Cosa è stato fatto (indice)

- **Tabellone, scheletro completo** (dal 7 in poi e fino all'arrivo): griglia 16×12 con scorrimento orizzontale, tutti i rami, checkpoint tondo grande 3×3, percorso 82-110 dal disegno dell'utente, etichette di tutte le caselle (tranne 81 e 110), pedine nel cerchio.
- **Board OG, correzione dei dati**: aggiunta una casella "2 tiri" dopo la partenza (le vecchie 1-6 diventano 2-7, il bivio A passa al 7); **rimosso l'80** (bivio pre-checkpoint, non esiste fisicamente); i tre crea 33, 63 e 73 entrano direttamente nel checkpoint 81, che ora ha le sue `previousPositions`. Seed e test aggiornati.
- **Rinomina dei package** backend: `entity` → `model` (dominio), `persistence` → `entity` (JPA).
- **Messaggi sul limite all'indietro** (checkpoint e partenza): turno sprecato e fermo al checkpoint con passi residui.
- **Movimento visibile** (strada A): il backend espone il percorso dell'azione, il frontend lo riproduce cella per cella, con pausa e "Continua" sullo StartOver.
- **StartOver**: messaggio proprio e `landOnCurrentSquare` riattivato (dopo il ritorno si attiva l'effetto del checkpoint).
- **Errori incontrati e risolti**: MapStruct "Ambiguous constructors", entity senza costruttore vuoto, errore SQL 1064 nel seed, `SquareMapperImpl` duplicato in `target` dopo la rinomina (vedi sezione 3).

---

## 2. Correzioni al main dump e al delta 01-10 (cosa oggi è sbagliato lì)

1. **Nomi dei package (main dump, ovunque)**: `com.project.fumanji.entity` → `com.project.fumanji.model`; `com.project.fumanji.entity.squares` → `com.project.fumanji.model.squares`; `com.project.fumanji.persistence` → `com.project.fumanji.entity`; `com.project.fumanji.persistence.squares` → `com.project.fumanji.entity.squares`. Tabella completa nella sezione 4. Ogni snippet del main dump con quei package ha solo gli `import`/`package` da aggiornare; la logica è quella indicata.
2. **`BivioSquare` (main dump, sezione 6)**: nel file reale c'è **un solo costruttore**, quello a 4 argomenti. Il costruttore a 3 argomenti descritto nel main (e il suo richiamo "retrocompatibile") **non esiste più**. Versione reale in sezione 6.
3. **80 e topologia (main dump, sezioni 5 e 9)**: la frase "Bivio pre-checkpoint (80)", la sua riga nella tabella posizioni, la topologia "il bivio pre-checkpoint raccoglie 3 provenienze e porta al checkpoint" e il passo crea → 80 → 81 non valgono più. Ora i tre crea (33, 63, 73) hanno `next` = [81] e il checkpoint 81 ha `previous` = [33, 63, 73]. Il checkpoint resta 81, l'arrivo resta 110.
4. **Tabella posizioni 0-7 (main dump, sezione 9)**: le righe 0-6 sono sostituite da quelle della sezione 5.2 di questo dump. Le posizioni dal 10 in poi sono **invariate**, tranne l'80 (rimosso) e i `next` di 33, 63, 73.
5. **Convenzione id delle righe "effetto" (main dump, sezioni 5, 8, 9)**: l'effetto del bivio A ora ha id **1007** (= 1000 + 7), non 1006. Le altre righe effetto (1012, 1033, 1042, 1047, 1063, 1070, 1073) sono invariate. Le caselle piazzate sono ancora **68** (+1 la nuova casella 1, −1 l'80, e le vecchie 1-6 spostate in 2-7).
6. **`moveByChosenDirection` (main dump, sezioni 5 e 8: "non toccato", "non tocca la regola `backwardAllowedAfterCheckpoint`")**: ora ha un controllo in più per la scelta "indietro" quando il giocatore è già sul pavimento (caso "turno sprecato"). La regola `backwardAllowedAfterCheckpoint` vive ancora in `moveBackwardByInternal`, ma tramite il nuovo metodo privato `isBackwardBlocked`.
7. **`StartOverSquare` (main dump, sezione 5: "Eccezioni invariate: StartOverSquare (commentata di proposito)")**: non vale più. Il `landOnCurrentSquare` è **attivo**: dopo il ritorno al checkpoint si applica l'effetto della casella di arrivo (checkpoint col suo messaggio; alla partenza una casella base, nessun effetto).
8. **`Game.resetToStart` (main dump, sezione 8: "ha mantenuto il nome")**: la firma è ora `resetToStart(Player player, String message)`; la versione a 1 argomento non esiste più.
9. **`Game.movementTrace` (main dump, sezione 5 e delta 01-10: "lista unica di posizioni, non attribuita al giocatore, fuori dall'API")**: ora è una `List<Movement>`, viene svuotata a ogni `performRoll` e `performChoice` ed è esposta dall'API (`movements`, `movedPlayerIndex`). Resta non attribuita per giocatore nel dominio: l'indice del giocatore si ricava da `getLastPlayerToMove()`.
10. **`GameStateResponse` (main dump, sezioni 5, 6, 10)**: ha due campi in più (`movedPlayerIndex`, `movements`), omessi dal JSON quando nulli.
11. **Test (main dump, sezione 11 e 12)**: `GameControllerIT` ha 5 test in più (20 in totale, se sono stati incollati tutti); `BoardAssemblerIT` ha ancora 2 test ma con dati aggiornati; il tiro "7" usato per arrivare alla scelta a bivio diventa **8** (il bivio A è al 7).
12. **Delta 01-10, sezione 4 "Layout e disegno" e "Pedine"**:
    - "Si disegna per pezzi: oggi solo le caselle 0-6" → il tabellone è **completo** (0-110, tranne i numeri vuoti).
    - "`BOARD_COLUMNS = 9`, `BOARD_ROWS = 8`" → **16 colonne × 12 righe**.
    - "Primo pezzo (0-6)... partenza in basso a sinistra" → la partenza è in colonna 0, riga 11.
    - "Una pedina su una posizione senza coordinate non si disegna (oltre la 6 sparisce)" → vale ancora per i numeri senza casella (gap), ma non più per le caselle reali.
    - "Movimento: transizione CSS in linea retta, non passo per passo" → la transizione CSS (0,4 s) resta ed è usata a **ogni passo** della riproduzione; il movimento passo per passo ora c'è (sezione 5.4).
    - "Limite noto: StartOver, la pedina non si anima" → **risolto** (sezione 5.4).
    - "Pedine affiancate nella parte bassa della casella" → vale per le caselle quadrate; nel cerchio la fascia è più stretta e più in alto (sezione 5.1).
    - Board con `width: 100%; max-width: 600px` e `readonly cellSize` → sostituiti da SVG a dimensione naturale in un contenitore scorrevole; `cellSize` **RIMOSSO** da `board.ts`.
13. **Delta 01-10, sezione 6**: `board-layout.ts`, `board-labels.ts`, `board.ts`, `board.html`, `board.css` sono sostituiti dalle versioni finali di questo dump (sezione 9).

---

## 3. Modalità di lavoro — novità e note tecniche emerse

- **Regola sui dump (richiesta dell'utente)**: nei dump di fine sessione va sempre specificato con chiarezza cosa rimane invariato, cosa non c'è più e cosa è modificato (legenda a inizio file, sezione di correzioni al main dump).
- **Frontend a pezzi, poi scheletro completo, poi estetica**: l'utente ha deciso di completare lo scheletro di tutta la board prima di curare l'estetica delle caselle specifiche (buchi neri, ecc.).
- **Quando si propone un layout** (es. il percorso 82-110) si può proporre una lettura della foto e farla correggere; l'utente ha poi risposto con un disegno.
- **Nuovi nomi dei package**: da ora si usano `model` (dominio), `entity` (JPA) e relative sottocartelle `squares`. Dopo la rinomina serve `.\mvnw.cmd clean` da `BE\spring`: in `target` restava la vecchia `SquareMapperImpl` generata nel package `persistence`, che andava in conflitto di bean con quella nuova (`ConflictingBeanDefinitionException`).
- **MapStruct e costruttori**: una classe di dominio mappata da `SquareMapper` deve avere **un solo costruttore pubblico** (o un parameterless, o un `@Default`). Con due costruttori pubblici MapStruct dà "Ambiguous constructors found for creating ...". Scelta: costruttore unico, niente `@Default` (non si lega il dominio a MapStruct).
- **Entity JPA**: ogni entity deve mantenere il costruttore vuoto `protected`; senza, Hibernate dà "No default constructor for entity" in lettura.
- **Script SQL**: togliendo l'ultima riga di un `INSERT ... VALUES` bisogna spostare il `;` sulla nuova ultima riga (errore 1064 la prima volta). Su Workbench l'esecuzione si ferma al primo errore: ciò che precede è già nel DB; in dubbio si ricrea il DB e si rifà tutto.
- **DB reale dopo cambi al seed**: le righe 1-6 hanno cambiato id, quindi si **ricrea il DB** `fumanji`, si avvia il backend (Hibernate crea lo schema, inclusa la nuova tabella `checkpoint_square_previous`) e si esegue il seed aggiornato. Il DB di test (`create-drop`) si aggiorna da solo.
- Il codice modificato si mostra a frammenti, i file nuovi per intero; per Java niente commenti salvo richiesta, per TypeScript commenti in italiano sempre.

---

## 4. Struttura cartelle — rinomina e aggiunte

### Rinomina dei package (backend) — **MODIFICATO**
La mappatura delle sottocartelle e dei test è stata proposta da Claude e l'utente non ha segnalato differenze.

| Prima | Ora |
|---|---|
| `com.project.fumanji.entity` (dominio: `Board`, `Dice`, `Game`, `InteractiveSquare`, `Player`, `Square`, `TurnPhase`) | `com.project.fumanji.model` |
| `com.project.fumanji.entity.squares` (le 10 sottoclassi) | `com.project.fumanji.model.squares` |
| `com.project.fumanji.persistence` (`SquareEntity`, `BoardEntity`, `EndGameEntity`, `SquareMapper`, `BoardAssembler`) | `com.project.fumanji.entity` |
| `com.project.fumanji.persistence.squares` (le 10 `...SquareEntity`) | `com.project.fumanji.entity.squares` |
| test: `.../entity/ControllableDice.java` | `.../model/ControllableDice.java` |
| test: `.../persistence/BoardAssemblerIT.java` | `.../entity/BoardAssemblerIT.java` |

**INVARIATI**: `api`, `config`, `dto`, `exception`, `mapper`, `repository`, `service` (cambiano solo gli import dei vecchi package).

### Nuovi file backend (**NUOVO**)
```
model/Movement.java
model/MovementType.java
dto/MovementResponse.java
```

### Nuovi file frontend (**NUOVO**)
```
FE/angular/src/app/models/movement-type.ts
FE/angular/src/app/models/movement-response.ts
```

### Frontend — file **MODIFICATI** in questa sessione
`board/board-layout.ts`, `board/board-labels.ts`, `board/board.ts`, `board/board.html`, `board/board.css`, `game-screen/game-screen.ts`, `game-screen/game-screen.html`, `models/game-state-response.ts`.

### **INVARIATI** (frontend)
`pawn/` (`pawn.ts`, `pawn.html`, `pawn.css`), `services/board-api.ts`, `services/game-api.ts`, gli altri modelli, `welcome/`, `player-setup/`, `winner/`, `app.*`, `proxy.conf.json`.

---

## 5. Decisioni di design

### 5.1 Tabellone — scheletro completo (frontend)

- **Griglia**: `BOARD_COLUMNS = 16`, `BOARD_ROWS = 12`, `CELL_SIZE = 50`. Coordinate in celle (x colonna, y riga, y = 0 in alto).
- **Scorrimento orizzontale**: l'SVG ha `width`/`height` pari a colonne/righe × 50 (1 unità = 1 px) dentro `.board-scroll` con `overflow-x: auto`. Motivo: `game-screen` è largo 600 px e il vecchio `max-width: 600px` sull'SVG rendeva illeggibili le etichette con 16+ colonne.
- **Percorso e coordinate** (le caselle intermedie sono interpolate sui tratti):
  - **0-7**: a destra, riga 11, colonne 0-7. 7 = Bivio A.
  - **Ramo in su dal Bivio A**: 10-12, colonna 7, righe 10-8. 12 = Bivio B.
  - **Ramo a destra dal Bivio A**: 40-42, riga 11, colonne 8-10. 42 = Bivio C.
  - **Dal Bivio B a sinistra**: 22-24 (riga 8, colonne 6-4), 25-27 (colonna 4, righe 7-5), 28-33 (riga 5, colonne 5-10). 33 = crea che entra nel cerchio da sinistra.
  - **Dal Bivio B a destra**: 16, 17 (riga 8, colonne 8, 9), 70 = Bivio D (10, 8), 71-72 (riga 8, colonne 11-12), 73 = crea (12, 7) che entra nel cerchio dal basso.
  - **Dal Bivio C in su**: 46, 47 (colonna 10, righe 10, 9) → 70.
  - **Dal Bivio C a destra**: 52-56 (riga 11, colonne 11-15), 57-62 (colonna 15, righe 10-5), 63 = crea (14, 5) che entra nel cerchio da destra.
  - **Checkpoint 81**: cerchio grande 3×3, angolo in alto a sinistra (11, 4), occupa colonne 11-13 e righe 4-6.
  - **Dal checkpoint all'arrivo** (disegno dell'utente, serpentina verso sinistra, 29 caselle da 82 a 110): 82-85 su (colonna 12, righe 3-0); 86-89 a sinistra (riga 0, colonne 11-8); 90-92 giù (colonna 8, righe 1-3); 93-95 a sinistra (riga 3, colonne 7-5); 96-98 su (colonna 5, righe 2-0); 99-102 a sinistra (riga 0, colonne 4-1); 103-110 giù (colonna 1, righe 1-8). Il 110 sta in (1, 8).
  - Le distanze tornano: il 70 è a 3 passi sia dal 12 sia dal 42; i tre crea (33, 63, 73) cadono al centro di tre lati del cerchio. Le posizioni senza casella (7-9 oltre il 7 e gli altri numeri vuoti, 80) non hanno coordinate e non si disegnano.
- **Checkpoint tondo**: `SQUARE_FOOTPRINTS` associa a una posizione lato in celle e forma (`'square' | 'circle'`); le caselle non indicate sono 1×1 quadrate. Il 81 è `{ span: 3, shape: 'circle' }`. Il template disegna un `<circle>` al posto del `<rect>`.
- **Pedine nel cerchio**: fascia pari all'80% del lato, al 75% dell'altezza (costanti `CIRCLE_PAWN_ROW_WIDTH_RATIO` e `CIRCLE_PAWN_ROW_HEIGHT_RATIO`), per restare dentro il cerchio; nelle caselle quadrate resta in basso a tutta larghezza.
- **Etichette**: partenza `partenza`; StartOver `0`; portali `0 N` con N = numero romano della foto (I=53 → `0 1`, II=23 → `0 2`, III=32 → `0 3`, IV=62 → `0 4`, V=27 → `0 5`, VI=58 → `0 6`); bivi con effetto prendono l'etichetta dell'effetto; "x2" e "giro bong" su due righe (`['h2o', 'x2']`, `['giro', 'bong']`). **81 e 110 senza etichetta** (mostrano il numero): da definire; nella foto si legge "JUMANJI" e "Fine", ma l'utente non ha risposto alla proposta.
- **Esteticamente**: confermato di fare prima tutto lo scheletro, poi curare le caselle specifiche (buchi neri per StartOver e portali, ecc.).

### 5.2 Board OG — correzione dei dati (backend)

- **Casella aggiunta dopo la partenza**: sul tabellone fisico subito dopo la partenza c'è un "2 tiri", poi obbligo e il resto. Nuova numerazione dell'inizio (i numeri 8 e 9 restano liberi, tutto dal 10 in poi resta com'era):

| Posizione | Contenuto |
|---|---|
| 0 | Partenza (vuota) |
| 1 | Obbligo (2 tiri) — **NUOVA** |
| 2 | Obbligo (generico) — era la 1 |
| 3 | Obbligo (turca) — era la 2 |
| 4 | Drink H2O (amount 1) — era la 3 |
| 5 | StartOver — era la 4 |
| 6 | *(no smoke — nessuna riga)* — era la 5 |
| **7** | **Bivio A** — next = [10, 40], previous = [6], effetto Snack (riga effetto id **1007**) — era la 6 |

- **80 rimosso**: l'80 era un bivio di solo instradamento introdotto perché `CheckpointSquare` calcolava il `previous` come `position - 1` e non poteva avere tre provenienze. Sul tabellone fisico non esiste: i tre crea sono attaccati al cerchio, quindi crea → checkpoint è **1 passo** (prima nel motore erano 2 e con un tiro di 1 ci si fermava sull'80 senza checkpoint).
- **Nuova topologia**: 33, 63 e 73 restano bivi di solo instradamento con effetto crea, ma con `next` = [81]. Il checkpoint 81 ha `previous` = [33, 63, 73] tramite `CheckpointSquare.previousPositions` (con lista vuota ricade su `position - 1`, comportamento base). Checkpoint 81 e arrivo 110 **invariati**; il percorso 82-109 è ancora di 28 caselle e poi l'arrivo.
- **Scelta tecnica**: la casella in più si ottiene spostando solo le vecchie 1-6 in 2-7 (sfruttando i numeri liberi 7-9) invece di rinumerare tutto; il costo è quindi limitato a seed, test ed etichette dell'inizio.

### 5.3 Motore — limite all'indietro (messaggi)

- Regola invariata: dopo il checkpoint non si va indietro oltre il checkpoint (`backwardAllowedAfterCheckpoint = false` sulla board OG); alla partenza non si va oltre lo 0.
- **Caso 1 — sei già sul pavimento e scegli "indietro"**: nessun movimento, nessun atterraggio (il checkpoint **non** si riattiva: prima il messaggio "ha raggiunto il checkpoint! ... 3 tiri di creazione" ricompariva), messaggio "turno sprecato". Gestito in `moveByChosenDirection` (scelta volontaria): dentro il ciclo di `moveBackwardByInternal` scatterebbe anche nella ripresa dopo la scelta a bivio, dove un passo il giocatore l'ha già fatto. Testi: sul checkpoint "Bravo {player}, hai sprecato un turno: non puoi andare indietro oltre il checkpoint"; alla partenza "Bravo {player}, hai sprecato un turno: non puoi andare indietro oltre la partenza" (se la posizione è ≤ 0 vale il testo della partenza, altrimenti quello del checkpoint).
- **Caso 2 — all'indietro si arriva al checkpoint con passi ancora da fare** (fermo obbligatorio): messaggio "{player} non può andare indietro oltre il checkpoint: il movimento si ferma qui", **in aggiunta** a quello del checkpoint raggiunto. Solo sul checkpoint; alla partenza non c'è messaggio (è sottinteso che non si va oltre).
- **Caso 3 — il muro blocca dopo qualche passo** (pavimento senza fermo obbligatorio): **nessun messaggio**, basta mostrare il movimento.
- I messaggi usano la lista dei messaggi del turno di `Game` (`addMessage`): il frontend li mostrava già fino al tiro successivo, nessuna modifica ad API e Angular per questi messaggi.
- **Regole future (non fatte, annotate)**: per regola di board, non chiedere la direzione a chi è sul checkpoint e mandarlo avanti; oppure far rimbalzare come all'arrivo (checkpoint come muro) con scelta di board tra far restare i giocatori o farli rimbalzare; stessa idea per le strade multiple che non si ricongiungono e non hanno traguardo (vie senza uscita). Il punto `isBackwardBlocked` è l'aggancio naturale.

### 5.4 Movimento visibile — strada A (backend + frontend)

- **Decisione**: strada **A**, riproduzione nel frontend. Il server risolve tutto subito come prima e restituisce il percorso; il frontend lo riproduce e applica lo stato finale alla fine. Il funzionamento del gioco non cambia (richiesta dell'utente: gli interessa solo mostrarlo meglio). La strada B (pausa vera sul server con nuova fase del turno) è stata scartata: più invasiva su `Game`.
- **Tipi di movimento** (`MovementType`): `STEP` passo normale (anche rimbalzo dall'arrivo e scelta di un bivio); `JUMP` salto (teletrasporto di un portale con collegamento unico, scelta di un portale); `RESET` ritorno al checkpoint dello StartOver, con messaggio.
- **Backend**: `Game.movementTrace` è una `List<Movement>` (`Movement(int position, MovementType type, String message)`); svuotata in `performRoll` e `performChoice` (la risposta della mossa contiene quindi solo il percorso di quell'azione, la risposta del tiro nessun percorso). `GameStateResponse` espone `movedPlayerIndex` (indice del giocatore che si è mosso, da `getLastPlayerToMove()`) e `movements`.
- **Frontend**:
  - `GameScreen` riproduce `state.movements` cella per cella (`STEP_DELAY_MS = 450`, poco più della transizione CSS di 0,4 s della pedina) e solo alla fine applica lo stato.
  - Durante la riproduzione la pedina si muove tramite il segnale `animatedPawn`; `boardPlayers` (computed) sovrascrive la posizione di quella pedina e il tabellone riceve quei giocatori.
  - Sul `RESET` con messaggio compare il messaggio con il pulsante "Continua": la pedina torna al checkpoint (scivolando in linea retta) solo dopo il click. Il percorso all'indietro non è unico (per esempio il 70 ha due `previous`) e non viene registrato, per questo il ritorno e i portali sono uno scivolamento in linea retta; i passi normali sono cella per cella.
  - `isBusy` resta attivo fino a fine riproduzione (non si azzera più in `complete`).
  - Alla vittoria si passa a `/winner` a movimento concluso.
- **Limiti noti**: durante la riproduzione stato, tabella e messaggi mostrano ancora quelli precedenti (si aggiornano alla fine); se si ricarica la pagina a metà riproduzione si vede subito lo stato finale; se si lascia la pagina durante una pausa la promessa pendente non viene risolta (innocuo).

### 5.5 StartOver

- Il messaggio dello StartOver è un template con `{player}`: "{player} è finito su uno start over: torna all'ultimo checkpoint (o alla partenza)". Viaggia **nell'evento `RESET`**, non nella lista `messages` del turno: si vede solo durante la pausa. Il testo è una proposta di Claude, modificabile nella costante di `StartOverSquare`.
- Decisione dell'utente: `game.landOnCurrentSquare(player)` **riattivato** in `StartOverSquare.applyEffect`: dopo il ritorno al checkpoint si attiva l'effetto del checkpoint (messaggio "ha raggiunto il checkpoint! ... 3 tiri di creazione" in `messages`, mostrato a fine movimento). Se il checkpoint è la partenza (0) l'atterraggio non fa nulla.
- Limite noto: il guardrail anti-loop (`activatedPositions`) impedisce di riattivare il checkpoint se era già stato attivato nella stessa catena.

---

## 6. Codice backend — versioni finali

### `model/Movement.java` — **NUOVO**
```java
package com.project.fumanji.model;

public record Movement(int position, MovementType type, String message) {
}
```

### `model/MovementType.java` — **NUOVO**
```java
package com.project.fumanji.model;

public enum MovementType {
    STEP,
    JUMP,
    RESET
}
```

### `model/Game.java` — **MODIFICATO** (solo questi membri; il resto della classe è **INVARIATO**)

Costanti, in cima alla classe:
```java
private static final String WASTED_TURN_AT_CHECKPOINT =
        "Bravo {player}, hai sprecato un turno: non puoi andare indietro oltre il checkpoint";
private static final String WASTED_TURN_AT_START =
        "Bravo {player}, hai sprecato un turno: non puoi andare indietro oltre la partenza";
private static final String STOPPED_AT_CHECKPOINT =
        "{player} non può andare indietro oltre il checkpoint: il movimento si ferma qui";
```

Campo della traccia (il tipo cambia da `List<Integer>` a `List<Movement>`):
```java
private final List<Movement> movementTrace = new ArrayList<>();
```

`performRoll` e `performChoice` (svuotano anche la traccia; `performMove` **INVARIATO**):
```java
public synchronized int performRoll() {
    checkTurnAction(TurnPhase.WAITING_ROLL);
    turnMessages.clear();
    movementTrace.clear();
    int rollValue = roll(getCurrentPlayer());
    turnPhase = TurnPhase.WAITING_DIRECTION;
    return rollValue;
}

public synchronized void performChoice(int chosenPosition) {
    checkTurnAction(TurnPhase.WAITING_CHOICE);
    movementTrace.clear();
    chooseDestination(getCurrentPlayer(), chosenPosition);
    resolveTurnPhase();
}
```

`moveByChosenDirection` e il nuovo `isBackwardBlocked`:
```java
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

private boolean isBackwardBlocked(Player player) {
    if (player.getPosition() <= 0) {
        return true;
    }
    return !board.isBackwardAllowedAfterCheckpoint() && player.getPosition() <= player.getCheckpoint();
}
```

`moveBackwardByInternal` (i due `if` dei pavimenti diventano `isBackwardBlocked`; il fermo obbligatorio emette il messaggio quando restano passi):
```java
private void moveBackwardByInternal(Player player, int steps, boolean respectCheckpoint) {
    int remaining = steps;

    while (remaining > 0) {
        if (isBackwardBlocked(player)) {
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
        if (respectCheckpoint && nextSquare.isMandatoryStop()) {
            if (remaining > 0) {
                addMessage(STOPPED_AT_CHECKPOINT.replace("{player}", player.getName()));
            }
            remaining = 0;
        }
    }

    doLandOnCurrentSquare(player);
}
```

`movePlayerTo` (due versioni; quella a 2 argomenti registra un `STEP`):
```java
private void movePlayerTo(Player player, int position) {
    movePlayerTo(player, position, MovementType.STEP, null);
}

private void movePlayerTo(Player player, int position, MovementType type, String message) {
    player.setPosition(position);
    movementTrace.add(new Movement(position, type, message));
}
```

`teleportTo` e `resetToStart`:
```java
public void teleportTo(Player player, int position) {
    movePlayerTo(player, position, MovementType.JUMP, null);
}

public void resetToStart(Player player, String message) {
    movePlayerTo(player, player.getCheckpoint(), MovementType.RESET, message);
}
```

`chooseDestination`: la sola riga cambiata (bivio = passo, portale = salto):
```java
movePlayerTo(waitingPlayer, chosenPosition, continueAfterChoice ? MovementType.STEP : MovementType.JUMP, null);
```

`getMovementTrace` (sincronizzato: il `GET` può leggerla mentre una mossa la scrive); `clearMovementTrace()` è **INVARIATO**:
```java
public synchronized List<Movement> getMovementTrace() {
    return List.copyOf(movementTrace);
}
```

### `model/squares/CheckpointSquare.java` — **MODIFICATO** (versione finale completa)
Costruttore unico (il costruttore a 1 argomento è **RIMOSSO**: con due costruttori pubblici MapStruct dà "Ambiguous constructors"); chi non ha provenienze esplicite passa `List.of()`.
```java
package com.project.fumanji.model.squares;

import java.util.List;

import com.project.fumanji.model.Game;
import com.project.fumanji.model.Player;
import com.project.fumanji.model.Square;

public class CheckpointSquare extends Square {

    private static final String MESSAGE_TEMPLATE =
            "{player} ha raggiunto il checkpoint! Quindi deve fare 3 tiri di creazione";

    private final List<Integer> previousPositions;

    public CheckpointSquare(int position, List<Integer> previousPositions) {
        super(position);
        this.previousPositions = previousPositions;
    }

    @Override
    public void applyEffect(Player player, Game game) {
        game.updateCheckpoint(player);
        game.addMessage(buildMessage(player));
    }

    public String buildMessage(Player player) {
        return MESSAGE_TEMPLATE.replace("{player}", player.getName());
    }

    @Override
    public String getDescription() {
        return MESSAGE_TEMPLATE;
    }

    @Override
    public List<Integer> getPreviousPositions() {
        return previousPositions.isEmpty() ? super.getPreviousPositions() : previousPositions;
    }

    @Override
    public boolean isMandatoryStop() {
        return true;
    }
}
```

### `model/squares/StartOverSquare.java` — **MODIFICATO** (versione finale completa)
```java
package com.project.fumanji.model.squares;

import com.project.fumanji.model.Game;
import com.project.fumanji.model.Player;
import com.project.fumanji.model.Square;

public class StartOverSquare extends Square {

    private static final String MESSAGE_TEMPLATE =
            "{player} è finito su uno start over: torna all'ultimo checkpoint (o alla partenza)";

    public StartOverSquare(int position) {
        super(position);
    }

    @Override
    public void applyEffect(Player player, Game game) {
        game.resetToStart(player, buildMessage(player));
        game.landOnCurrentSquare(player);
    }

    public String buildMessage(Player player) {
        return MESSAGE_TEMPLATE.replace("{player}", player.getName());
    }

    @Override
    public String getDescription() {
        return "Torna all'ultimo checkpoint (o alla partenza)";
    }
}
```

### `model/squares/BivioSquare.java` — **versione reale** (correzione al main dump; così com'è nel file dell'utente)
```java
package com.project.fumanji.model.squares;

import java.util.List;

import com.project.fumanji.model.Game;
import com.project.fumanji.model.InteractiveSquare;
import com.project.fumanji.model.Player;
import com.project.fumanji.model.Square;


public class BivioSquare extends Square implements InteractiveSquare {

    private final List<Integer> nextPositions;
    private final List<Integer> previousPositions;
    private final Square effect; // nullable, es. una ObligationSquare usata solo per il suo applyEffect

    public BivioSquare(int position, List<Integer> nextPositions, List<Integer> previousPositions, Square effect) {
        super(position);
        this.nextPositions = nextPositions;
        this.previousPositions = previousPositions;
        this.effect = effect;
    }

    @Override
    public void applyEffect(Player player, Game game) {
        if (effect != null) {
            effect.applyEffect(player, game);
        }
    }

    @Override
    public String getDescription() {
        return effect != null ? effect.getDescription() : null;
    }

    @Override
    public List<Integer> getNextPositions() { return nextPositions; }

    @Override
    public List<Integer> getPreviousPositions() { return previousPositions; }
}
```

### `entity/squares/CheckpointSquareEntity.java` — **MODIFICATO** (versione finale completa; era `persistence/squares/`)
Il costruttore vuoto `protected` è obbligatorio per Hibernate.
```java
package com.project.fumanji.entity.squares;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;

import com.project.fumanji.entity.SquareEntity;

@Entity
public class CheckpointSquareEntity extends SquareEntity {

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "checkpoint_square_previous", joinColumns = @JoinColumn(name = "square_id"))
    @Column(name = "previous_position")
    private List<Integer> previousPositions = new ArrayList<>();

    protected CheckpointSquareEntity() {
    }

    public CheckpointSquareEntity(int position) {
        this(position, List.of());
    }

    public CheckpointSquareEntity(int position, List<Integer> previousPositions) {
        super(position);
        this.previousPositions = new ArrayList<>(previousPositions);
    }

    public List<Integer> getPreviousPositions() {
        return previousPositions;
    }
}
```
`SquareMapper` **INVARIATO**: abbina `previousPositions` per nome. Tabella nuova creata da Hibernate: `checkpoint_square_previous (square_id, previous_position)`.

### `dto/MovementResponse.java` — **NUOVO**
```java
package com.project.fumanji.dto;

import com.project.fumanji.model.MovementType;

public record MovementResponse(int position, MovementType type, String message) {
}
```

### `dto/GameStateResponse.java` — **MODIFICATO** (versione finale completa)
`movedPlayerIndex` e `movements` sono assenti dal JSON quando nulli (`default-property-inclusion=non_null`); `message` di un movimento è assente tranne che nel `RESET`.
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
        String winnerName,
        List<String> messages,
        Integer movedPlayerIndex,
        List<MovementResponse> movements) {
}
```

### `mapper/GameStateAssembler.java` — **MODIFICATO** (metodo `toResponse`; import di `MovementResponse` da aggiungere)
```java
public GameStateResponse toResponse(Game game) {
    List<PlayerStateResponse> players = game.getPlayers().stream()
            .map(player -> new PlayerStateResponse(player.getName(), player.getPosition(),
                    player.getSnack(), player.getDrink(), player.getCheckpoint()))
            .toList();

    List<Integer> choiceOptions = game.isAwaitingChoice() ? List.copyOf(game.getChoiceOptions()) : null;
    String winnerName = game.getWinner() != null ? game.getWinner().getName() : null;
    List<String> turnMessages = game.getTurnMessages();
    List<MovementResponse> movements = game.getMovementTrace().stream()
            .map(movement -> new MovementResponse(movement.position(), movement.type(), movement.message()))
            .toList();
    Integer movedPlayerIndex = movements.isEmpty()
            ? null
            : game.getPlayers().indexOf(game.getLastPlayerToMove());

    return new GameStateResponse(game.getCurrentPlayer().getName(), game.getTurnPhase(),
            game.getLastRoll(), players, choiceOptions, winnerName,
            turnMessages.isEmpty() ? null : turnMessages,
            movedPlayerIndex, movements.isEmpty() ? null : movements);
}
```

### **INVARIATI** (backend, quelli che potrebbero sembrare toccati)
`Board`, `Dice`, `Player`, `Square` (resta con `getDescription()` del delta 01-10), `InteractiveSquare`, `TurnPhase`, tutte le altre caselle e le altre entity, `SquareMapper`, `BoardAssembler`, `BoardResponseAssembler` (usa `getPreviousPositions()`, quindi l'81 ora espone [33, 63, 73] senza modifiche), `BoardController`, `BoardService`, `GameService`, `GameRegistry`, `EndGameService`, `GameConfig`, `GameController`, `GlobalExceptionHandler`, `pom.xml`, `application.properties`. `playTurn()` resta com'è (non usa le fasi; la sua traccia non viene mai svuotata, ma non è usato da nessun endpoint).

---

## 7. Seed della board OG — **MODIFICATO** (versione finale completa; identico in `src/test/resources/seed-og-board.sql`)

Differenze rispetto al main dump: nuova casella 1; 2-7 = vecchie 1-6; bivio A al 7 con riga effetto 1007; **tolta** ogni riga dell'80; `next` di 33, 63, 73 = 81; nuova tabella `checkpoint_square_previous`. Restano 68 caselle piazzate.

```sql
-- BOARD
INSERT INTO board_entity (id, finish_position, direction_choice_enabled, mandatory_checkpoint_enabled, backward_allowed_after_checkpoint)
VALUES (1, 110, 1, 1, 0);

-- SQUARE_ENTITY: righe posizionate (board_id = 1) + righe "effetto" (board_id = NULL, position = 0)
INSERT INTO square_entity (id, position, board_id, square_type) VALUES
(1, 1, 1, 'ObligationSquareEntity'),
(2, 2, 1, 'ObligationSquareEntity'),
(3, 3, 1, 'ObligationSquareEntity'),
(4, 4, 1, 'DrinkSquareEntity'),
(5, 5, 1, 'StartOverSquareEntity'),
(7, 7, 1, 'BivioSquareEntity'),
(10, 10, 1, 'ObligationSquareEntity'),
(11, 11, 1, 'ObligationSquareEntity'),
(12, 12, 1, 'BivioSquareEntity'),
(16, 16, 1, 'StartOverSquareEntity'),
(17, 17, 1, 'BivioSquareEntity'),
(22, 22, 1, 'DrinkSquareEntity'),
(23, 23, 1, 'PortalSquareEntity'),
(25, 25, 1, 'ObligationSquareEntity'),
(26, 26, 1, 'ObligationSquareEntity'),
(27, 27, 1, 'PortalSquareEntity'),
(28, 28, 1, 'SnackSquareEntity'),
(29, 29, 1, 'ObligationSquareEntity'),
(30, 30, 1, 'ObligationSquareEntity'),
(31, 31, 1, 'DrinkSquareEntity'),
(32, 32, 1, 'PortalSquareEntity'),
(33, 33, 1, 'BivioSquareEntity'),
(40, 40, 1, 'ObligationSquareEntity'),
(41, 41, 1, 'ObligationSquareEntity'),
(42, 42, 1, 'BivioSquareEntity'),
(46, 46, 1, 'StartOverSquareEntity'),
(47, 47, 1, 'BivioSquareEntity'),
(52, 52, 1, 'DrinkSquareEntity'),
(53, 53, 1, 'PortalSquareEntity'),
(55, 55, 1, 'ObligationSquareEntity'),
(56, 56, 1, 'SnackSquareEntity'),
(57, 57, 1, 'ObligationSquareEntity'),
(58, 58, 1, 'PortalSquareEntity'),
(59, 59, 1, 'ObligationSquareEntity'),
(60, 60, 1, 'DrinkSquareEntity'),
(61, 61, 1, 'ObligationSquareEntity'),
(62, 62, 1, 'PortalSquareEntity'),
(63, 63, 1, 'BivioSquareEntity'),
(70, 70, 1, 'BivioSquareEntity'),
(71, 71, 1, 'SnackSquareEntity'),
(72, 72, 1, 'StartOverSquareEntity'),
(73, 73, 1, 'BivioSquareEntity'),
(81, 81, 1, 'CheckpointSquareEntity'),
(82, 82, 1, 'ObligationSquareEntity'),
(83, 83, 1, 'DrinkSquareEntity'),
(84, 84, 1, 'SnackSquareEntity'),
(86, 86, 1, 'ObligationSquareEntity'),
(87, 87, 1, 'DrinkSquareEntity'),
(88, 88, 1, 'StartOverSquareEntity'),
(89, 89, 1, 'ObligationSquareEntity'),
(90, 90, 1, 'ObligationSquareEntity'),
(91, 91, 1, 'ObligationSquareEntity'),
(92, 92, 1, 'ObligationSquareEntity'),
(93, 93, 1, 'StartOverSquareEntity'),
(94, 94, 1, 'DrinkSquareEntity'),
(95, 95, 1, 'SnackSquareEntity'),
(96, 96, 1, 'StartOverSquareEntity'),
(98, 98, 1, 'ObligationSquareEntity'),
(99, 99, 1, 'ObligationSquareEntity'),
(100, 100, 1, 'ObligationSquareEntity'),
(102, 102, 1, 'SnackSquareEntity'),
(103, 103, 1, 'StartOverSquareEntity'),
(104, 104, 1, 'DrinkSquareEntity'),
(105, 105, 1, 'ObligationSquareEntity'),
(106, 106, 1, 'StartOverSquareEntity'),
(107, 107, 1, 'ObligationSquareEntity'),
(108, 108, 1, 'ObligationSquareEntity'),
(109, 109, 1, 'StartOverSquareEntity'),
-- righe "effetto" dei bivi: nessuna board (non sono posizionate), position=0 = placeholder inerte
(1007, 0, NULL, 'SnackSquareEntity'),
(1012, 0, NULL, 'ObligationSquareEntity'),
(1042, 0, NULL, 'ObligationSquareEntity'),
(1070, 0, NULL, 'StartOverSquareEntity'),
(1033, 0, NULL, 'ObligationSquareEntity'),
(1047, 0, NULL, 'DrinkSquareEntity'),
(1063, 0, NULL, 'ObligationSquareEntity'),
(1073, 0, NULL, 'ObligationSquareEntity');

-- OBLIGATION: messaggi (positioned + effetto)
INSERT INTO obligation_square_entity (id, message) VALUES
(1, '{player} deve fare 2 tiri'),
(2, '{player} deve dare un obbligo a qualcuno'),
(3, 'tutti i giocatori devono fare una turca (almeno 3 tiri a giro)'),
(10, '{player} deve fare 2 tiri'),
(11, 'tutti i giocatori devono fare una turca (almeno 3 tiri a giro)'),
(25, 'tutti i giocatori devono fare una turca (almeno 3 tiri a giro)'),
(26, '{player} deve dare un obbligo a qualcuno'),
(29, '{player} deve fare 2 tiri'),
(30, '{player} deve dare un obbligo a qualcuno'),
(40, '{player} deve fare 2 tiri'),
(41, '{player} deve dare un obbligo a qualcuno'),
(55, 'tutti i giocatori devono fare una turca (almeno 3 tiri a giro)'),
(57, '{player} deve fare 2 tiri'),
(59, '{player} deve dare un obbligo a qualcuno'),
(61, 'tutti i giocatori devono fare una turca (almeno 3 tiri a giro)'),
(82, '{player} deve fare 3 tiri di creazione'),
(86, '{player} deve dare un obbligo a qualcuno'),
(89, '{player} deve dare un obbligo a qualcuno'),
(90, '{player} deve fare 3 tiri di creazione'),
(91, '{player} deve fare un hit di bong o pipa'),
(92, 'tutti i giocatori devono fare una turca (almeno 3 tiri a giro)'),
(98, 'tutti i giocatori devono fare una turca (almeno 3 tiri a giro)'),
(99, '{player} deve fare 2 tiri'),
(100, '{player} deve dare un obbligo a qualcuno'),
(105, 'tutti devono fare un hit di bong o pipa'),
(107, '{player} deve fare un hit di bong o pipa'),
(108, '{player} deve fare 3 tiri di creazione'),
(1012, '{player} deve dare un obbligo a qualcuno'),
(1042, 'tutti i giocatori devono fare una turca (almeno 3 tiri a giro)'),
(1033, '{player} deve fare 3 tiri di creazione'),
(1063, '{player} deve fare 3 tiri di creazione'),
(1073, '{player} deve fare 3 tiri di creazione');

-- DRINK (amount 1 di default, 2 dove segnato "x2")
INSERT INTO drink_square_entity (id, amount) VALUES
(4, 1), (22, 1), (31, 1), (52, 1), (60, 1),
(83, 2), (87, 1), (94, 2), (104, 2),
(1047, 1);

-- SNACK
INSERT INTO snack_square_entity (id, amount) VALUES
(28, 1), (56, 1), (71, 1), (84, 1), (95, 1), (102, 1),
(1007, 1);

-- STARTOVER
INSERT INTO start_over_square_entity (id) VALUES
(5), (16), (46), (72), (88), (93), (96), (103), (106), (109),
(1070);

-- CHECKPOINT
INSERT INTO checkpoint_square_entity (id) VALUES (81);

-- CHECKPOINT: provenienze (i tre crea che entrano nel cerchio)
INSERT INTO checkpoint_square_previous (square_id, previous_position) VALUES
(81, 33), (81, 63), (81, 73);

-- PORTAL
INSERT INTO portal_square_entity (id) VALUES (23), (27), (32), (53), (58), (62);

INSERT INTO portal_square_link (square_id, linked_position) VALUES
(53, 23), (53, 32), (53, 62), (53, 27), (53, 58),
(23, 53), (23, 32), (23, 62), (23, 27), (23, 58),
(32, 53), (32, 23), (32, 62), (32, 27), (32, 58),
(62, 53), (62, 23), (62, 32), (62, 27), (62, 58),
(27, 53), (27, 23), (27, 32), (27, 62), (27, 58),
(58, 53), (58, 23), (58, 32), (58, 62), (58, 27);

-- BIVIO (id, effect_square_id nullable)
INSERT INTO bivio_square_entity (id, effect_square_id) VALUES
(7, 1007),
(12, 1012),
(17, NULL),
(33, 1033),
(42, 1042),
(47, 1047),
(63, 1063),
(70, 1070),
(73, 1073);

-- NEXT / PREVIOUS POSITIONS DEI BIVI
INSERT INTO bivio_square_next (square_id, next_position) VALUES
(7, 10), (7, 40),
(12, 16), (12, 22),
(17, 70),
(33, 81),
(42, 46), (42, 52),
(47, 70),
(63, 81),
(70, 71),
(73, 81);

INSERT INTO bivio_square_previous (square_id, previous_position) VALUES
(7, 6),
(12, 11),
(17, 16),
(33, 32),
(42, 41),
(47, 46),
(63, 62),
(70, 17), (70, 47),
(73, 72);
```

---

## 8. Test

### `entity/BoardAssemblerIT.java` — **MODIFICATO** (versione finale completa; era `persistence/BoardAssemblerIT.java`)
Cambiamenti: bivio A al 7 con `previous` [6]; verifica dei `previous` dell'81 (se MapStruct scegliesse il costruttore sbagliato fallirebbe); caselle 3 (obbligo), 4 (drink), 5 (StartOver), 6 (buco no smoke); verifiche su 33, 63, 73 con `next` [81]; **RIMOSSE** le verifiche sull'80; `assertDrinkAmount` del 3 diventa del 4.
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

Import: quelli dei vecchi package passano ai nuovi (`com.project.fumanji.model.Board`, `...model.ControllableDice`, `...model.Game`, `...model.Player`); in più `com.project.fumanji.model.squares.CheckpointSquare` e `org.springframework.test.web.servlet.ResultActions`.

**Test modificati** (tiro 7 → 8; testo del messaggio della casella 1, che ora è il "2 tiri"):
```java
@Test
void choiceMovesPlayerAndContinuesRemainingSteps() throws Exception {
    String gameId = createGame();
    dice.setNextRoll(8);

    mockMvc.perform(post("/api/games/{gameId}/roll", gameId))
            .andExpect(status().isOk());

    mockMvc.perform(post("/api/games/{gameId}/move", gameId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {"forward": true}
                            """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.turnPhase", is("WAITING_CHOICE")))
            .andExpect(jsonPath("$.choiceOptions", containsInAnyOrder(10, 40)));

    mockMvc.perform(post("/api/games/{gameId}/choice", gameId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {"position": 10}
                            """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.currentPlayerName", is("Luigi")))
            .andExpect(jsonPath("$.turnPhase", is("WAITING_ROLL")))
            .andExpect(jsonPath("$.players[0].position", is(10)))
            .andExpect(jsonPath("$.choiceOptions").doesNotExist());
}

@Test
void rejectsInvalidChoice() throws Exception {
    String gameId = createGame();
    dice.setNextRoll(8);

    mockMvc.perform(post("/api/games/{gameId}/roll", gameId))
            .andExpect(status().isOk());
    mockMvc.perform(post("/api/games/{gameId}/move", gameId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {"forward": true}
                            """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.turnPhase", is("WAITING_CHOICE")));

    mockMvc.perform(post("/api/games/{gameId}/choice", gameId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {"position": 99}
                            """))
            .andExpect(status().isBadRequest());
}

@Test
void returnsObligationMessageWhenLandingOnObligationSquare() throws Exception {
    String gameId = createGame();
    dice.setNextRoll(1);

    mockMvc.perform(post("/api/games/{gameId}/roll", gameId))
            .andExpect(status().isOk());
    mockMvc.perform(post("/api/games/{gameId}/move", gameId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {"forward": true}
                            """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.messages", hasSize(1)))
            .andExpect(jsonPath("$.messages[0]", is("Mario deve fare 2 tiri")));
}
```
`clearsMessagesOnNextRoll` e tutti gli altri test sono **INVARIATI**.

**Helper nuovi** (**NUOVO**):
```java
private UUID registerGame(Board board) {
    return gameRegistry.register(new Game(board, List.of(new Player("Mario")), dice));
}

private Board boardWithCheckpointAt3() {
    return Board.builder(10)
            .square(3, new CheckpointSquare(3, List.of()))
            .directionChoiceEnabled(true)
            .mandatoryCheckpointEnabled(true)
            .backwardAllowedAfterCheckpoint(false)
            .build();
}

private void roll(UUID gameId) throws Exception {
    mockMvc.perform(post("/api/games/{gameId}/roll", gameId)).andExpect(status().isOk());
}

private ResultActions move(UUID gameId, boolean forward) throws Exception {
    return mockMvc.perform(post("/api/games/{gameId}/move", gameId)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"forward\": " + forward + "}"));
}
```

**Test nuovi** (**NUOVO**, 5):
```java
@Test
void wastesTurnWhenChoosingBackwardOnCheckpoint() throws Exception {
    UUID gameId = registerGame(boardWithCheckpointAt3());
    dice.setNextRoll(3);

    roll(gameId);
    move(gameId, true).andExpect(status().isOk());

    roll(gameId);
    move(gameId, false)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.players[0].position", is(3)))
            .andExpect(jsonPath("$.messages", hasSize(1)))
            .andExpect(jsonPath("$.messages[0]",
                    is("Bravo Mario, hai sprecato un turno: non puoi andare indietro oltre il checkpoint")));
}

@Test
void wastesTurnWhenChoosingBackwardOnStart() throws Exception {
    UUID gameId = registerGame(Board.builder(10).directionChoiceEnabled(true).build());
    dice.setNextRoll(2);

    roll(gameId);
    move(gameId, false)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.players[0].position", is(0)))
            .andExpect(jsonPath("$.messages", hasSize(1)))
            .andExpect(jsonPath("$.messages[0]",
                    is("Bravo Mario, hai sprecato un turno: non puoi andare indietro oltre la partenza")));
}

@Test
void warnsWhenBackwardMovementStopsAtCheckpointWithStepsLeft() throws Exception {
    UUID gameId = registerGame(boardWithCheckpointAt3());
    dice.setNextRoll(3);

    roll(gameId);
    move(gameId, true).andExpect(status().isOk());
    roll(gameId);
    move(gameId, true).andExpect(status().isOk());

    dice.setNextRoll(5);
    roll(gameId);
    move(gameId, false)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.players[0].position", is(3)))
            .andExpect(jsonPath("$.messages", hasSize(2)))
            .andExpect(jsonPath("$.messages[0]",
                    is("Mario non può andare indietro oltre il checkpoint: il movimento si ferma qui")));
}

@Test
void returnsWalkedStepsAsMovements() throws Exception {
    String gameId = createGame();
    dice.setNextRoll(2);

    mockMvc.perform(post("/api/games/{gameId}/roll", gameId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.movements").doesNotExist());

    mockMvc.perform(post("/api/games/{gameId}/move", gameId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {"forward": true}
                            """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.movedPlayerIndex", is(0)))
            .andExpect(jsonPath("$.movements", hasSize(2)))
            .andExpect(jsonPath("$.movements[0].position", is(1)))
            .andExpect(jsonPath("$.movements[0].type", is("STEP")))
            .andExpect(jsonPath("$.movements[1].position", is(2)))
            .andExpect(jsonPath("$.movements[1].type", is("STEP")));
}

@Test
void returnsResetMovementWithMessageOnStartOver() throws Exception {
    String gameId = createGame();
    dice.setNextRoll(5);

    mockMvc.perform(post("/api/games/{gameId}/roll", gameId))
            .andExpect(status().isOk());
    mockMvc.perform(post("/api/games/{gameId}/move", gameId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {"forward": true}
                            """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.movements", hasSize(6)))
            .andExpect(jsonPath("$.movements[4].position", is(5)))
            .andExpect(jsonPath("$.movements[5].position", is(0)))
            .andExpect(jsonPath("$.movements[5].type", is("RESET")))
            .andExpect(jsonPath("$.movements[5].message",
                    is("Mario è finito su uno start over: torna all'ultimo checkpoint (o alla partenza)")))
            .andExpect(jsonPath("$.players[0].position", is(0)));
}
```
Nei test sul limite all'indietro la partita ha **un solo giocatore** (`registerGame`), così il turno torna sempre a Mario. Nel terzo test il secondo messaggio è quello del checkpoint raggiunto.

### `model/ControllableDice.java` — **INVARIATO** nel contenuto (solo package: dal vecchio `entity` a `model`).

---

## 9. Codice frontend — versioni finali

### `models/movement-type.ts` — **NUOVO**
```typescript
export type MovementType = 'STEP' | 'JUMP' | 'RESET';
```

### `models/movement-response.ts` — **NUOVO**
```typescript
import { MovementType } from './movement-type';

export interface MovementResponse {
  position: number;
  type: MovementType;
  // Assente dalla risposta quando non c'è (il backend omette i valori null)
  message?: string;
}
```

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
  winnerName?: string;
  // Messaggi generati dalle caselle durante il turno; assenti dalla risposta quando non ce ne sono
  messages?: string[];
  // Indice del giocatore che si è mosso e posizioni attraversate durante l'azione; assenti se non c'è stato movimento
  movedPlayerIndex?: number;
  movements?: MovementResponse[];
}
```

### `board/board-layout.ts` — **MODIFICATO** (versione finale completa)
**RIMOSSI** rispetto alle versioni intermedie della sessione: `SHARED_SQUARES` e `drawnPosition` (alias 80→81, non più necessario perché l'80 non esiste).
```typescript
// Coordinate in celle della griglia: x = colonna, y = riga (la riga 0 è in alto)
export interface Point {
  x: number;
  y: number;
}

// Tratto di caselle consecutive (da `from` a `to`, estremi inclusi) disposte in linea retta tra i due punti chiave
interface Segment {
  from: number;
  to: number;
}

// Lato di una casella in unità SVG e dimensioni della griglia in celle
export const CELL_SIZE = 50;
export const BOARD_COLUMNS = 16;
export const BOARD_ROWS = 12;

// Punti chiave posizionati a mano (in celle)
const KEY_POINTS: Record<number, Point> = {
  // Partenza e bivio A
  0: { x: 0, y: 11 },
  7: { x: 7, y: 11 },
  // Ramo in su dal bivio A (12 = bivio B)
  10: { x: 7, y: 10 },
  12: { x: 7, y: 8 },
  // Ramo a sinistra dal bivio B, fino al crea (33)
  22: { x: 6, y: 8 },
  24: { x: 4, y: 8 },
  25: { x: 4, y: 7 },
  27: { x: 4, y: 5 },
  28: { x: 5, y: 5 },
  33: { x: 10, y: 5 },
  // Ramo a destra dal bivio B, bivio D (70) e coda fino al crea (73)
  16: { x: 8, y: 8 },
  17: { x: 9, y: 8 },
  70: { x: 10, y: 8 },
  72: { x: 12, y: 8 },
  73: { x: 12, y: 7 },
  // Ramo a destra dal bivio A (42 = bivio C), ramo in su (46-47) e ramo a destra fino al crea (63)
  40: { x: 8, y: 11 },
  42: { x: 10, y: 11 },
  46: { x: 10, y: 10 },
  47: { x: 10, y: 9 },
  52: { x: 11, y: 11 },
  56: { x: 15, y: 11 },
  57: { x: 15, y: 10 },
  62: { x: 15, y: 5 },
  63: { x: 14, y: 5 },
  // Checkpoint: angolo in alto a sinistra del blocco 3x3 occupato dal cerchio
  81: { x: 11, y: 4 },
  // Dal checkpoint all'arrivo: su, a sinistra, giù, a sinistra, su, a sinistra, giù
  82: { x: 12, y: 3 },
  84: { x: 12, y: 1 },
  85: { x: 12, y: 0 },
  89: { x: 8, y: 0 },
  90: { x: 8, y: 1 },
  92: { x: 8, y: 3 },
  93: { x: 7, y: 3 },
  95: { x: 5, y: 3 },
  96: { x: 5, y: 2 },
  98: { x: 5, y: 0 },
  99: { x: 4, y: 0 },
  102: { x: 1, y: 0 },
  103: { x: 1, y: 1 },
  110: { x: 1, y: 8 },
};

// Tratti da interpolare: le caselle intermedie si distribuiscono in modo uniforme tra gli estremi
const SEGMENTS: Segment[] = [
  { from: 0, to: 7 },
  { from: 10, to: 12 },
  { from: 22, to: 24 },
  { from: 25, to: 27 },
  { from: 28, to: 33 },
  { from: 40, to: 42 },
  { from: 52, to: 56 },
  { from: 57, to: 62 },
  { from: 70, to: 72 },
  { from: 82, to: 84 },
  { from: 85, to: 89 },
  { from: 90, to: 92 },
  { from: 93, to: 95 },
  { from: 96, to: 98 },
  { from: 99, to: 102 },
  { from: 103, to: 110 },
];

// Forma di una casella
export type SquareShape = 'square' | 'circle';

// Ingombro di una casella: lato in celle e forma
export interface SquareFootprint {
  span: number;
  shape: SquareShape;
}

// Le caselle non indicate in SQUARE_FOOTPRINTS sono quadrate di una cella
export const DEFAULT_FOOTPRINT: SquareFootprint = { span: 1, shape: 'square' };

export const SQUARE_FOOTPRINTS: Readonly<Partial<Record<number, SquareFootprint>>> = {
  // Checkpoint: cerchio grande, la coordinata è l'angolo in alto a sinistra del blocco 3x3
  81: { span: 3, shape: 'circle' },
};

// Costruisce la mappa posizione -> coordinate: punti chiave più caselle interpolate dei tratti
function buildLayout(): ReadonlyMap<number, Point> {
  const layout = new Map<number, Point>();

  for (const [position, point] of Object.entries(KEY_POINTS)) {
    layout.set(Number(position), point);
  }

  for (const { from, to } of SEGMENTS) {
    const start = KEY_POINTS[from];
    const end = KEY_POINTS[to];
    for (let position = from + 1; position < to; position++) {
      const ratio = (position - from) / (to - from);
      layout.set(position, {
        x: start.x + (end.x - start.x) * ratio,
        y: start.y + (end.y - start.y) * ratio,
      });
    }
  }

  return layout;
}

export const BOARD_LAYOUT = buildLayout();
```

### `board/board-labels.ts` — **MODIFICATO** (versione finale completa)
81 e 110 non hanno etichetta (mostrano il numero).
```typescript
// Etichette mostrate nelle caselle, per posizione (board OG): dato visivo statico,
// da spostare nel backend insieme alle coordinate.
// Ogni elemento dell'array è una riga dell'etichetta
export const SQUARE_LABELS: Readonly<Partial<Record<number, readonly string[]>>> = {
  0: ['partenza'],
  1: ['2 tiri'],
  2: ['obbligo'],
  3: ['turca'],
  4: ['h2o'],
  // StartOver: per ora "0" al posto del buco nero
  5: ['0'],
  6: ['no smoke'],
  // Bivio A: prende l'etichetta del suo effetto (snack)
  7: ['schimica'],
  // Ramo in su: 12 è il Bivio B, prende l'etichetta del suo effetto (obbligo)
  10: ['2 tiri'],
  11: ['turca'],
  12: ['obbligo'],
  // Ramo a destra: 42 è il Bivio C, prende l'etichetta del suo effetto (turca)
  40: ['2 tiri'],
  41: ['obbligo'],
  42: ['turca'],
  // Ramo a destra dal bivio B (70 = bivio D, prende l'etichetta del suo effetto)
  16: ['0'],
  17: ['no smoke'],
  70: ['0'],
  71: ['schimica'],
  72: ['0'],
  73: ['crea'],
  // Ramo a sinistra dal bivio B (23, 27, 32 = portali; 33 = bivio con effetto crea)
  22: ['h2o'],
  23: ['0 2'],
  24: ['no smoke'],
  25: ['turca'],
  26: ['obbligo'],
  27: ['0 5'],
  28: ['schimica'],
  29: ['2 tiri'],
  30: ['obbligo'],
  31: ['h2o'],
  32: ['0 3'],
  33: ['crea'],
  // Ramo in su dal bivio C (47 = bivio con effetto drink)
  46: ['0'],
  47: ['h2o'],
  // Ramo a destra dal bivio C (53, 58, 62 = portali; 63 = bivio con effetto crea)
  52: ['h2o'],
  53: ['0 1'],
  54: ['no smoke'],
  55: ['turca'],
  56: ['schimica'],
  57: ['2 tiri'],
  58: ['0 6'],
  59: ['obbligo'],
  60: ['h2o'],
  61: ['turca'],
  62: ['0 4'],
  63: ['crea'],
  // Dal checkpoint all'arrivo (81 e 110 senza etichetta: mostrano il numero)
  82: ['crea'],
  83: ['h2o', 'x2'],
  84: ['schimica'],
  85: ['no smoke'],
  86: ['obbligo'],
  87: ['h2o'],
  88: ['0'],
  89: ['obbligo'],
  90: ['crea'],
  91: ['bong'],
  92: ['turca'],
  93: ['0'],
  94: ['h2o', 'x2'],
  95: ['schimica'],
  96: ['0'],
  97: ['no smoke'],
  98: ['turca'],
  99: ['2 tiri'],
  100: ['obbligo'],
  101: ['no smoke'],
  102: ['schimica'],
  103: ['0'],
  104: ['h2o', 'x2'],
  105: ['giro', 'bong'],
  106: ['0'],
  107: ['bong'],
  108: ['crea'],
  109: ['0'],
};
```

### `board/board.ts` — **MODIFICATO** (versione finale completa)
*(l'ordine degli import è indicativo; `readonly cellSize` **RIMOSSO**)*
```typescript
import { Component, OnInit, computed, inject, input, signal } from '@angular/core';

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
    </div>
  }
</div>
```

### `board/board.css` — **MODIFICATO** (versione finale completa)
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

### `game-screen/game-screen.ts` — **MODIFICATO** (versione finale completa)
`ngOnInit`, `roll`, `move`, `choose` e `toErrorMessage` sono **INVARIATI**; cambiano import, costanti, campi, `runAction` e i metodi di riproduzione. `ngOnInit` applica lo stato del `GET` direttamente (senza riproduzione).
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

### `game-screen/game-screen.html` — **MODIFICATO** (versione finale completa)
Il box della pausa sta subito dopo il blocco dei messaggi; il tabellone riceve i giocatori "animati". La tabella dei giocatori usa ancora lo stato applicato (si aggiorna a fine movimento).
```html
<div class="game-screen">
  @if (errorMessage()) {
    <p class="error">{{ errorMessage() }}</p>
  }

  @let game = state();
  @if (game) {
    <h2>Turno di {{ game.currentPlayerName }}</h2>
    <p>Fase: {{ game.turnPhase }}</p>
    @if (game.lastRoll > 0) {
      <p>Ultimo tiro: {{ game.lastRoll }}</p>
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

  <app-board [players]="boardPlayers()" />
</div>
```

---

## 10. Stato delle verifiche

- **Backend (JUnit)**: dopo il `clean` e le correzioni (costruttore unico di `CheckpointSquare`, costruttore vuoto di `CheckpointSquareEntity`, `;` del seed) l'utente ha riportato che i test funzionano (`BoardAssemblerIT`, `GameControllerIT`). Per i 5 test nuovi di `GameControllerIT` non è stato riportato un esito per ciascuno: l'utente ha detto "per il resto funziona tutto".
- **Frontend**: l'utente ha verificato in locale il tabellone completo con le pedine ("è a posto") e poi il movimento visibile con la pausa sullo StartOver ("funziona tutto"). Le etichette a due righe (83, 94, 104, 105) sono state viste nel tabellone completo.
- **Non verificato**: gli spec generati dalla CLI per `board`, `pawn`, `game-screen` e gli altri componenti (`ng test`); il comportamento al refresh a metà riproduzione; il comportamento quando si lascia la pagina durante la pausa; il ritorno allo StartOver con il checkpoint già raggiunto (messaggio del checkpoint dopo il ritorno).
- Nessuna verifica in sandbox in questa sessione.

---

## 11. TODO / prossimi passi (aggiornati)

### Aperti (nuovi in questa sessione)
1. **PRIMA COSA della prossima sessione — scelte di bivio e portale**: rivedere come arrivano al giocatore. Oggi i pulsanti dicono "Vai alla casella N" (es. "vai alla casella 45") e non fanno capire dove si va. Da decidere con l'utente come presentarle.
2. **Passi rimasti al bivio**: quando il giocatore arriva a un bivio deve vedere quanti passi gli restano ancora da fare, per calcolare al volo dove gli conviene andare. In `Game` esiste già `awaitingRemainingSteps` (privato, non esposto dall'API).
3. **Etichette di 81 e 110**: ancora senza etichetta (mostrano il numero). Nella foto si legge "JUMANJI" e "Fine"; da confermare.
4. **Regole di board future** (annotate, non fatte): non chiedere la direzione a chi è sul checkpoint e mandarlo avanti; checkpoint come muro con rimbalzo o permanenza a scelta di board; vie senza uscita senza traguardo (già discusse in passato).
5. **Altre cose che l'utente vorrà fare** dopo lo scheletro: non ancora dette.
6. **Estetica**: StartOver e portali come buchi neri (oggi `0` e `0 N`), fase del turno ancora valore grezzo, testi provvisori; da fare ora che lo scheletro è completo.
7. **Tabella e stato durante la riproduzione**: si aggiornano solo a fine movimento (scelta voluta con la strada A); da rivedere se dà fastidio.

### Aperti (dai dump precedenti, **INVARIATI**)
- Etichette e coordinate nel backend/DB (l'utente ha detto che cambieranno sicuramente) e dati nuovi per istanza (etichetta dell'obbligo, numero del portale).
- `BoardControllerIT` per l'endpoint `GET /api/boards/{boardId}` (offerto, da decidere).
- Spec generati da `ng g c` per `board`, `pawn`, `player-setup`, `game-screen`, `winner`, `GameApi`: eseguire (`ng test`) e adattare o rimuovere.
- Id della board fisso (`OG_BOARD_ID` nel backend e in `board.ts`), da rivedere con le board multiple.
- Pedine con molti giocatori nella stessa casella: nelle caselle quadrate con 10 giocatori il raggio scende a 2,5 (illeggibile); nel cerchio, grazie alla fascia più larga, con 10 giocatori il raggio è 6.
- Tutti gli altri aperti e rimandati del main dump (TTL delle partite, storico/statistiche, board multiple e factory delle caselle, autenticazione, Electron, ordine dei turni lato frontend, proxy solo per sviluppo, nomi doppi, rivalutare EAGER, bivio con percorsi non riconvergenti, schermata del vincitore, test Postman).

### Chiusi in questa sessione
- ~~Completare il tabellone per pezzi dalla casella 7 in poi~~ → scheletro completo (resta l'estetica).
- ~~Pedina sullo StartOver: l'animazione non si vede~~ → riproduzione con messaggio e "Continua".
- ~~Pedina passo per passo~~ → riproduzione lato frontend (strada A); il backend espone il percorso dell'azione.
- ~~Bivio pre-checkpoint (80) come passo extra~~ → rimosso; i tre crea entrano nel checkpoint in 1 passo.
- ~~Casella "2 tiri" mancante dopo la partenza~~ → aggiunta.
- ~~Messaggi sul limite all'indietro (checkpoint e partenza)~~ → fatti.
- ~~Rinomina dei package `entity`/`persistence`~~ → fatta (`model`/`entity`).
