package com.project.fumanji.dto;

public record PlayerStateResponse(
    String name, 
    int position, 
    int snack, 
    int drink, 
    int checkpoint) {
    
}
