package com.project.fumanji.entity.squares;

import com.project.fumanji.entity.SquareEntity;

import jakarta.persistence.Entity;

@Entity
public class ObligationSquareEntity extends SquareEntity {

    private String message;
    
    protected ObligationSquareEntity() {
    }

    public ObligationSquareEntity(int position, String message) {
        super(position);
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}