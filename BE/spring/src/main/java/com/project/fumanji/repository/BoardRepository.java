package com.project.fumanji.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.fumanji.entity.BoardEntity;

public interface BoardRepository extends JpaRepository<BoardEntity, Long> {
    
}
