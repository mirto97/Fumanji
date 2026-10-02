package com.project.fumanji.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class EndGameEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String winnerName;

    @ManyToOne
    @JoinColumn(name = "board_id")
    private BoardEntity board;

    protected EndGameEntity() {
    }

    public EndGameEntity(String winnerName, BoardEntity board) {
        this.winnerName = winnerName;
        this.board = board;
    }

    public Long getId() {
        return id;
    }

    public String getWinnerName() {
        return winnerName;
    }

    public BoardEntity getBoard() {
        return board;
    }
}