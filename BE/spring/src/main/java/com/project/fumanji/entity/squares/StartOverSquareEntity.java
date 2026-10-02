package com.project.fumanji.entity.squares;

import com.project.fumanji.entity.SquareEntity;

import jakarta.persistence.Entity;

@Entity
public class StartOverSquareEntity extends SquareEntity {

    protected StartOverSquareEntity() {
    }

    public StartOverSquareEntity(int position) {
        super(position);
    }
}