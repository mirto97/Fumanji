package com.project.fumanji.persistence.squares;

import com.project.fumanji.persistence.SquareEntity;

import jakarta.persistence.Entity;

@Entity
public class RollAgainSquareEntity extends SquareEntity {
    
    protected RollAgainSquareEntity() {
    }

    public RollAgainSquareEntity(int position) {
        super(position);
    }
}
