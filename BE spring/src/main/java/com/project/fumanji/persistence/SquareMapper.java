package com.project.fumanji.persistence;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.SubclassMapping;

import com.project.fumanji.entity.Square;
import com.project.fumanji.entity.squares.BivioSquare;
import com.project.fumanji.entity.squares.CheckpointSquare;
import com.project.fumanji.entity.squares.DrinkSquare;
import com.project.fumanji.entity.squares.MoveBackSquare;
import com.project.fumanji.entity.squares.MoveForwardSquare;
import com.project.fumanji.entity.squares.ObligationSquare;
import com.project.fumanji.entity.squares.PortalSquare;
import com.project.fumanji.entity.squares.RollAgainSquare;
import com.project.fumanji.entity.squares.SnackSquare;
import com.project.fumanji.entity.squares.StartOverSquare;
import com.project.fumanji.persistence.squares.BivioSquareEntity;
import com.project.fumanji.persistence.squares.CheckpointSquareEntity;
import com.project.fumanji.persistence.squares.DrinkSquareEntity;
import com.project.fumanji.persistence.squares.MoveBackSquareEntity;
import com.project.fumanji.persistence.squares.MoveForwardSquareEntity;
import com.project.fumanji.persistence.squares.ObligationSquareEntity;
import com.project.fumanji.persistence.squares.PortalSquareEntity;
import com.project.fumanji.persistence.squares.RollAgainSquareEntity;
import com.project.fumanji.persistence.squares.SnackSquareEntity;
import com.project.fumanji.persistence.squares.StartOverSquareEntity;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SquareMapper {

    // TODO commenta

    @SubclassMapping(source = StartOverSquareEntity.class, target = StartOverSquare.class)
    @SubclassMapping(source = MoveBackSquareEntity.class, target = MoveBackSquare.class)
    @SubclassMapping(source = MoveForwardSquareEntity.class, target = MoveForwardSquare.class)
    @SubclassMapping(source = RollAgainSquareEntity.class, target = RollAgainSquare.class)
    @SubclassMapping(source = SnackSquareEntity.class, target = SnackSquare.class)
    @SubclassMapping(source = DrinkSquareEntity.class, target = DrinkSquare.class)
    @SubclassMapping(source = ObligationSquareEntity.class, target = ObligationSquare.class)
    @SubclassMapping(source = CheckpointSquareEntity.class, target = CheckpointSquare.class)
    @SubclassMapping(source = PortalSquareEntity.class, target = PortalSquare.class)
    @SubclassMapping(source = BivioSquareEntity.class, target = BivioSquare.class)
    Square toDomain(SquareEntity entity);
}