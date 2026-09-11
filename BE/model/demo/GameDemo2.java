package demo;

import java.util.List;

import entity.Board;
import entity.Dice;
import entity.Game;
import entity.Player;
import entity.squares.DrinkSquare;
import entity.squares.MoveBackSquare;
import entity.squares.MoveForwardSquare;
import entity.squares.ObligationSquare;
import entity.squares.RollAgainSquare;
import entity.squares.SnackSquare;
import entity.squares.StartOverSquare;

public class GameDemo2 {

    public static void main(String[] args) {

        Board board = new Board(25);

        board.getSquares().set(2, new RollAgainSquare(2));
        board.getSquares().set(3, new SnackSquare(3, 3));
        board.getSquares().set(6, new DrinkSquare(6, 2));
        board.getSquares().set(9, new MoveForwardSquare(9, 4));   // atterra su 13 -> concatenazione
        board.getSquares().set(13, new ObligationSquare(13));
        board.getSquares().set(16, new MoveBackSquare(16, 3));    // atterra su 13 -> concatenazione
        board.getSquares().set(20, new StartOverSquare(20));
        board.getSquares().set(23, new SnackSquare(23, -5));      // testa il clamp a 0

        Player player1 = new Player("Gigi");
        Player player2 = new Player("Dodo");

        Dice dice = new Dice();
        Game game = new Game(board, List.of(player1, player2), dice);

        int maxTurns = 1000;
        int turnCount = 0;

        while (game.getWinner() == null && turnCount < maxTurns) {
            game.playTurn();
            turnCount++;

            for (Player player : game.getPlayers()) {
                String rollInfo = player == game.getLastPlayerToMove()
                        ? " (dado: " + game.getLastRoll() + ")"
                        : "";
                System.out.println(player.getName()
                        + " -> position " + player.getPosition() + rollInfo + " - "
                        + " snack: " + player.getSnack()
                        + " drink: " + player.getDrink()
                        + " - " + turnCount);
            }
        }

        if (game.getWinner() != null) {
            System.out.println("Winner: " + game.getWinner().getName());
        } else {
            System.out.println("No winner after " + turnCount + " turns");
        }
    }
}