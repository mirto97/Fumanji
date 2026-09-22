package com.project.fumanji.persistence.squares;

import jakarta.persistence.Entity;

import com.project.fumanji.persistence.SquareEntity;

@Entity
public class DrinkSquareEntity extends SquareEntity {

    private int amount;

    protected DrinkSquareEntity() {
    }

    public DrinkSquareEntity(int position, int amount) {
        super(position);
        this.amount = amount;
    }

    public int getAmount() {
        return amount;
    }
}