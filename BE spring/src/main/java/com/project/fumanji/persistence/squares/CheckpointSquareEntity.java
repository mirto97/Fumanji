package com.project.fumanji.persistence.squares;

import jakarta.persistence.Entity;

import com.project.fumanji.persistence.SquareEntity;

@Entity
public class CheckpointSquareEntity extends SquareEntity {

    protected CheckpointSquareEntity() {
    }

    public CheckpointSquareEntity(int position) {
        super(position);
    }
}