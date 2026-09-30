package com.project.fumanji.persistence.squares;

import jakarta.persistence.Entity;

import com.project.fumanji.persistence.SquareEntity;

@Entity
public class MoveForwardSquareEntity extends SquareEntity {

    private int steps;

    protected MoveForwardSquareEntity() {
    }

    public MoveForwardSquareEntity(int position, int steps) {
        super(position);
        this.steps = steps;
    }

    public int getSteps() {
        return steps;
    }
}