package com.project.fumanji.persistence;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
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

    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SquareEntity> squares = new ArrayList<>();

    protected BoardEntity() {
    }

    public BoardEntity(int finishPosition, boolean directionChoiceEnabled, boolean mandatoryCheckpointEnabled) {
        this.finishPosition = finishPosition;
        this.directionChoiceEnabled = directionChoiceEnabled;
        this.mandatoryCheckpointEnabled = mandatoryCheckpointEnabled;
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
}