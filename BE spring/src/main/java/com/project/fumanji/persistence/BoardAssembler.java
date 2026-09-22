package com.project.fumanji.persistence;

import org.springframework.stereotype.Component;

import com.project.fumanji.entity.Board;

@Component
public class BoardAssembler {

    private final SquareMapper squareMapper;

    public BoardAssembler(SquareMapper squareMapper) {
        this.squareMapper = squareMapper;
    }

    public Board toDomain(BoardEntity entity) {
        Board.Builder builder = Board.builder(entity.getFinishPosition())
                .directionChoiceEnabled(entity.isDirectionChoiceEnabled())
                .mandatoryCheckpointEnabled(entity.isMandatoryCheckpointEnabled());

        for (SquareEntity squareEntity : entity.getSquares()) {
            builder.square(squareEntity.getPosition(), squareMapper.toDomain(squareEntity));
        }

        return builder.build();
    }
}