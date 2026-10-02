package com.project.fumanji.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

@Entity
public class BoardEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int finishPosition;
    private boolean directionChoiceEnabled;
    private boolean mandatoryCheckpointEnabled;
    private boolean backwardAllowedAfterCheckpoint;

    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<SquareEntity> squares = new ArrayList<>();

    protected BoardEntity() {
    }

    public BoardEntity(int finishPosition, boolean directionChoiceEnabled, boolean mandatoryCheckpointEnabled, boolean backwardAllowedAfterCheckpoint) {
        this.finishPosition = finishPosition;
        this.directionChoiceEnabled = directionChoiceEnabled;
        this.mandatoryCheckpointEnabled = mandatoryCheckpointEnabled;
        this.backwardAllowedAfterCheckpoint = backwardAllowedAfterCheckpoint;
    }

    public void addSquare(SquareEntity square) {
        squares.add(square);
        square.setBoard(this);
    }

    public Long getId() {
        return id;
    }

    public int getFinishPosition() {
        return finishPosition;
    }

    public boolean isDirectionChoiceEnabled() {
        return directionChoiceEnabled;
    }

    public boolean isMandatoryCheckpointEnabled() {
        return mandatoryCheckpointEnabled;
    }

    public List<SquareEntity> getSquares() {
        return squares;
    }

    public boolean isBackwardAllowedAfterCheckpoint() {
        return backwardAllowedAfterCheckpoint;
    }
}