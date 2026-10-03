package com.project.fumanji.entity.squares;

import com.project.fumanji.entity.SquareEntity;

import jakarta.persistence.Entity;

@Entity
public class RollAgainSquareEntity extends SquareEntity {
    
    protected RollAgainSquareEntity() {
    }

    public RollAgainSquareEntity(int position) {
        super(position);
    }
}
