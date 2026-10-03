package com.project.fumanji.entity.squares;

import com.project.fumanji.entity.SquareEntity;

import jakarta.persistence.Entity;

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