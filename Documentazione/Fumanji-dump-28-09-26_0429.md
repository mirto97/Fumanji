# FUMANJI — Dump di sessione (delta)

> **Data e ora**: 28/09/2026, 04:29 (Europe/Rome).
> **Natura del file**: contiene **solo ciò che è stato fatto/deciso in questa conversazione**, non lo stato accumulato del progetto. Da unire a parte al dump precedente (`Fumanji-dump-24-09-26.md`), che resta valido per tutto ciò che qui non compare.
> **Regola di snellezza** (introdotta in questa sessione): nel dump non finiscono cose cancellate/superate/obsolete, nemmeno se presenti nei dump precedenti.
> Archivio esteso, non riassunto: il codice riportato è quello **finale** di questa sessione.

---

## 1. Cosa è stato fatto in questa sessione (indice)

- `BoardRepository` creato, in un nuovo package `com.project.fumanji.repository`.
- DB `fumanji` droppato e ricreato; script SQL della board OG lanciato sul DB reale.
- Infrastruttura di test: schema `fumanji_test`, profilo `test`, seed via `@Sql`; prima prova end-to-end (`BoardAssemblerIT`) verde.
- Fix `LazyInitializationException`: fetch `EAGER` su tutte le collection della persistenza.
- Stato di `Game` tra le richieste REST: **deciso** (in memoria, `GameRegistry`, salvataggio solo a fine partita).
- Layer applicativo e REST completo per una partita intera: `GameRegistry`, `GameService`, `EndGameService`, `GameController`, DTO, gestione errori, `GameStateAssembler`.
- Dominio: **fasi del turno** (`TurnPhase`) e metodi di turno in `Game`.
- Salvataggio a fine partita: `EndGameEntity` + `EndGameRepository` + `EndGameService`.
- Endpoint `GET` per rileggere lo stato di una partita.
- Test di integrazione JUnit (MockMvc) su tutti gli endpoint: tutti verdi.

---

## 2. Modalità di lavoro e ambiente — novità

- **Dump snello**: cose cancellate/obsolete/superate non vanno nel dump. Il dump di fine conversazione è solo il delta della sessione, l'unione la fa l'utente a parte.
- **Sandbox — aggiornamento del "limite noto"**: nel sandbox non c'è il binario `javac`, ma il runtime contiene il modulo `jdk.compiler`, quindi il **Java puro** si compila con `java -m jdk.compiler/com.sun.tools.javac.Main ...`. Spring/JPA/MapStruct restano non compilabili (niente Maven Central): quella verifica la fa l'utente in locale.
- **Ambiente dell'utente**: VS Code (non IntelliJ/Eclipse), Windows + PowerShell. Maven **non** è installato globalmente: si usa il wrapper `.\mvnw.cmd` **dalla cartella `BE spring`** (nella root `Fumanji` non esiste). I test JUnit si lanciano da VS Code (icone accanto ai `@Test` / pannello Test Results).
- **PowerShell + Maven**: `-Dincludes=g:a` con i due punti viene interpretato male da PowerShell; per cercare una dipendenza si redirige `dependency:tree` su file e si usa `Select-String`.
- **MySQL Workbench**: l'autocompletamento può duplicare il testo mentre si digita/incolla uno script (errore 1064 con `board_entityboard_entityboard_entity`): usare una tab di query nuova e incollare senza editare.
- **`ddl-auto=update` non rende nullable una colonna già esistente `NOT NULL`**: per applicare la modifica di `board_id` è servito droppare e ricreare il DB.
- **Preferenza dell'utente sui test**: vuole poter fissare il risultato del dado nei test per coprire vari casi → vedi TODO 1.

---

## 3. Decisioni di design (delta)

### 3.1 Persistenza — fetch EAGER (da rivalutare)
- Causa: `BoardAssembler.toDomain()` legge le collection fuori da una sessione Hibernate → `LazyInitializationException` (`@OneToMany` e `@ElementCollection` sono LAZY di default).
- Scelta: `fetch = FetchType.EAGER` su `BoardEntity.squares`, `BivioSquareEntity.nextPositions`, `BivioSquareEntity.previousPositions`, `PortalSquareEntity.linkedPositions`. `BivioSquareEntity.effect` (`@ManyToOne`) è EAGER per default JPA, quindi non toccato.
- Alternative valutate e non scelte: `@Transactional` su ogni lettura; `JOIN FETCH`/`@EntityGraph`. **Da rivalutare quando si lavorerà su più board.**

### 3.2 Stato di `Game` tra le richieste REST
- Durante la partita `Game` vive **in memoria**: `Map<UUID, Game>` in un bean singleton (`GameRegistry`, `ConcurrentHashMap` perché le richieste REST arrivano su thread diversi).
- Nessuna persistenza per mossa. Motivo: `Game`/`Player` sono dominio puro con stato "in volo" (`awaitingOptions`, ecc.); persisterli richiederebbe una gerarchia JPA "live".
- **Rischio accettato per ora**: un riavvio/crash del server a metà partita perde quella partita (uso locale su singolo dispositivo).
- **TTL** (rimozione delle partite abbandonate via `@Scheduled` su timestamp di ultima attività): non implementato, voluto come miglioramento futuro prossimo.
- La costruzione di un `Game` è responsabilità di `GameService`, non del registro (il registro è un puro contenitore).

### 3.3 Fine partita
- Alla vittoria: `GameService` salva un `EndGameEntity` (nome del vincitore + board giocata) tramite `EndGameService`, poi **rimuove la partita dal `GameRegistry`**. Richieste successive su quel `gameId` → `404`; il vincitore si legge dalla risposta della mossa vincente (`winnerName`).
- Scelta delegata dall'utente a Claude puntando al massimo future proofing: un unico punto di chiusura dove in futuro salvare **tutti** i dati della partita per analisi; `EndGameService.save` riceve l'intero `Game` (oggi usa solo il vincitore).
- Per ora l'utente non è interessato ai dati della partita: gli basta sapere che tutto ha funzionato.
- Board salvata: `OG_BOARD_ID = 1L` fisso (stessa costante di `createGame`), da rivedere con le board multiple.
- Storico completo/statistiche (traccia movimenti **per giocatore**, statistiche): rimandato. Nota: `Game.movementTrace` oggi è una lista unica di posizioni, non attribuita al giocatore; le statistiche da tracciare non sono ancora definite.

### 3.4 Ordine dei turni
- L'ordine di gioco è semplicemente l'ordine in cui arrivano i nomi in `playerNames` nella richiesta di creazione: lo decidono i giocatori. Il backend lo preserva e non ha logica di riordino.
- Lato frontend, in futuro: far impostare l'ordine esplicitamente ("Giocatore 1", "Giocatore 2"… dove i giocatori scrivono il nome di chi inizia, poi chi è secondo, ecc.).

### 3.5 Fasi del turno (`TurnPhase`) e metodi di turno in `Game`
- Il server impedisce le azioni fuori sequenza: fasi `WAITING_ROLL` → `WAITING_DIRECTION` → (`WAITING_CHOICE` se c'è una scelta a bivio/portale) → prossimo giocatore, `WAITING_ROLL`.
- Motivo: senza fasi un client potrebbe chiamare `move` senza tirare, tirare due volte, o tirare con una scelta pendente (un doppio click sul frontend basterebbe).
- Metodi pubblici di turno, tutti `synchronized`: `performRoll()`, `performMove(boolean forward)`, `performChoice(int chosenPosition)`. `synchronized` perché altrimenti due richieste concorrenti (doppio click) potrebbero superare entrambe il controllo di fase.
- **`roll`, `moveByChosenDirection`, `chooseDestination` NON sono stati toccati**: le caselle li usano internamente (es. `RollAgainSquare` chiama `rollAndMove` → `roll`) e un controllo di fase lì le romperebbe.
- Il turno avanza (`resolveTurnPhase`) quando il movimento è concluso e non c'è nessuna scelta pendente; se c'è un vincitore non avanza e non cambia fase.
- Dopo la vittoria `getTurnPhase()` resta sull'ultima fase: la fine partita si legge da `getWinner()`. Qualunque azione dopo la vittoria lancia `IllegalStateException("Game is already over")`.
- `RollAgainSquare` a catena dentro `performMove` funziona (verificato).
- `playTurn()` resta com'è e **non passa dalle fasi**; nessun endpoint lo usa (vedi TODO 7).
- Nomi dei metodi (`performRoll/Move/Choice`) approvati dall'utente.

### 3.6 API REST
- `POST /api/games` — body `{"playerNames": [...]}` (validato: lista non vuota, nomi non blank) → `CreateGameResponse{gameId, currentPlayerName}`.
- `POST /api/games/{gameId}/roll` — tira il dado.
- `POST /api/games/{gameId}/move` — body `{"forward": true|false}`; con `directionChoiceEnabled = false` il `false` ricade silenziosamente su "avanti" (comportamento invariato del dominio).
- `POST /api/games/{gameId}/choice` — body `{"position": n}`; risolve la scelta a bivio/portale.
- `GET /api/games/{gameId}` — stato corrente (per rileggerlo, es. dopo un refresh del frontend).
- Roll, move, choice e GET rispondono tutti con lo stesso `GameStateResponse`: giocatore di turno, fase, ultimo tiro, stato di tutti i giocatori, `choiceOptions` (solo se c'è una scelta pendente), `winnerName` (solo se c'è un vincitore). Con `spring.jackson.default-property-inclusion=non_null` (già presente in `application.properties`) i campi nulli non compaiono nel JSON.
- Errori (`GlobalExceptionHandler`): `gameId` sconosciuto → `404`; azione fuori fase (`IllegalStateException`) → `409`; scelta non valida (`IllegalArgumentException`) o body non valido (validazione) → `400`. Nota accettata: gli handler su `IllegalStateException`/`IllegalArgumentException` sono ampi, un bug interno che le lancia diventerebbe `409`/`400` invece di `500`; accettato perché il dominio usa già solo queste eccezioni standard.
- I request DTO usano `Boolean`/`Integer` con `@NotNull` (non primitivi): con `boolean` un body `{}` diventerebbe silenziosamente `forward = false`.

### 3.7 Package (convenzione dell'utente)
- Convenzione che l'utente segue di solito (vista in un progetto passato, EventHub): `api`, `dto`, `entity`, `exception`, `mapper`, `repository`, `security`, `service`, adattata caso per caso.
- Per Fumanji, oltre a `entity`/`entity.squares` (dominio) e `persistence`/`persistence.squares` (JPA): `repository` (interfacce `JpaRepository`), `service` (`GameRegistry`, `GameService`, `EndGameService`, `GameCreationResult`), `api` (controller), `dto` (record), `exception`, `mapper` (`GameStateAssembler`, scritto a mano come `BoardAssembler`, non MapStruct: non è mappatura dichiarativa pura).
- I DTO sono `record` (contenitori dati immutabili per JSON, accessor `campo()` invece di `getCampo()`, Jackson li gestisce nativamente).

### 3.8 Test di integrazione
- Schema dedicato `fumanji_test` (da creare a mano una volta, stesso server MySQL), profilo `test`, `ddl-auto=create-drop`.
- Seed via `@Sql("/seed-og-board.sql")` (`src/test/resources`, copia identica dello script della sezione 9 del dump precedente).
- **`@Sql` di default gira prima di OGNI metodo di test**: con più `@Test` nella stessa classe il seed verrebbe rieseguito sullo stesso schema → violazione della chiave primaria (`Error Code`/`ScriptStatementFailedException` sulla prima `INSERT`). Soluzione in `GameControllerIT`: `executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS` (disponibile da Spring Framework 6.1). `BoardAssemblerIT` ha un solo metodo e usa il default: se gli si aggiunge un secondo `@Test` va cambiato allo stesso modo.
- Il dado in `GameService.createGame` è `new Dice()` (casuale): nei test sul flusso vero si asserisce solo ciò che non dipende dal tiro (dalla casella 0 con un tiro 1–6 non si finisce mai in una scelta pendente: con il 6 si atterra esattamente sul Bivio A, che non forza la scelta, quindi dopo la prima mossa il turno passa sempre al secondo giocatore). Il caso felice di `choice` e la scelta non valida **non sono coperti** a livello HTTP finché `Dice` non è iniettabile; la logica è però verificata a livello di dominio (vedi sezione 6).
- Il test di fine partita registra direttamente in `GameRegistry` una partita piccola (`new Board(5)`, `FixedDice(5)`) e la chiude passando dagli endpoint veri.

### 3.9 Spring Boot 4.x — attenzione ai test (scoperto in sessione)
- `@AutoConfigureMockMvc`/`@WebMvcTest` sono passati da `org.springframework.boot.test.autoconfigure.web.servlet` a **`org.springframework.boot.webmvc.test.autoconfigure`** e richiedono la dipendenza test **`spring-boot-starter-webmvc-test`** (non più inclusa in `spring-boot-starter-test`). Aggiunta al pom.
- `TestRestTemplate` è passato in un altro modulo (`org.springframework.boot.resttestclient`, richiede `@AutoConfigureTestRestTemplate`): non usato in questo progetto.
- Il jar `spring-boot-test-autoconfigure-4.1.1` di ~27 KB nel repository Maven locale era **corretto** (non corrotto): è piccolo perché la parte web è stata spostata fuori.

---

## 4. Codice — versioni finali di questa sessione

### 4.1 `pom.xml` — dipendenza aggiunta (dentro `<dependencies>`, insieme alle altre di test)
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webmvc-test</artifactId>
    <scope>test</scope>
</dependency>
```

### 4.2 Dominio

#### `entity/TurnPhase.java` (nuovo)
```java
package com.project.fumanji.entity;

public enum TurnPhase {
    WAITING_ROLL,
    WAITING_DIRECTION,
    WAITING_CHOICE
}
```

#### `entity/Game.java` — solo le aggiunte (nient'altro è stato toccato)

Campo, insieme agli altri:
```java
private TurnPhase turnPhase = TurnPhase.WAITING_ROLL;
```

Metodi, subito prima di `roll(Player player)`:
```java
public synchronized int performRoll() {
    checkTurnAction(TurnPhase.WAITING_ROLL);
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
```

Getter, subito prima di `getBoard()`:
```java
public Player getCurrentPlayer() {
    return players.get(currentPlayerIndex);
}

public TurnPhase getTurnPhase() {
    return turnPhase;
}
```

### 4.3 Persistenza

#### Modifiche EAGER (solo righe cambiate)

`persistence/BoardEntity.java`:
```java
import jakarta.persistence.FetchType;
```
```java
@OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
private List<SquareEntity> squares = new ArrayList<>();
```

`persistence/squares/BivioSquareEntity.java`:
```java
import jakarta.persistence.FetchType;
```
```java
@ElementCollection(fetch = FetchType.EAGER)
@CollectionTable(name = "bivio_square_next", joinColumns = @JoinColumn(name = "square_id"))
@Column(name = "next_position")
private List<Integer> nextPositions = new ArrayList<>();

@ElementCollection(fetch = FetchType.EAGER)
@CollectionTable(name = "bivio_square_previous", joinColumns = @JoinColumn(name = "square_id"))
@Column(name = "previous_position")
private List<Integer> previousPositions = new ArrayList<>();
```

`persistence/squares/PortalSquareEntity.java`:
```java
import jakarta.persistence.FetchType;
```
```java
@ElementCollection(fetch = FetchType.EAGER)
@CollectionTable(name = "portal_square_link", joinColumns = @JoinColumn(name = "square_id"))
@Column(name = "linked_position")
private List<Integer> linkedPositions = new ArrayList<>();
```

#### `persistence/EndGameEntity.java` (nuovo)
```java
package com.project.fumanji.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class EndGameEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String winnerName;

    @ManyToOne
    @JoinColumn(name = "board_id")
    private BoardEntity board;

    protected EndGameEntity() {
    }

    public EndGameEntity(String winnerName, BoardEntity board) {
        this.winnerName = winnerName;
        this.board = board;
    }

    public Long getId() {
        return id;
    }

    public String getWinnerName() {
        return winnerName;
    }

    public BoardEntity getBoard() {
        return board;
    }
}
```
La tabella `end_game_entity` la crea Hibernate al primo avvio (`ddl-auto=update`) sul DB `fumanji`.

### 4.4 Repository

#### `repository/BoardRepository.java` (nuovo)
```java
package com.project.fumanji.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.fumanji.persistence.BoardEntity;

public interface BoardRepository extends JpaRepository<BoardEntity, Long> {
}
```

#### `repository/EndGameRepository.java` (nuovo)
```java
package com.project.fumanji.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.fumanji.persistence.EndGameEntity;

public interface EndGameRepository extends JpaRepository<EndGameEntity, Long> {
}
```

### 4.5 Service

#### `service/GameRegistry.java` (nuovo)
```java
package com.project.fumanji.service;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import com.project.fumanji.entity.Game;

/**
 * Registro in-memory delle partite in corso: nessuna persistenza per mossa,
 * il salvataggio su DB avviene solo a fine partita (vedi EndGameEntity, futuro).
 * ConcurrentHashMap perche' il bean e' singleton e le richieste REST arrivano
 * concorrenti su thread diversi.
 */
@Component
public class GameRegistry {

    private final Map<UUID, Game> games = new ConcurrentHashMap<>();

    public UUID register(Game game) {
        UUID gameId = UUID.randomUUID();
        games.put(gameId, game);
        return gameId;
    }

    public Optional<Game> get(UUID gameId) {
        return Optional.ofNullable(games.get(gameId));
    }

    public void remove(UUID gameId) {
        games.remove(gameId);
    }
}
```

#### `service/GameCreationResult.java` (nuovo)
```java
package com.project.fumanji.service;

import java.util.UUID;

public record GameCreationResult(UUID gameId, String currentPlayerName) {
}
```

#### `service/EndGameService.java` (nuovo)
```java
package com.project.fumanji.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.fumanji.entity.Game;
import com.project.fumanji.persistence.BoardEntity;
import com.project.fumanji.persistence.EndGameEntity;
import com.project.fumanji.repository.BoardRepository;
import com.project.fumanji.repository.EndGameRepository;

/**
 * Salva su DB l'esito di una partita conclusa.
 * Fase attuale: solo vincitore e board giocata - punto di estensione per lo storico completo.
 */
@Service
public class EndGameService {

    private final BoardRepository boardRepository;
    private final EndGameRepository endGameRepository;

    public EndGameService(BoardRepository boardRepository, EndGameRepository endGameRepository) {
        this.boardRepository = boardRepository;
        this.endGameRepository = endGameRepository;
    }

    @Transactional
    public void save(Game game, Long boardId) {
        BoardEntity board = boardRepository.getReferenceById(boardId);
        endGameRepository.save(new EndGameEntity(game.getWinner().getName(), board));
    }
}
```

#### `service/GameService.java` (nuovo, versione finale)
```java
package com.project.fumanji.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.project.fumanji.entity.Board;
import com.project.fumanji.entity.Dice;
import com.project.fumanji.entity.Game;
import com.project.fumanji.entity.Player;
import com.project.fumanji.exception.GameNotFoundException;
import com.project.fumanji.persistence.BoardAssembler;
import com.project.fumanji.persistence.BoardEntity;
import com.project.fumanji.repository.BoardRepository;

/**
 * Costruisce una nuova partita e la registra in GameRegistry.
 * Fase attuale: una sola board fissa (id 1, la board OG) - da rivedere
 * quando si supporteranno board multiple.
 */
@Service
public class GameService {

    private static final long OG_BOARD_ID = 1L;

    private final BoardRepository boardRepository;
    private final BoardAssembler boardAssembler;
    private final GameRegistry gameRegistry;
    private final EndGameService endGameService;

    public GameService(BoardRepository boardRepository, BoardAssembler boardAssembler,
            GameRegistry gameRegistry, EndGameService endGameService) {
        this.boardRepository = boardRepository;
        this.boardAssembler = boardAssembler;
        this.gameRegistry = gameRegistry;
        this.endGameService = endGameService;
    }

    public GameCreationResult createGame(List<String> playerNames) {
        BoardEntity boardEntity = boardRepository.findById(OG_BOARD_ID).orElseThrow();
        Board board = boardAssembler.toDomain(boardEntity);

        List<Player> players = new ArrayList<>();
        for (String name : playerNames) {
            players.add(new Player(name));
        }

        Game game = new Game(board, players, new Dice());
        UUID gameId = gameRegistry.register(game);
        return new GameCreationResult(gameId, players.get(0).getName());
    }

    public Game roll(UUID gameId) {
        Game game = getGame(gameId);
        game.performRoll();
        return game;
    }

    public Game move(UUID gameId, boolean forward) {
        Game game = getGame(gameId);
        game.performMove(forward);
        closeIfFinished(gameId, game);
        return game;
    }

    public Game choose(UUID gameId, int position) {
        Game game = getGame(gameId);
        game.performChoice(position);
        closeIfFinished(gameId, game);
        return game;
    }

    public Game getGame(UUID gameId) {
        return gameRegistry.get(gameId).orElseThrow(() -> new GameNotFoundException(gameId));
    }

    private void closeIfFinished(UUID gameId, Game game) {
        if (game.getWinner() == null) {
            return;
        }
        endGameService.save(game, OG_BOARD_ID);
        gameRegistry.remove(gameId);
    }
}
```

### 4.6 DTO

#### `dto/CreateGameRequest.java` (nuovo)
```java
package com.project.fumanji.dto;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

public record CreateGameRequest(@NotEmpty List<@NotBlank String> playerNames) {
}
```

#### `dto/CreateGameResponse.java` (nuovo)
```java
package com.project.fumanji.dto;

import java.util.UUID;

public record CreateGameResponse(UUID gameId, String currentPlayerName) {
}
```

#### `dto/PlayerStateResponse.java` (nuovo)
```java
package com.project.fumanji.dto;

public record PlayerStateResponse(String name, int position, int snack, int drink, int checkpoint) {
}
```

#### `dto/GameStateResponse.java` (nuovo)
```java
package com.project.fumanji.dto;

import java.util.List;

import com.project.fumanji.entity.TurnPhase;

public record GameStateResponse(
        String currentPlayerName,
        TurnPhase turnPhase,
        int lastRoll,
        List<PlayerStateResponse> players,
        List<Integer> choiceOptions,
        String winnerName) {
}
```

#### `dto/MoveRequest.java` (nuovo)
```java
package com.project.fumanji.dto;

import jakarta.validation.constraints.NotNull;

public record MoveRequest(@NotNull Boolean forward) {
}
```

#### `dto/ChoiceRequest.java` (nuovo)
```java
package com.project.fumanji.dto;

import jakarta.validation.constraints.NotNull;

public record ChoiceRequest(@NotNull Integer position) {
}
```

#### `dto/ErrorResponse.java` (nuovo)
```java
package com.project.fumanji.dto;

public record ErrorResponse(String message) {
}
```

### 4.7 Exception

#### `exception/GameNotFoundException.java` (nuovo)
```java
package com.project.fumanji.exception;

import java.util.UUID;

public class GameNotFoundException extends RuntimeException {

    public GameNotFoundException(UUID gameId) {
        super("Game not found: " + gameId);
    }
}
```

#### `exception/GlobalExceptionHandler.java` (nuovo)
```java
package com.project.fumanji.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.project.fumanji.dto.ErrorResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(GameNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleGameNotFound(GameNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(new ErrorResponse(ex.getMessage()));
    }
}
```

### 4.8 Mapper

#### `mapper/GameStateAssembler.java` (nuovo)
```java
package com.project.fumanji.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import com.project.fumanji.dto.GameStateResponse;
import com.project.fumanji.dto.PlayerStateResponse;
import com.project.fumanji.entity.Game;

@Component
public class GameStateAssembler {

    public GameStateResponse toResponse(Game game) {
        List<PlayerStateResponse> players = game.getPlayers().stream()
                .map(player -> new PlayerStateResponse(player.getName(), player.getPosition(),
                        player.getSnack(), player.getDrink(), player.getCheckpoint()))
                .toList();

        List<Integer> choiceOptions = game.isAwaitingChoice() ? List.copyOf(game.getChoiceOptions()) : null;
        String winnerName = game.getWinner() != null ? game.getWinner().getName() : null;

        return new GameStateResponse(game.getCurrentPlayer().getName(), game.getTurnPhase(),
                game.getLastRoll(), players, choiceOptions, winnerName);
    }
}
```

### 4.9 Controller

#### `api/GameController.java` (nuovo, versione finale)
```java
package com.project.fumanji.api;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.fumanji.dto.ChoiceRequest;
import com.project.fumanji.dto.CreateGameRequest;
import com.project.fumanji.dto.CreateGameResponse;
import com.project.fumanji.dto.GameStateResponse;
import com.project.fumanji.dto.MoveRequest;
import com.project.fumanji.entity.Game;
import com.project.fumanji.mapper.GameStateAssembler;
import com.project.fumanji.service.GameCreationResult;
import com.project.fumanji.service.GameService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/games")
public class GameController {

    private final GameService gameService;
    private final GameStateAssembler gameStateAssembler;

    public GameController(GameService gameService, GameStateAssembler gameStateAssembler) {
        this.gameService = gameService;
        this.gameStateAssembler = gameStateAssembler;
    }

    @PostMapping
    public ResponseEntity<CreateGameResponse> createGame(@RequestBody @Valid CreateGameRequest request) {
        GameCreationResult result = gameService.createGame(request.playerNames());
        return ResponseEntity.ok(new CreateGameResponse(result.gameId(), result.currentPlayerName()));
    }

    @GetMapping("/{gameId}")
    public ResponseEntity<GameStateResponse> getGame(@PathVariable UUID gameId) {
        Game game = gameService.getGame(gameId);
        return ResponseEntity.ok(gameStateAssembler.toResponse(game));
    }

    @PostMapping("/{gameId}/roll")
    public ResponseEntity<GameStateResponse> roll(@PathVariable UUID gameId) {
        Game game = gameService.roll(gameId);
        return ResponseEntity.ok(gameStateAssembler.toResponse(game));
    }

    @PostMapping("/{gameId}/move")
    public ResponseEntity<GameStateResponse> move(@PathVariable UUID gameId,
            @RequestBody @Valid MoveRequest request) {
        Game game = gameService.move(gameId, request.forward());
        return ResponseEntity.ok(gameStateAssembler.toResponse(game));
    }

    @PostMapping("/{gameId}/choice")
    public ResponseEntity<GameStateResponse> choice(@PathVariable UUID gameId,
            @RequestBody @Valid ChoiceRequest request) {
        Game game = gameService.choose(gameId, request.position());
        return ResponseEntity.ok(gameStateAssembler.toResponse(game));
    }
}
```

---

## 5. Test e risorse di test

### 5.1 `src/test/resources/application-test.properties` (nuovo)
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/fumanji_test?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=1234
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=false
```
Prerequisito: schema `fumanji_test` creato a mano (`CREATE DATABASE fumanji_test;`). Nota: le properties di profilo si **sommano** a quelle di `application.properties` (es. `logging.level.root=warn` resta attivo nei test).

### 5.2 `src/test/resources/seed-og-board.sql` (nuovo)
Copia identica, senza modifiche, dello script di seed della sezione 9 del dump precedente.

### 5.3 `test/java/com/project/fumanji/persistence/BoardAssemblerIT.java` (nuovo)
```java
package com.project.fumanji.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import com.project.fumanji.entity.Board;
import com.project.fumanji.entity.Square;
import com.project.fumanji.entity.squares.BivioSquare;
import com.project.fumanji.entity.squares.CheckpointSquare;
import com.project.fumanji.repository.BoardRepository;

@SpringBootTest
@ActiveProfiles("test")
@Sql("/seed-og-board.sql")
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

        Square square6 = board.getSquares().get(6);
        assertThat(square6).isInstanceOf(BivioSquare.class);
        assertThat(square6.getNextPositions()).containsExactly(10, 40);

        Square square81 = board.getSquares().get(81);
        assertThat(square81).isInstanceOf(CheckpointSquare.class);
        assertThat(square81.isMandatoryStop()).isTrue();
    }
}
```
Smoke test: non è la regressione completa (68 caselle, tutti i next/previous dei 10 bivi, i 6 portali) — quella resta un'opzione.

### 5.4 `test/java/com/project/fumanji/api/GameControllerIT.java` (nuovo, versione finale)
```java
package com.project.fumanji.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.util.JsonPathExpectationsHelper;
import org.springframework.test.web.servlet.MockMvc;

import com.project.fumanji.entity.Board;
import com.project.fumanji.entity.Dice;
import com.project.fumanji.entity.Game;
import com.project.fumanji.entity.Player;
import com.project.fumanji.repository.EndGameRepository;
import com.project.fumanji.service.GameRegistry;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql(scripts = "/seed-og-board.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
class GameControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private GameRegistry gameRegistry;

    @Autowired
    private EndGameRepository endGameRepository;

    private static class FixedDice extends Dice {

        private final int value;

        private FixedDice(int value) {
            this.value = value;
        }

        @Override
        public int roll() {
            return value;
        }
    }

    private String createGame() throws Exception {
        String responseBody = mockMvc.perform(post("/api/games")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"playerNames": ["Mario", "Luigi"]}
                                """))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return (String) new JsonPathExpectationsHelper("$.gameId").evaluateJsonPath(responseBody);
    }

    @Test
    void createsGameAndReturnsFirstPlayerAsCurrent() throws Exception {
        String requestBody = """
                {"playerNames": ["Mario", "Luigi"]}
                """;

        mockMvc.perform(post("/api/games")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameId", notNullValue()))
                .andExpect(jsonPath("$.currentPlayerName", is("Mario")));
    }

    @Test
    void rejectsEmptyPlayerNames() throws Exception {
        String requestBody = """
                {"playerNames": []}
                """;

        mockMvc.perform(post("/api/games")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rollThenMovePassesTurnToNextPlayer() throws Exception {
        String gameId = createGame();

        mockMvc.perform(post("/api/games/{gameId}/roll", gameId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentPlayerName", is("Mario")))
                .andExpect(jsonPath("$.turnPhase", is("WAITING_DIRECTION")))
                .andExpect(jsonPath("$.lastRoll", allOf(greaterThanOrEqualTo(1), lessThanOrEqualTo(6))));

        mockMvc.perform(post("/api/games/{gameId}/move", gameId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"forward": true}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentPlayerName", is("Luigi")))
                .andExpect(jsonPath("$.turnPhase", is("WAITING_ROLL")))
                .andExpect(jsonPath("$.choiceOptions").doesNotExist());
    }

    @Test
    void rejectsMoveBeforeRoll() throws Exception {
        String gameId = createGame();

        mockMvc.perform(post("/api/games/{gameId}/move", gameId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"forward": true}
                                """))
                .andExpect(status().isConflict());
    }

    @Test
    void rejectsSecondRollInSameTurn() throws Exception {
        String gameId = createGame();

        mockMvc.perform(post("/api/games/{gameId}/roll", gameId))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/games/{gameId}/roll", gameId))
                .andExpect(status().isConflict());
    }

    @Test
    void rejectsChoiceWithoutPendingChoice() throws Exception {
        String gameId = createGame();

        mockMvc.perform(post("/api/games/{gameId}/choice", gameId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"position": 10}
                                """))
                .andExpect(status().isConflict());
    }

    @Test
    void rejectsMoveWithoutDirection() throws Exception {
        String gameId = createGame();

        mockMvc.perform(post("/api/games/{gameId}/move", gameId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void returnsNotFoundForUnknownGame() throws Exception {
        mockMvc.perform(post("/api/games/{gameId}/roll", UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
    void savesEndGameAndRemovesGameFromRegistryWhenThereIsAWinner() throws Exception {
        Game game = new Game(new Board(5), List.of(new Player("Mario"), new Player("Luigi")), new FixedDice(5));
        UUID gameId = gameRegistry.register(game);
        long endGamesBefore = endGameRepository.count();

        mockMvc.perform(post("/api/games/{gameId}/roll", gameId))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/games/{gameId}/move", gameId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"forward": true}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.winnerName", is("Mario")));

        assertThat(endGameRepository.count()).isEqualTo(endGamesBefore + 1);
        assertThat(gameRegistry.get(gameId)).isEmpty();
        mockMvc.perform(post("/api/games/{gameId}/roll", gameId))
                .andExpect(status().isNotFound());
    }

    @Test
    void returnsCurrentStateOfExistingGame() throws Exception {
        String gameId = createGame();

        mockMvc.perform(get("/api/games/{gameId}", gameId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentPlayerName", is("Mario")))
                .andExpect(jsonPath("$.turnPhase", is("WAITING_ROLL")))
                .andExpect(jsonPath("$.players", hasSize(2)));
    }

    @Test
    void returnsNotFoundWhenReadingUnknownGame() throws Exception {
        mockMvc.perform(get("/api/games/{gameId}", UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }
}
```

---

## 6. Stato delle verifiche

- **JUnit**: `BoardAssemblerIT` (1 test) e `GameControllerIT` (11 test) tutti verdi. `GameController` testato con MockMvc (`@SpringBootTest` + `@AutoConfigureMockMvc`) contro lo schema `fumanji_test` seedato.
- **Postman**: `POST /api/games` verificato positivo sul DB reale. I test Postman di `roll`/`move`/`choice`/`GET` sono rimandati dall'utente, da considerare positivi finché non segnala problemi.
- **Sandbox (logica delle fasi del turno)**: verificata compilando il dominio Java puro dal dump con `Game` modificato, con 16 controlli tutti ok: stato iniziale; `move` senza `roll` rifiutato; `choice` senza scelta pendente rifiutata; doppio `roll` rifiutato; `move` che arriva a un bivio → `WAITING_CHOICE` senza avanzare il turno; `roll` con scelta pendente rifiutato; scelta non valida → `IllegalArgumentException` con fase invariata; scelta valida → turno al giocatore successivo; scelta indietro dal via; vittoria (turno non avanza, azioni successive rifiutate); `RollAgainSquare` a catena (4→7); portale con scelta (atterraggio esatto).
- Il codice Spring/JPA di questa sessione è stato verificato dall'utente in locale (compilazione e test), non in sandbox.

---

## 7. TODO / prossimi passi (voci aperte o cambiate in questa sessione)

1. **Rendere `Dice` iniettabile** (bean/parametro) così i test possono fissare il risultato del dado e coprire il caso felice di `choice`, la scelta non valida e altri scenari. **L'utente ha chiesto di riproporlo all'inizio della prossima sessione.**
2. **Test Postman** di `roll`/`move`/`choice`/`GET`: l'utente li fa appena ha tempo e scrive solo se sono negativi.
3. **Frontend Angular** (prossimo passo pianificato): schermata iniziale con numero di giocatori + nomi, nessun login; i nomi vanno inseriti nell'ordine di gioco (vedi 3.4). Servirà anche gestire le risposte `404`/`409`/`400` dell'API.
4. **TTL** per le partite abbandonate in `GameRegistry` (`@Scheduled`): futuro prossimo (vedi 3.2).
5. **Storico/statistiche complete a fine partita**: ridisegnare `Game.movementTrace` per-giocatore e definire quali statistiche tracciare; estendere `EndGameService` (vedi 3.3).
6. **Rivalutare EAGER** (3.1) e l'`OG_BOARD_ID` fisso in `GameService` (`createGame` e `closeIfFinished`) quando si supporteranno board multiple.
7. **`playTurn()`** in `Game`: non passa dalle fasi e nessun endpoint lo usa; è candidato alla rimozione, **non deciso**.
8. **`BoardAssemblerIT`**: se gli si aggiunge un secondo `@Test`, passare `@Sql` a `executionPhase = BEFORE_TEST_CLASS` (vedi 3.8). Eventuale regressione completa della board OG (68 caselle, next/previous dei 10 bivi, 6 portali) al posto dello smoke test.
9. **Test JUnit con dado fisso a livello di dominio/servizio** per il caso `WAITING_CHOICE` end-to-end dopo il punto 1.
