package com.project.fumanji.persistence.squares;

import jakarta.persistence.Entity;

import com.project.fumanji.persistence.SquareEntity;

@Entity
public class StartOverSquareEntity extends SquareEntity {

    protected StartOverSquareEntity() {
    }

    public StartOverSquareEntity(int position) {
        super(position);
    }
}