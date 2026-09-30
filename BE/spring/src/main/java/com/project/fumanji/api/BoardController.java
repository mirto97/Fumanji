package com.project.fumanji.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.fumanji.dto.BoardResponse;
import com.project.fumanji.mapper.BoardResponseAssembler;
import com.project.fumanji.service.BoardService;

@RestController
@RequestMapping("/api/boards")
public class BoardController {

    private final BoardService boardService;
    private final BoardResponseAssembler boardResponseAssembler;

    public BoardController(BoardService boardService, BoardResponseAssembler boardResponseAssembler) {
        this.boardService = boardService;
        this.boardResponseAssembler = boardResponseAssembler;
    }

    @GetMapping("/{boardId}")
    public ResponseEntity<BoardResponse> getBoard(@PathVariable Long boardId) {
        return ResponseEntity.ok(boardResponseAssembler.toResponse(boardService.getBoard(boardId)));
    }
}