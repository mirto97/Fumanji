package com.project.fumanji.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.project.fumanji.entity.Dice;

@Configuration
public class GameConfig {

    @Bean
    public Dice dice() {
        return new Dice();
    }
}