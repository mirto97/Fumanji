package com.project.fumanji.entity;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.SubclassMapping;

import com.project.fumanji.entity.squares.BivioSquareEntity;
import com.project.fumanji.entity.squares.CheckpointSquareEntity;
import com.project.fumanji.entity.squares.DrinkSquareEntity;
import com.project.fumanji.entity.squares.MoveBackSquareEntity;
import com.project.fumanji.entity.squares.MoveForwardSquareEntity;
import com.project.fumanji.entity.squares.ObligationSquareEntity;
import com.project.fumanji.entity.squares.PortalSquareEntity;
import com.project.fumanji.entity.squares.RollAgainSquareEntity;
import com.project.fumanji.entity.squares.SnackSquareEntity;
import com.project.fumanji.entity.squares.StartOverSquareEntity;
import com.project.fumanji.model.Square;
import com.project.fumanji.model.squares.BivioSquare;
import com.project.fumanji.model.squares.CheckpointSquare;
import com.project.fumanji.model.squares.DrinkSquare;
import com.project.fumanji.model.squares.MoveBackSquare;
import com.project.fumanji.model.squares.MoveForwardSquare;
import com.project.fumanji.model.squares.ObligationSquare;
import com.project.fumanji.model.squares.PortalSquare;
import com.project.fumanji.model.squares.RollAgainSquare;
import com.project.fumanji.model.squares.SnackSquare;
import com.project.fumanji.model.squares.StartOverSquare;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SquareMapper {

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