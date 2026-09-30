package com.project.fumanji.exception;

public class BoardNotFoundException extends RuntimeException {

    public BoardNotFoundException(Long boardId) {
        super("Board not found: " + boardId);
    }
}
