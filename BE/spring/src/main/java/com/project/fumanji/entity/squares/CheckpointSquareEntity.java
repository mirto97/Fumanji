package com.project.fumanji.entity.squares;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;

import java.util.ArrayList;
import java.util.List;

import com.project.fumanji.entity.SquareEntity;

@Entity
public class CheckpointSquareEntity extends SquareEntity {

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "checkpoint_square_previous", joinColumns = @JoinColumn(name = "square_id"))
    @Column(name = "previous_position")
    private List<Integer> previousPositions = new ArrayList<>();

    protected CheckpointSquareEntity() {
    }

    public CheckpointSquareEntity(int position) {
        this(position, List.of());
    }

    public CheckpointSquareEntity(int position, List<Integer> previousPositions) {
        super(position);
        this.previousPositions = new ArrayList<>(previousPositions);
    }

    public List<Integer> getPreviousPositions() {
        return previousPositions;
    }
}