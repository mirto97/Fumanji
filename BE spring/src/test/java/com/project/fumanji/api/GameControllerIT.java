package com.project.fumanji.api;

import static org.hamcrest.Matchers.allOf;
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

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
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
@Sql(scripts = "/seed-og-board.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS )
class GameControllerIT {

    // TODO test commentareeeeeeeee

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


}