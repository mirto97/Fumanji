package com.project.fumanji.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.project.fumanji.persistence.BoardEntity;

public interface BoardRepository extends JpaRepository<BoardEntity, Long> {
    
}
