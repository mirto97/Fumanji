# FUMANJI — Dump di sessione (delta)

> **Data e ora**: 29/09/2026 (Europe/Rome).
> **Natura del file**: contiene solo ciò che è stato fatto/deciso in questa conversazione. Da unire a parte ai dump precedenti (24-09-26 e 28-09-26_0429), che restano validi per tutto ciò che qui non compare.
> Dump snello: cose cancellate/superate/obsolete non compaiono.

---

## 1. Cosa è stato fatto in questa sessione (indice)

- Dice reso iniettabile (bean Spring), con test dedicati.
- `playTurn()`: deciso di mantenerlo, non rimosso.
- `BoardAssemblerIT`: regressione completa della board OG (prima era solo uno smoke test).
- TTL sulle partite abbandonate (`GameRegistry`): confermato rimandato, non necessario ora.

---

## 2. Decisioni di design (delta)

### 2.1 Dice iniettabile
- Nuovo bean Spring per `Dice` invece di `@Component` diretto sulla classe: `Dice` resta pure Java, senza annotazioni, coerente col dominio puro (stesso principio della persistenza separata). Bean dichiarato in un nuovo `GameConfig` (`@Configuration`, `@Bean Dice dice()`).
- `GameService` riceve un'unica istanza `Dice` condivisa via costruttore, al posto di `new Dice()` per ogni partita creata in `createGame` (`Dice` è stateless — `ThreadLocalRandom.current()` — condivisione sicura, tiro sempre casuale in produzione).
- Nei test (`GameControllerIT`), il bean di produzione viene sostituito da `ControllableDice` (`extends Dice`, `setNextRoll(int)` impostabile a runtime) tramite un `@TestConfiguration` + `@Primary` interno alla classe di test. Sostituisce il precedente `FixedDice` a valore fisso nel costruttore.
- **Gotcha Spring Boot scoperto in sessione**: un bean di test con lo stesso nome (= nome del metodo `@Bean`) di un bean di produzione fallisce la registrazione (`BeanDefinitionOverrideException`) prima che `@Primary` possa intervenire — `@Primary` risolve solo l'ambiguità fra più bean candidati per tipo, non un conflitto di nome. Fix: rinominato il bean di test da `dice()` a `testDice()`. Il campo `@Autowired ControllableDice dice` nel test resta risolto per tipo, il nome del bean non conta ai fini dell'injection.
- `@BeforeEach` aggiunto in `GameControllerIT` per resettare il dado a un valore neutro prima di ogni test (isolamento fra metodi, dato che il bean è condiviso nel contesto Spring cachato).
- Sbloccati due scenari HTTP prima non testabili: scelta valida a bivio (con prosecuzione dei passi residui) e scelta non valida (400).

### 2.2 playTurn()
- Deciso di mantenerlo: non passa dalle fasi (`TurnPhase`) e nessun endpoint lo usa oggi, ma resta per un futuro utilizzo non ancora definito.

### 2.3 Regressione completa BoardAssemblerIT
- Con un secondo `@Test` nella classe, serviva la stessa correzione già nota da `GameControllerIT`: `@Sql` passato a `executionPhase = BEFORE_TEST_CLASS` (il default riesegue il seed prima di ogni metodo, violando la chiave primaria dal secondo test in poi).
- Nuovo test che copre: tipo esatto di tutte le 68 caselle piazzate + le caselle base attese (partenza, arrivo, i 6 gap "no smoke"); `next`/`previous` di tutti i 10 bivi; collegamenti dei 6 portali (ognuno verso le altre 5 posizioni); `amount` di tutte le caselle Snack/Drink.
- Aggiunti getter pubblici mancanti per coerenza con `BivioSquare` (che espone già `getNextPositions()`/`getPreviousPositions()` pubblici, nessuna copia difensiva): `PortalSquare.getLinkedPositions()`, `SnackSquare.getAmount()`, `DrinkSquare.getAmount()`.

### 2.4 TTL partite abbandonate (GameRegistry)
- Ridiscusso e confermato rimandato: nessun deploy con uptime lungo o utenti concorrenti reali per ora, il rischio non si manifesta ancora. Resta "miglioramento futuro prossimo" come già segnato nel dump precedente.

---

## 3. Codice — versioni di questa sessione

### 3.1 Nuovi file

#### `config/GameConfig.java`
```java
package com.project.fumanji.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.project.fumanji.entity.Dice;

@Configuration
public class GameConfig {

    @Bean
    public Dice dice() {
        return new Dice();
    }
}
```

#### `test/java/com/project/fumanji/entity/ControllableDice.java`
```java
package com.project.fumanji.entity;

/**
 * Dado di test: valore del prossimo tiro impostabile a piacere,
 * per coprire nei test scenari che dipendono dal risultato del dado.
 */
public class ControllableDice extends Dice {

    private int nextRoll = 1;

    public void setNextRoll(int nextRoll) {
        this.nextRoll = nextRoll;
    }

    @Override
    public int roll() {
        return nextRoll;
    }
}
```

### 3.2 `GameService.java` — solo le righe cambiate
```java
private final BoardRepository boardRepository;
private final BoardAssembler boardAssembler;
private final GameRegistry gameRegistry;
private final EndGameService endGameService;
private final Dice dice;

public GameService(BoardRepository boardRepository, BoardAssembler boardAssembler,
        GameRegistry gameRegistry, EndGameService endGameService, Dice dice) {
    this.boardRepository = boardRepository;
    this.boardAssembler = boardAssembler;
    this.gameRegistry = gameRegistry;
    this.endGameService = endGameService;
    this.dice = dice;
}
```
e dentro `createGame`:
```java
Game game = new Game(board, players, dice);
```
(era `new Game(board, players, new Dice())`)

### 3.3 Dominio — nuovi getter

`entity/squares/PortalSquare.java`:
```java
public List<Integer> getLinkedPositions() {
    return linkedPositions;
}
```

`entity/squares/SnackSquare.java`:
```java
public int getAmount() {
    return amount;
}
```

`entity/squares/DrinkSquare.java`:
```java
public int getAmount() {
    return amount;
}
```

### 3.4 Test

#### `GameControllerIT.java` — solo le aggiunte/modifiche

Import aggiunti:
```java
import static org.hamcrest.Matchers.containsInAnyOrder;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import com.project.fumanji.entity.ControllableDice;
```
Import rimosso: `com.project.fumanji.entity.Dice` (era usato solo dalla classe `FixedDice`, rimossa).

Classe nested `FixedDice` rimossa, sostituita da:
```java
@TestConfiguration
static class DiceTestConfig {

    @Bean
    @Primary
    public ControllableDice testDice() {
        return new ControllableDice();
    }
}
```

Nuovo campo:
```java
@Autowired
private ControllableDice dice;
```

Nuovo metodo:
```java
@BeforeEach
void resetDice() {
    dice.setNextRoll(1);
}
```

In `savesEndGameAndRemovesGameFromRegistryWhenThereIsAWinner`, prima riga cambiata:
```java
dice.setNextRoll(5);
Game game = new Game(new Board(5), List.of(new Player("Mario"), new Player("Luigi")), dice);
```
(era `new Game(..., new FixedDice(5))`)

Due nuovi test:
```java
@Test
void choiceMovesPlayerAndContinuesRemainingSteps() throws Exception {
    String gameId = createGame();
    dice.setNextRoll(7);

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
    dice.setNextRoll(7);

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
```
Usano la casella reale della board OG (Bivio A, posizione 6, `next=[10,40]`); tiro di 7 (fuori dal range reale 1-6) è un artificio di test per raggiungere la scelta dal via senza simulare più turni.

#### `BoardAssemblerIT.java` — solo le aggiunte/modifiche

Annotazione di classe cambiata:
```java
@Sql(scripts = "/seed-og-board.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
```
(era `@Sql("/seed-og-board.sql")`)

Import aggiunti:
```java
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.project.fumanji.entity.squares.DrinkSquare;
import com.project.fumanji.entity.squares.ObligationSquare;
import com.project.fumanji.entity.squares.PortalSquare;
import com.project.fumanji.entity.squares.SnackSquare;
import com.project.fumanji.entity.squares.StartOverSquare;
```

Nuovo test + helper:
```java
@Test
void reconstructsAllOgBoardSquaresCorrectly() {
    BoardEntity entity = boardRepository.findById(1L).orElseThrow();
    Board board = boardAssembler.toDomain(entity);
    List<Square> squares = board.getSquares();

    // caselle base: partenza, arrivo, i 6 gap "no smoke"
    List<Integer> plainPositions = List.of(0, 5, 24, 54, 85, 97, 101, 110);
    for (int position : plainPositions) {
        assertThat(squares.get(position)).as("position %d", position).isExactlyInstanceOf(Square.class);
    }

    // tipi delle 68 caselle piazzate
    Map<Integer, Class<? extends Square>> expectedTypes = new HashMap<>();
    expectedTypes.put(1, ObligationSquare.class);
    expectedTypes.put(2, ObligationSquare.class);
    expectedTypes.put(3, DrinkSquare.class);
    expectedTypes.put(4, StartOverSquare.class);
    expectedTypes.put(6, BivioSquare.class);
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
    expectedTypes.put(80, BivioSquare.class);
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

    // next/previous dei 10 bivi
    assertBivio(squares, 6, List.of(10, 40), List.of(5));
    assertBivio(squares, 12, List.of(16, 22), List.of(11));
    assertBivio(squares, 17, List.of(70), List.of(16));
    assertBivio(squares, 33, List.of(80), List.of(32));
    assertBivio(squares, 42, List.of(46, 52), List.of(41));
    assertBivio(squares, 47, List.of(70), List.of(46));
    assertBivio(squares, 63, List.of(80), List.of(62));
    assertBivio(squares, 70, List.of(71), List.of(17, 47));
    assertBivio(squares, 73, List.of(80), List.of(72));
    assertBivio(squares, 80, List.of(81), List.of(33, 63, 73));

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

    assertDrinkAmount(squares, 3, 1);
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
```
Gli `amount` escludono le righe "effetto" dei bivi (id `1006` Snack, `1047` Drink): non sono posizionate sulla board (`board_id = NULL`), quindi non entrano mai in `board.getSquares()`.

---

## 4. Stato delle verifiche
- Tutti i test JUnit (`GameControllerIT`, inclusi i due nuovi sulla scelta a bivio; `BoardAssemblerIT`, inclusa la regressione completa) verificati verdi in locale.

---

## 5. TODO / prossimi passi (aggiornato)

### Chiuso in questa sessione
- ~~Dice iniettabile~~ → `GameConfig` + `GameService` + `ControllableDice`, test sul caso felice e sulla scelta non valida verdi.
- ~~`playTurn()`: rimuovere o mantenere~~ → mantenuto.
- ~~`BoardAssemblerIT`: regressione completa~~ → fatta (tipi, bivi, portali, amount), `executionPhase` corretto.

### Confermato rimandato (nessun cambiamento rispetto al dump precedente)
- TTL per le partite abbandonate in `GameRegistry` (`@Scheduled`) — non necessario ora, nessun deploy con uptime lungo/utenti concorrenti reali.
- Storico/statistiche complete a fine partita.
- Rivalutare EAGER e `OG_BOARD_ID` fisso in `GameService` — quando si supporteranno board multiple.
- Test Postman di roll/move/choice/GET — a carico dell'utente.

### Prossimo passo
- Frontend Angular (deciso a inizio sessione di rimandarlo a dopo il lavoro di backend).