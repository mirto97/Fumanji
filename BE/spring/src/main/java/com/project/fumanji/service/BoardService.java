package com.project.fumanji.service;

import com.project.fumanji.entity.Board;
import com.project.fumanji.exception.BoardNotFoundException;
import com.project.fumanji.persistence.BoardAssembler;
import com.project.fumanji.persistence.BoardEntity;
import com.project.fumanji.repository.BoardRepository;

public class BoardService {
    
        private final BoardRepository boardRepository;
    private final BoardAssembler boardAssembler;

    public BoardService(BoardRepository boardRepository, BoardAssembler boardAssembler) {
        this.boardRepository = boardRepository;
        this.boardAssembler = boardAssembler;
    }

    public Board getBoard(Long boardId) {
        BoardEntity entity = boardRepository.findById(boardId)
                .orElseThrow(() -> new BoardNotFoundException(boardId));
        return boardAssembler.toDomain(entity);
    }
}
