package com.project.fumanji.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.fumanji.persistence.EndGameEntity;

public interface EndGameRepository extends JpaRepository<EndGameEntity, Long> {
    
}
