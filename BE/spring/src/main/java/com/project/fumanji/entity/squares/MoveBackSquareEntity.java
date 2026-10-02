package com.project.fumanji.entity.squares;

import com.project.fumanji.entity.SquareEntity;

import jakarta.persistence.Entity;

@Entity
public class MoveBackSquareEntity extends SquareEntity {

    private int steps;

    protected MoveBackSquareEntity() {
    }

    public MoveBackSquareEntity(int position, int steps) {
        super(position);
        this.steps = steps;
    }

    public int getSteps() {
        return steps;
    }
}