# FUMANJI — Dump di sessione (delta)

> **Data**: 01/10/2026 (Europe/Rome). La sessione è a cavallo tra il 30/09 e l'01/10.
> **Natura del file**: contiene **solo** ciò che è stato fatto/deciso in questa conversazione, con la **versione finale** di ogni file toccato. Resta valido per tutto ciò che qui non compare il dump principale (`Fumanji-main-dump-24-09-26.md`) insieme al delta unito (`Fumanji-dump-delta-unito-30-09-26_1513.md`); l'unione la fa l'utente a parte.
> Dump snello: passaggi superati e cose annullate non compaiono (es. i cerchi del primo tabellone, le coordinate libere, il separatore `/` nelle etichette). Archivio esteso, non riassunto: il codice è quello **finale**. Nessuna verifica in sandbox in questa sessione: tutto il codice Spring e Angular è stato provato dall'utente in locale.

---

## 1. Cosa è stato fatto (indice)

- **Tabellone — decisioni di design**: dati esposti dall'API, layout ibrido con coordinate in file statico, SVG, caselle quadrate attaccate, etichette al posto dei numeri, pedine.
- **Dominio**: `getDescription()` su `Square` e sulle 10 sottoclassi (testo dell'effetto, template grezzo); `CheckpointSquare` con template estratto in costante.
- **Backend**: endpoint `GET /api/boards/{boardId}` (DTO, enum `SquareType`, service, assembler, controller, eccezione 404). Verificato con Postman.
- **Frontend**: modelli, `BoardApi`, componente `board` in SVG dentro `game-screen` (primo pezzo: caselle 0-6), layout a griglia, mappa statica delle etichette, componente `pawn` con le pedine dei giocatori.

---

## 2. Modalità di lavoro — novità

- **Notazione dell'utente**: quando scrive elenchi tipo "schimica/x2, h2o/x2, giro/bong" la `/` indica solo l'**a capo**, non è un separatore da usare nel codice. Nei dati non si usano caratteri speciali come separatori: si usano strutture (es. un array di righe).
- **Quando la richiesta ha portata più ampia del previsto** (è successo per le etichette): ci si ferma, si spiega l'impatto reale e le strade possibili, si decide insieme, poi si scrive il codice.
- **Frontend a pezzi**: ogni nuovo pezzo di tabellone porta con sé coordinate **ed** etichette.
- Il codice modificato si mostra a frammenti (solo le righe/i blocchi cambiati), i file nuovi per intero, come da regola già in vigore.
- Il codice Angular generato con la CLI usa `ng g c <nome>` da `FE\angular`; poi si sostituiscono i contenuti. Gli spec generati non sono stati eseguiti.

---

## 3. Struttura cartelle — aggiunte

```
BE/spring/src/main/java/com/project/fumanji/
├── api/BoardController.java                 (NUOVO)
├── dto/
│   ├── BoardResponse.java                   (NUOVO)
│   ├── SquareResponse.java                  (NUOVO)
│   └── SquareType.java                      (NUOVO)
├── exception/
│   ├── BoardNotFoundException.java          (NUOVO)
│   └── GlobalExceptionHandler.java          (aggiunto un handler)
├── mapper/BoardResponseAssembler.java       (NUOVO)
├── service/BoardService.java                (NUOVO)
└── entity/ (+ squares/)                     (aggiunto getDescription() a Square e alle 10 sottoclassi)

FE/angular/src/app/
├── board/                                   (NUOVO)
│   ├── board.ts / board.html / board.css / board.spec.ts
│   ├── board-layout.ts
│   └── board-labels.ts
├── pawn/                                    (NUOVO)
│   └── pawn.ts / pawn.html / pawn.css / pawn.spec.ts
├── models/
│   ├── board-response.ts                    (NUOVO)
│   ├── square-response.ts                   (NUOVO)
│   └── square-type.ts                       (NUOVO)
├── services/board-api.ts                    (NUOVO)
└── game-screen/                             (modificati .ts e .html)
```

---

## 4. Decisioni di design — tabellone

### Dati esposti dall'API
- L'endpoint restituisce **tutte** le posizioni (0-110, comprese partenza, arrivo e caselle base), perché la board OG è nota e il frontend deve solo imparare a gestirla.
- Per ogni casella: `position`, `type`, `description` (testo dell'effetto), `nextPositions`, `previousPositions`, `linkedPositions` (solo per i portali). Con `spring.jackson.default-property-inclusion=non_null` i campi nulli (`description`, `linkedPositions`) non compaiono nel JSON.
- `BoardResponse` contiene solo `finishPosition` e `squares`: i flag delle regole (`directionChoiceEnabled`, ecc.) **non** sono esposti (aggiungibili dopo).
- Il tipo è un enum `SquareType` (stesso criterio di `TurnPhase`), non il nome della classe Java: l'API non dipende dai nomi interni.
- L'assembler **scarta le posizioni fuori tabellone** da `next`/`previous` (`previous = -1` della partenza, `next = 111` dell'arrivo), che altrimenti finirebbero nel JSON.
- Rotta `GET /api/boards/{boardId}`, pronta per le board multiple; board inesistente → `404` (`BoardNotFoundException`).
- Esempio di risposta per la casella 6: `{"position":6,"type":"BIVIO","description":"+1 snack","nextPositions":[10,40],"previousPositions":[5]}`. La casella 0 ha `previousPositions: []`, la 110 `nextPositions: []`.
- `GameService` non è stato toccato: continua a caricare la board da solo con il suo `OG_BOARD_ID`.

### Testo dell'effetto (`getDescription()`)
- Ogni casella decide il proprio testo. **Template grezzo**: i placeholder non vengono sostituiti (il tabellone non ha un giocatore di turno legato alla casella).
- Caselle senza testo proprio: **testo fisso per tipo**, con la quantità dentro il testo.
- `BivioSquare` non ha un effetto né un messaggio propri: la sua descrizione è quella del suo `effect` (nessun testo se non ha effetto).
- Casella base: nessun testo (`null`).

| Casella | Testo |
|---|---|
| Snack | `+{amount} snack` (con importo negativo: `-N snack`) |
| Drink | `+{amount} drink` |
| StartOver | `Torna all'ultimo checkpoint (o alla partenza)` |
| Portale | `Portale: ti teletrasporta a un altro portale` |
| MoveForward | `Avanti di {steps} caselle` |
| MoveBack | `Indietro di {steps} caselle` |
| RollAgain | `Tira di nuovo` |
| Obbligo | il suo template, invariato |
| Checkpoint | `{player} ha raggiunto il checkpoint! Quindi deve fare 3 tiri di creazione` (template, `buildMessage` lo usa sostituendo il nome) |
| Bivio | descrizione dell'`effect`, se c'è |
| Casella base | nessun testo |

### Layout e disegno
- **Layout ibrido**: coordinate scritte a mano solo per i punti chiave; le caselle intermedie si interpolano lungo un tratto.
- **Coordinate in un file statico del frontend** per ora; le colonne nel DB sono per il futuro (stesso destino delle etichette).
- **SVG**, caselle cliccabili. Al click compare un dettaglio sotto il disegno (numero, tipo grezzo, descrizione). Esplorare la board durante la partita è un desiderio dell'utente, ma per ora si fa la cosa più semplice.
- Il tabellone sta **dentro la schermata di gioco**, come componente `board` incluso in `game-screen`.
- **Caselle quadrate, tutte attaccate senza spazi**: le coordinate sono in **celle della griglia** (colonna, riga; riga 0 in alto), lato fisso `CELL_SIZE = 50` unità SVG. Un tratto interpolato dà caselle adiacenti solo se è dritto e il numero di passi coincide con la distanza in celle (es. 0→6 = 6 colonne).
- **Si disegna per pezzi**: oggi solo le caselle 0-6. Le posizioni senza coordinate non vengono disegnate (né le caselle né le pedine).
- **Primo pezzo (0-6)**: la partenza è in basso a sinistra e si va verso destra fino al Bivio A (6). Dal bivio un ramo continua a destra e uno va in su (quale dei due, 10 o 40, è ancora da dire).
- L'id della board è `1` fisso anche nel frontend (`OG_BOARD_ID` in `board.ts`), come nel backend.

### Etichette nelle caselle (al posto dei numeri)
- Vocabolario: partenza, obbligo, turca, h2o, schimica, no smoke, 2 tiri, crea, bong, giro bong, h2o x2, schimica x2.
- Le caselle **senza effetto** si chiamano "no smoke" (compresi i bivi senza effetto 17 e 80). Un bivio **con** effetto prende l'etichetta del suo effetto (es. il 6 = "schimica").
- StartOver: in futuro visivamente **buchi neri**; per ora l'etichetta è `0`. Portali: come StartOver ma con il numero vicino ("0 1", "0 4"…).
- Etichette lunghe su **2 righe** ("schimica" / "x2", "h2o" / "x2", "giro" / "bong"). Senza etichetta si mostra il numero di posizione.
- **Dove vivono**: mappa statica posizione → etichetta nel frontend (strada B), scelta per semplicità e coerenza con le coordinate. L'utente ha detto che **in futuro cambierà sicuramente** (probabilmente verso il backend/DB).
- **Perché non nel backend ora** (strada A scartata per ora): servirebbe un campo `label` nell'API e dati nuovi per istanza (etichetta dell'obbligo, numero del portale), quindi nuove colonne nel DB, entity JPA, costruttori del dominio e script di seed (anche quello dei test). Dati oggi non disponibili nell'API: le sei etichette degli obblighi si distinguono solo dal messaggio; il numero del portale non è salvato (I=53, II=23, III=32, IV=62, V=27, VI=58 esiste solo nel dump); il "x2" di Snack/Drink dipende dall'`amount`, che arriva solo dentro il testo; "partenza" e "no smoke" sono entrambe caselle base e si distinguono solo per posizione.

### Pedine
- Una pedina per giocatore, tutte visibili: cerchio colorato con l'iniziale del nome, colore assegnato in base all'**ordine di gioco** (palette di 10 colori).
- Più pedine sulla stessa casella si dispongono **affiancate** nella parte bassa della casella (ognuna in una fascia della larghezza della casella; il raggio si restringe col numero di pedine: con tanti giocatori sulla stessa casella l'iniziale diventa minuscola, limite accettato).
- **Movimento**: transizione CSS (0,4 s) in **linea retta** dalla casella di partenza a quella di arrivo, non passo per passo. Il passo per passo richiederebbe che il backend esponesse i passi (oggi `Game.movementTrace` è una lista unica, non attribuita al giocatore e fuori dall'API).
- La pedina si muove quando arriva la risposta di `move` (o di `choice`), non quella del tiro, perché è lì che cambia la posizione.
- Una pedina su una posizione senza coordinate non si disegna (finché il layout arriva solo alla 6, oltre la 6 sparisce).
- **Componente con selettore ad attributo** (`<g appPawn>`), non un tag: un elemento personalizzato dentro un SVG non viene disegnato. Per lo stesso motivo il suo template usa elementi con prefisso `svg:`. Host con `pointer-events: none` per non intercettare i click sulle caselle.
- **Limite noto (da sistemare in futuro, detto dall'utente)**: quando il giocatore atterra su uno **StartOver** la posizione finale coincide con quella di partenza (il motore lo rimanda subito al checkpoint, cioè a 0 prima del checkpoint), quindi la pedina non si anima e il giocatore non vede nulla. Oggi nessun messaggio spiega l'evento.

---

## 5. Codice backend — versioni finali

### Dominio — `getDescription()`

`entity/Square.java` — nuovo metodo:
```java
public String getDescription() {
    return null;
}
```

`entity/squares/StartOverSquare.java`:
```java
@Override
public String getDescription() {
    return "Torna all'ultimo checkpoint (o alla partenza)";
}
```

`entity/squares/PortalSquare.java`:
```java
@Override
public String getDescription() {
    return "Portale: ti teletrasporta a un altro portale";
}
```

`entity/squares/RollAgainSquare.java`:
```java
@Override
public String getDescription() {
    return "Tira di nuovo";
}
```

`entity/squares/MoveForwardSquare.java`:
```java
@Override
public String getDescription() {
    return "Avanti di " + steps + " caselle";
}
```

`entity/squares/MoveBackSquare.java`:
```java
@Override
public String getDescription() {
    return "Indietro di " + steps + " caselle";
}
```

`entity/squares/SnackSquare.java` — versione dell'utente (gestisce anche importi negativi):
```java
@Override
public String getDescription() {
    return (amount<0 ? amount : "+"+amount) + " snack";
}
```

`entity/squares/DrinkSquare.java` — versione consegnata in chat; lo stesso trattamento per gli importi negativi dello snack era stato suggerito anche qui, applicazione non confermata:
```java
@Override
public String getDescription() {
    return "+" + amount + " drink";
}
```

`entity/squares/ObligationSquare.java`:
```java
@Override
public String getDescription() {
    return message;
}
```

`entity/squares/CheckpointSquare.java` — costante come primo campo della classe, `buildMessage` che la usa, e la descrizione:
```java
private static final String MESSAGE_TEMPLATE =
        "{player} ha raggiunto il checkpoint! Quindi deve fare 3 tiri di creazione";
```
```java
public String buildMessage(Player player) {
    return MESSAGE_TEMPLATE.replace("{player}", player.getName());
}

@Override
public String getDescription() {
    return MESSAGE_TEMPLATE;
}
```

`entity/squares/BivioSquare.java`:
```java
@Override
public String getDescription() {
    return effect != null ? effect.getDescription() : null;
}
```

### `dto/SquareType.java`
```java
package com.project.fumanji.dto;

public enum SquareType {
    BASE,
    START_OVER,
    MOVE_BACK,
    MOVE_FORWARD,
    ROLL_AGAIN,
    SNACK,
    DRINK,
    OBLIGATION,
    CHECKPOINT,
    PORTAL,
    BIVIO
}
```

### `dto/SquareResponse.java`
```java
package com.project.fumanji.dto;

import java.util.List;

public record SquareResponse(
        int position,
        SquareType type,
        String description,
        List<Integer> nextPositions,
        List<Integer> previousPositions,
        List<Integer> linkedPositions) {
}
```

### `dto/BoardResponse.java`
```java
package com.project.fumanji.dto;

import java.util.List;

public record BoardResponse(int finishPosition, List<SquareResponse> squares) {
}
```

### `exception/BoardNotFoundException.java`
```java
package com.project.fumanji.exception;

public class BoardNotFoundException extends RuntimeException {

    public BoardNotFoundException(Long boardId) {
        super("Board not found: " + boardId);
    }
}
```

### `exception/GlobalExceptionHandler.java` — handler aggiunto
```java
@ExceptionHandler(BoardNotFoundException.class)
public ResponseEntity<ErrorResponse> handleBoardNotFound(BoardNotFoundException ex) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(ex.getMessage()));
}
```

### `service/BoardService.java`
```java
package com.project.fumanji.service;

import org.springframework.stereotype.Service;

import com.project.fumanji.entity.Board;
import com.project.fumanji.exception.BoardNotFoundException;
import com.project.fumanji.persistence.BoardAssembler;
import com.project.fumanji.persistence.BoardEntity;
import com.project.fumanji.repository.BoardRepository;

@Service
public class BoardService {

    private final BoardRepository boardRepository;
    private final BoardAssembler boardAssembler;

    public BoardService(BoardRepository boardRepository, BoardAssembler boardAssembler) {
        this.boardRepository = boardRepository;
        this.boardAssembler = boardAssembler;
    }

    public Board getBoard(Long boardId) {
        BoardEntity entity = boardRepository.findById(boardId)
                .orElseThrow(() -> new BoardNotFoundException(boardId));
        return boardAssembler.toDomain(entity);
    }
}
```

### `mapper/BoardResponseAssembler.java`
```java
package com.project.fumanji.mapper;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.project.fumanji.dto.BoardResponse;
import com.project.fumanji.dto.SquareResponse;
import com.project.fumanji.dto.SquareType;
import com.project.fumanji.entity.Board;
import com.project.fumanji.entity.Square;
import com.project.fumanji.entity.squares.BivioSquare;
import com.project.fumanji.entity.squares.CheckpointSquare;
import com.project.fumanji.entity.squares.DrinkSquare;
import com.project.fumanji.entity.squares.MoveBackSquare;
import com.project.fumanji.entity.squares.MoveForwardSquare;
import com.project.fumanji.entity.squares.ObligationSquare;
import com.project.fumanji.entity.squares.PortalSquare;
import com.project.fumanji.entity.squares.RollAgainSquare;
import com.project.fumanji.entity.squares.SnackSquare;
import com.project.fumanji.entity.squares.StartOverSquare;

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
                .map(square -> toSquareResponse(square, finishPosition))
                .toList();
        return new BoardResponse(finishPosition, squares);
    }

    private SquareResponse toSquareResponse(Square square, int finishPosition) {
        List<Integer> linkedPositions = square instanceof PortalSquare portal
                ? portal.getLinkedPositions()
                : null;

        return new SquareResponse(
                square.getPosition(),
                TYPES.getOrDefault(square.getClass(), SquareType.BASE),
                square.getDescription(),
                withinBoard(square.getNextPositions(), finishPosition),
                withinBoard(square.getPreviousPositions(), finishPosition),
                linkedPositions);
    }

    private List<Integer> withinBoard(List<Integer> positions, int finishPosition) {
        return positions.stream()
                .filter(position -> position >= 0 && position <= finishPosition)
                .toList();
    }
}
```

### `api/BoardController.java`
```java
package com.project.fumanji.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.fumanji.dto.BoardResponse;
import com.project.fumanji.mapper.BoardResponseAssembler;
import com.project.fumanji.service.BoardService;

@RestController
@RequestMapping("/api/boards")
public class BoardController {

    private final BoardService boardService;
    private final BoardResponseAssembler boardResponseAssembler;

    public BoardController(BoardService boardService, BoardResponseAssembler boardResponseAssembler) {
        this.boardService = boardService;
        this.boardResponseAssembler = boardResponseAssembler;
    }

    @GetMapping("/{boardId}")
    public ResponseEntity<BoardResponse> getBoard(@PathVariable Long boardId) {
        return ResponseEntity.ok(boardResponseAssembler.toResponse(boardService.getBoard(boardId)));
    }
}
```

---

## 6. Codice frontend — versioni finali

### Models (`src/app/models/`)

`square-type.ts`:
```typescript
export type SquareType =
  | 'BASE'
  | 'START_OVER'
  | 'MOVE_BACK'
  | 'MOVE_FORWARD'
  | 'ROLL_AGAIN'
  | 'SNACK'
  | 'DRINK'
  | 'OBLIGATION'
  | 'CHECKPOINT'
  | 'PORTAL'
  | 'BIVIO';
```

`square-response.ts`:
```typescript
import { SquareType } from './square-type';

export interface SquareResponse {
  position: number;
  type: SquareType;
  // Assenti dalla risposta quando non ci sono (il backend omette i valori null)
  description?: string;
  nextPositions: number[];
  previousPositions: number[];
  linkedPositions?: number[];
}
```

`board-response.ts`:
```typescript
import { SquareResponse } from './square-response';

export interface BoardResponse {
  finishPosition: number;
  squares: SquareResponse[];
}
```

### `services/board-api.ts`
```typescript
import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { BoardResponse } from '../models/board-response';

@Injectable({
  providedIn: 'root',
})
export class BoardApi {

  private readonly http = inject(HttpClient);

  // Percorso relativo: in sviluppo lo inoltra il proxy del dev server al backend
  private readonly baseUrl = '/api/boards';

  getBoard(boardId: number): Observable<BoardResponse> {
    return this.http.get<BoardResponse>(`${this.baseUrl}/${boardId}`);
  }
}
```

### `board/board-layout.ts`
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
export const BOARD_COLUMNS = 9;
export const BOARD_ROWS = 8;

// Punti chiave posizionati a mano (in celle): partenza e bivio A
const KEY_POINTS: Record<number, Point> = {
  0: { x: 1, y: 6 },
  6: { x: 7, y: 6 },
};

// Tratti da interpolare: le caselle intermedie si distribuiscono in modo uniforme tra gli estremi
const SEGMENTS: Segment[] = [
  { from: 0, to: 6 },
];

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

### `board/board-labels.ts`
```typescript
// Etichette mostrate nelle caselle, per posizione (board OG): dato visivo statico,
// da spostare nel backend insieme alle coordinate.
// Ogni elemento dell'array è una riga dell'etichetta
export const SQUARE_LABELS: Readonly<Partial<Record<number, readonly string[]>>> = {
  0: ['partenza'],
  1: ['obbligo'],
  2: ['turca'],
  3: ['h2o'],
  // StartOver: per ora "0" al posto del buco nero
  4: ['0'],
  5: ['no smoke'],
  // Bivio A: prende l'etichetta del suo effetto (snack)
  6: ['schimica'],
};
```
Quando si aggiungeranno le altre, le due righe si scrivono così: `['schimica', 'x2']`, `['h2o', 'x2']`, `['giro', 'bong']`.

### `board/board.ts`
*(l'ordine degli import è indicativo)*
```typescript
import { Component, OnInit, computed, inject, input, signal } from '@angular/core';

import { BoardResponse } from '../models/board-response';
import { PlayerStateResponse } from '../models/player-state-response';
import { SquareResponse } from '../models/square-response';
import { Pawn } from '../pawn/pawn';
import { BoardApi } from '../services/board-api';
import { SQUARE_LABELS } from './board-labels';
import { BOARD_COLUMNS, BOARD_LAYOUT, BOARD_ROWS, CELL_SIZE } from './board-layout';

// Id della board OG: per ora una sola board fissa, stesso id usato dal backend
const OG_BOARD_ID = 1;

// Distanza verticale tra le righe di un'etichetta, in unità SVG
const LABEL_LINE_HEIGHT = 12;

// Raggio massimo di una pedina; con più pedine sulla stessa casella si restringe per farle stare affiancate
const PAWN_RADIUS = 8;

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

// Casella insieme alle coordinate SVG del suo angolo in alto a sinistra e alle righe dell'etichetta
interface PlacedSquare {
  square: SquareResponse;
  x: number;
  y: number;
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

  readonly cellSize = CELL_SIZE;
  readonly viewBox = `0 0 ${BOARD_COLUMNS * CELL_SIZE} ${BOARD_ROWS * CELL_SIZE}`;

  // Tabellone ricevuto dal backend; resta null finché non arriva la risposta
  readonly board = signal<BoardResponse | null>(null);

  // Messaggio mostrato se il tabellone non si riesce a caricare
  readonly errorMessage = signal<string | null>(null);

  // Posizione della casella cliccata, di cui si mostra il dettaglio
  readonly selectedPosition = signal<number | null>(null);

  // Stato dei giocatori, da cui si ricavano le pedine; lo fornisce la schermata di gioco
  readonly players = input<PlayerStateResponse[]>([]);

  // Si disegnano solo le caselle che hanno già una coordinata: il tabellone si completa per pezzi
  readonly placedSquares = computed<PlacedSquare[]>(() =>
    (this.board()?.squares ?? []).flatMap(square => {
      const cell = BOARD_LAYOUT.get(square.position);
      if (!cell) {
        return [];
      }
      // Da cella della griglia a coordinate SVG
      const x = cell.x * CELL_SIZE;
      const y = cell.y * CELL_SIZE;
      // Senza etichetta si mostra il numero di posizione; ogni elemento è una riga
      const rows = SQUARE_LABELS[square.position] ?? [String(square.position)];
      return [{
        square,
        x,
        y,
        // Righe centrate verticalmente nella casella
        lines: rows.map((text, index) => ({
          text,
          y: y + CELL_SIZE / 2 + (index - (rows.length - 1) / 2) * LABEL_LINE_HEIGHT,
        })),
      }];
    }));

  readonly pawns = computed<PlacedPawn[]>(() => {
    const players = this.players();

    // Quante pedine ci sono su ogni posizione, per dividere la casella fra loro
    const countByPosition = new Map<number, number>();
    for (const player of players) {
      countByPosition.set(player.position, (countByPosition.get(player.position) ?? 0) + 1);
    }

    // Quante pedine di ogni posizione sono già state sistemate
    const placedByPosition = new Map<number, number>();

    return players.flatMap((player, index) => {
      const cell = BOARD_LAYOUT.get(player.position);
      // Posizione senza coordinate: la pedina non si disegna
      if (!cell) {
        return [];
      }
      const slot = placedByPosition.get(player.position) ?? 0;
      placedByPosition.set(player.position, slot + 1);

      // Pedine affiancate nella parte bassa della casella, ognuna nella sua fascia
      const slotWidth = CELL_SIZE / countByPosition.get(player.position)!;
      return [{
        index,
        x: cell.x * CELL_SIZE + slotWidth * (slot + 0.5),
        y: (cell.y + 1) * CELL_SIZE - PAWN_RADIUS - 2,
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

### `board/board.html`
```html
<div class="board">
  @if (errorMessage()) {
    <p class="error">{{ errorMessage() }}</p>
  }

  <svg class="board-svg" [attr.viewBox]="viewBox">
    @for (placed of placedSquares(); track placed.square.position) {
      <g
        class="square"
        [class.selected]="placed.square.position === selectedPosition()"
        (click)="select(placed.square.position)"
      >
        <rect [attr.x]="placed.x" [attr.y]="placed.y" [attr.width]="cellSize" [attr.height]="cellSize" />
        @for (line of placed.lines; track $index) {
          <text
            [attr.x]="placed.x + cellSize / 2"
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

### `board/board.css`
```css
.board-svg {
  width: 100%;
  max-width: 600px;
  border: 1px solid #ccc;
}

.square {
  cursor: pointer;
}

.square rect {
  fill: #fff;
  stroke: #333;
  stroke-width: 2;
}

.square.selected rect {
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

### `pawn/pawn.ts`
```typescript
import { Component, computed, input } from '@angular/core';

// Pedina di un giocatore: si usa come attributo su un <g> dentro l'SVG del tabellone.
// Il centro è l'origine del <g>, spostato con transform: la transizione CSS la fa scivolare
@Component({
  selector: '[appPawn]',
  imports: [],
  templateUrl: './pawn.html',
  styleUrl: './pawn.css',
  host: {
    '[style.transform]': 'transform()',
  },
})
export class Pawn {

  readonly x = input.required<number>();
  readonly y = input.required<number>();
  readonly radius = input.required<number>();
  readonly color = input.required<string>();
  readonly initial = input.required<string>();

  // Posizione del centro della pedina in unità SVG
  readonly transform = computed(() => `translate(${this.x()}px, ${this.y()}px)`);
}
```

### `pawn/pawn.html`
```html
<svg:circle [attr.r]="radius()" [attr.fill]="color()" stroke="#fff" stroke-width="1" />
<svg:text
  text-anchor="middle"
  dominant-baseline="central"
  fill="#fff"
  [attr.font-size]="radius() * 1.2"
>{{ initial() }}</svg:text>
```

### `pawn/pawn.css`
```css
:host {
  /* La pedina scivola in linea retta quando cambia posizione */
  transition: transform 0.4s ease-in-out;
  /* Non intercetta i click: restano sulla casella sotto */
  pointer-events: none;
}
```

### `game-screen/game-screen.ts` — aggiunte
```typescript
import { Board } from '../board/board';
```
```typescript
imports: [Board],
```

### `game-screen/game-screen.html` — in fondo, dopo la chiusura del blocco `@if (game)` e prima dell'ultimo `</div>`
```html
<app-board [players]="game?.players ?? []" />
```

---

## 7. Stato delle verifiche

- **Backend**: `GET /api/boards/1` provato dall'utente con Postman, restituisce tutta la board "precisa". Un primo errore di avvio (`BoardService` non trovato come bean) era un errore di digitazione dell'utente, risolto.
- **Frontend**: l'utente ha verificato in locale il tabellone 0-6 (caselle cliccabili con dettaglio), le caselle quadrate attaccate con le etichette e le pedine ("funziona"). Un errore di tipo su `PlacedSquare` (interfaccia con `point` rimasta dalla prima versione) era dovuto a una sostituzione non applicata, risolto.
- **Osservazione dell'utente sulle pedine**: alcuni lanci non sembrano muovere la pedina; la causa è lo StartOver (vedi sezione 4, "Limite noto"), non un errore di movimento.
- **Non verificato**: le etichette a due righe (nessuna etichetta attuale ne ha due: si provano cambiando un'etichetta, per esempio `6: ['schimica', 'x2']`); gli spec generati dalla CLI per `board` e `pawn` (probabilmente richiedono provider di test, `HttpClient` per `board`); nessun test JUnit scritto per `BoardController` (offerto `BoardControllerIT`, non richiesto).
- Nessuna verifica in sandbox in questa sessione.

---

## 8. TODO / prossimi passi (aggiornati)

### Aperti
1. **Completare il tabellone per pezzi, dalla casella 7 in poi**: per ogni pezzo coordinate ed etichette. Da dire prima: quale ramo del Bivio A (10 o 40) va a destra e quale in su. Etichette ancora non definite: Checkpoint (81) e arrivo (110). Portali: etichetta "0" più il numero (I=53, II=23, III=32, IV=62, V=27, VI=58). Etichette lunghe su due righe (`['schimica', 'x2']`, `['h2o', 'x2']`, `['giro', 'bong']`).
2. **Pedina sullo StartOver**: l'animazione non si vede quando si atterra su uno StartOver (posizione finale = posizione di partenza). Da sistemare in futuro.
3. **Pedina passo per passo**: richiede che il backend esponga i passi per giocatore (`Game.movementTrace` oggi è una lista unica non attribuita e fuori dall'API).
4. **Etichette e coordinate nel backend/DB**: l'utente ha detto che cambieranno sicuramente. Insieme, valutare dati nuovi per istanza (etichetta dell'obbligo, numero del portale).
5. **StartOver e portali come buchi neri** (oggi `0`), e fase del turno ancora mostrata come valore grezzo.
6. **`BoardControllerIT`** per l'endpoint (offerto, da decidere).
7. **Spec generati** per `board` e `pawn` (`ng test`): eseguirli e adattarli o rimuoverli.
8. **Id della board fisso**: ora anche nel frontend (`OG_BOARD_ID` in `board.ts`); da rivedere con le board multiple insieme a quello del backend.
9. **Pedine con molti giocatori sulla stessa casella**: con 10 giocatori il raggio scende a 2,5 e l'iniziale è illeggibile; da rivedere se serve.

### Chiusi
- ~~Tabellone: endpoint con caselle e collegamenti~~ → fatto e verificato con Postman.
- ~~Tabellone: decisione su come disegnarlo~~ → SVG, layout ibrido a griglia, coordinate ed etichette in file statici del frontend; primo pezzo (0-6) funzionante.
- ~~Testo dell'effetto delle caselle~~ → `getDescription()` su tutte le caselle.
- ~~Componente pedina~~ → fatto (movimento in linea retta).
