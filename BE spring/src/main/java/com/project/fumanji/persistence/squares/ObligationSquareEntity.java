package com.project.fumanji.persistence.squares;

import jakarta.persistence.Entity;

import com.project.fumanji.persistence.SquareEntity;

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