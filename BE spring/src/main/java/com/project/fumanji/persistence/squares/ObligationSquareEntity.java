package com.project.fumanji.persistence.squares;

import jakarta.persistence.Entity;

import com.project.fumanji.persistence.SquareEntity;

@Entity
public class ObligationSquareEntity extends SquareEntity {

    protected ObligationSquareEntity() {
    }

    public ObligationSquareEntity(int position) {
        super(position);
    }
}