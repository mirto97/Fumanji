
import java.util.List;

import entity.Board;
import entity.Dice;
import entity.Game;
import entity.Player;
import entity.squares.BivioSquare;
import entity.squares.CheckpointSquare;
import entity.squares.DrinkSquare;
import entity.squares.MoveBackSquare;
import entity.squares.MoveForwardSquare;
import entity.squares.ObligationSquare;
import entity.squares.PortalSquare;
import entity.squares.RollAgainSquare;
import entity.squares.SnackSquare;
import entity.squares.StartOverSquare;

public class GameDemo4 {

    // TODO studiare e ricommentare

    private static boolean pickSecondOption = false;

    public static void main(String[] args) {
        Board board = new Board(55);
        board.setDirectionChoiceEnabled(true);
        board.setMandatoryCheckpointEnabled(true);

        // tronco iniziale
        board.getSquares().set(3, new RollAgainSquare(3));
        board.getSquares().set(5, new SnackSquare(5, 3));
        board.getSquares().set(7, new DrinkSquare(7, 2));
        board.getSquares().set(9, new CheckpointSquare(9));
        board.getSquares().set(11, new StartOverSquare(11)); // spostato qui: zona molto trafficata, dopo il checkpoint

        // bivio: ramo principale (15-19) o alternativo (25-29)
        board.getSquares().set(14, new BivioSquare(14, List.of(15, 25), List.of(13)));

        // ramo principale
        board.getSquares().set(15, new MoveForwardSquare(15, 3)); // atterra su 18 -> concatenazione
        board.getSquares().set(18, new ObligationSquare(18));
        board.getSquares().set(19, new BivioSquare(19, List.of(35), List.of(18))); // redirect al ricongiungimento

        // ramo alternativo (numeri liberi, il principale non ci passa mai)
        board.getSquares().set(25, new MoveBackSquare(25, 4)); // atterra su 21
        board.getSquares().set(27, new PortalSquare(27, List.of(47, 50))); // portale con scelta
        board.getSquares().set(29, new BivioSquare(29, List.of(35), List.of(28))); // redirect al ricongiungimento

        // ricongiungimento
        board.getSquares().set(35, new BivioSquare(35, List.of(36), List.of(19, 29)));

        // tronco finale
        board.getSquares().set(38, new PortalSquare(38, List.of(43))); // portale singolo, teleport immediato
        board.getSquares().set(43, new SnackSquare(43, -10)); // testa il clamp a 0
        board.getSquares().set(46, new DrinkSquare(46, -10)); // testa il clamp a 0

        Player player1 = new Player("Gigi");
        Player player2 = new Player("Dodo");
        List<Player> players = List.of(player1, player2);

        Game game = new Game(board, players, new Dice());

        int currentPlayerIndex = 0;
        int turnCount = 0;
        int maxTurns = 300;
        int moveCounter = 0;

        while (game.getWinner() == null && turnCount < maxTurns) {
            if (currentPlayerIndex == 0) {
                turnCount++;
            }

            Player currentPlayer = players.get(currentPlayerIndex);
            moveCounter++;

            int rollValue = game.roll(currentPlayer);
            boolean goBackward = moveCounter % 5 == 0; // ogni 5 mosse testa la direzione indietro
            game.moveByChosenDirection(currentPlayer, !goBackward);

            while (game.isAwaitingChoice()) {
                resolveChoiceAutomatically(game, currentPlayer);
            }

            System.out.println(currentPlayer.getName()
                    + " -> position " + currentPlayer.getPosition()
                    + " (dado: " + rollValue + (goBackward ? ", indietro" : "") + ")"
                    + " - snack: " + currentPlayer.getSnack()
                    + " drink: " + currentPlayer.getDrink()
                    + " checkpoint: " + currentPlayer.getCheckpoint()
                    + " - turno " + turnCount);

            if (game.getWinner() == null) {
                currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
            }
        }

        if (game.getWinner() != null) {
            System.out.println("Winner: " + game.getWinner().getName());
        } else {
            System.out.println("No winner after " + turnCount + " turns");
        }
    }

    private static void resolveChoiceAutomatically(Game game, Player player) {
        List<Integer> options = game.getChoiceOptions();
        int index = pickSecondOption && options.size() > 1 ? 1 : 0;
        int chosen = options.get(index);
        pickSecondOption = !pickSecondOption; // alterna per testare entrambe le opzioni nel corso della partita

        System.out.println(player.getName() + " deve scegliere tra " + options + " -> sceglie " + chosen);
        game.chooseDestination(player, chosen);
    }
}
