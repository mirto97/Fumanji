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