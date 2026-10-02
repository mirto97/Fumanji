package com.project.fumanji.entity;

import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "square_type")
public abstract class SquareEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "board_id")
    private BoardEntity board;

    private int position;

    protected SquareEntity() {
    }

    public SquareEntity(int position) {
        this.position = position;
    }

    void setBoard(BoardEntity board) {
        this.board = board;
    }

    public Long getId() {
        return id;
    }

    public BoardEntity getBoard() {
        return board;
    }

    public int getPosition() {
        return position;
    }
}