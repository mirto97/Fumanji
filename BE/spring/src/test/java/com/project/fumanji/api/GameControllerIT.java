package com.project.fumanji.api;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.util.JsonPathExpectationsHelper;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.project.fumanji.entity.BoardAssembler;
import com.project.fumanji.entity.BoardEntity;
import com.project.fumanji.entity.ControllableDice;
import com.project.fumanji.model.Board;
import com.project.fumanji.model.Game;
import com.project.fumanji.model.Player;
import com.project.fumanji.model.Square;
import com.project.fumanji.model.squares.BivioSquare;
import com.project.fumanji.model.squares.CheckpointSquare;
import com.project.fumanji.model.squares.DrinkSquare;
import com.project.fumanji.model.squares.ObligationSquare;
import com.project.fumanji.model.squares.PortalSquare;
import com.project.fumanji.model.squares.SnackSquare;
import com.project.fumanji.model.squares.StartOverSquare;
import com.project.fumanji.repository.BoardRepository;
import com.project.fumanji.repository.EndGameRepository;
import com.project.fumanji.service.GameRegistry;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql(scripts = "/seed-og-board.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS )
class GameControllerIT {

    // TODO test commentareeeeeeeee

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private GameRegistry gameRegistry;

    @Autowired
    private EndGameRepository endGameRepository;

    @Autowired 
    private BoardAssembler boardAssembler;

    @Autowired 
    private BoardRepository boardRepository;

    @Autowired
    private ControllableDice dice;

    @BeforeEach
    void resetDice() {
        dice.setNextRoll(1);
    }

    @TestConfiguration
    static class DiceTestConfig {

        @Bean
        @Primary
        public ControllableDice testDice() {
            return new ControllableDice();
        }
    }

    // helper

    private ResultActions choose(UUID gameId, int position) throws Exception {
        return mockMvc.perform(post("/api/games/{gameId}/choice", gameId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"position\": " + position + "}"));
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



    // test

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

    @Test
    void savesEndGameAndRemovesGameFromRegistryWhenThereIsAWinner() throws Exception {
        dice.setNextRoll(5);
        Game game = new Game(new Board(5), List.of(new Player("Mario"), new Player("Luigi")), dice);
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

    /**
     * metodo di supporto
     * @return
     * @throws Exception
     */
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
                .andExpect(jsonPath("$.choiceOptions", containsInAnyOrder(10, 40)))
                .andExpect(jsonPath("$.choiceRemainingSteps", is(1)));

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

    @Test
    void clearsMessagesOnNextRoll() throws Exception {
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
                .andExpect(jsonPath("$.messages", hasSize(1)));

        mockMvc.perform(post("/api/games/{gameId}/roll", gameId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.messages").doesNotExist());
    }

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
                        is("Mario torna all'ultimo checkpoint")))
                .andExpect(jsonPath("$.players[0].position", is(0)));
    }

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
}