import java.util.List;

import entity.Board;
import entity.Dice;
import entity.Game;
import entity.Player;

// prima demo con tabellone da 10 spazi e 2 giocatori. funzionante
public class GameDemo1 {
    public static void main(String[] args) {

        Board board = new Board(10);

        Player player1 = new Player("Gigi");
        Player player2 = new Player("Dodo");

        Dice dice = new Dice();

        Game game = new Game(board, List.of(player1, player2), dice);

        // turni massimi perchè boh non si sa mai
        int maxTurns = 1000;
        int turnCount = 0;

        while (game.getWinner() == null && turnCount < maxTurns) {
            game.playTurn();
            turnCount++;

            for (Player player : game.getPlayers()) {
                System.out.println(player.getName() + " -> position " + player.getPosition() + " - " + turnCount);
            }
        }
        

        if (game.getWinner() != null) {
            System.out.println("Winner: " + game.getWinner().getName());
        } else {
            System.out.println("No winner after " + turnCount + " turns");
        }
    }
}
