package com.project.fumanji.entity.squares;

import com.project.fumanji.entity.SquareEntity;

import jakarta.persistence.Entity;

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