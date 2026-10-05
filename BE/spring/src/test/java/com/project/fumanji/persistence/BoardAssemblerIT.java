package com.project.fumanji.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import com.project.fumanji.entity.BoardAssembler;
import com.project.fumanji.entity.BoardEntity;
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

        // next/previous dei 10 bivi
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

    // helper

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