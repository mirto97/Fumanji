package com.project.fumanji.persistence.squares;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;

import com.project.fumanji.persistence.SquareEntity;

@Entity
public class PortalSquareEntity extends SquareEntity {

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "portal_square_link", joinColumns = @JoinColumn(name = "square_id"))
    @Column(name = "linked_position")
    private List<Integer> linkedPositions = new ArrayList<>();

    protected PortalSquareEntity() {
    }

    public PortalSquareEntity(int position, List<Integer> linkedPositions) {
        super(position);
        this.linkedPositions = new ArrayList<>(linkedPositions);
    }

    public List<Integer> getLinkedPositions() {
        return linkedPositions;
    }
}