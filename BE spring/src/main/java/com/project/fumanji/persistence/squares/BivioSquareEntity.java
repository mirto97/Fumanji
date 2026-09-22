package com.project.fumanji.persistence.squares;

import java.util.ArrayList;
import java.util.List;

import com.project.fumanji.persistence.SquareEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class BivioSquareEntity extends SquareEntity {

    @ElementCollection
    @CollectionTable(name = "bivio_square_next", joinColumns = @JoinColumn(name = "square_id"))
    @Column(name = "next_position")
    private List<Integer> nextPositions = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "bivio_square_previous", joinColumns = @JoinColumn(name = "square_id"))
    @Column(name = "previous_position")
    private List<Integer> previousPositions = new ArrayList<>();


    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "effect_square_id")
    private SquareEntity effect;

    protected BivioSquareEntity() {
    }

    public BivioSquareEntity(int position, List<Integer> nextPositions, List<Integer> previousPositions, SquareEntity effect) {
        super(position);
        this.nextPositions = new ArrayList<>(nextPositions);
        this.previousPositions = new ArrayList<>(previousPositions);
        this.effect = effect;
    }

    public SquareEntity getEffect() {
        return effect;
    }

    public List<Integer> getNextPositions() {
        return nextPositions;
    }

    public List<Integer> getPreviousPositions() {
        return previousPositions;
    }
}