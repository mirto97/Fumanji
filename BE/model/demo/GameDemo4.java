package demo;

import java.util.List;
import java.util.Scanner;
import entity.Board;
import entity.Dice;
import entity.Game;
import entity.Player;
import entity.Square;
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

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        Board board = new Board(55);
        board.setDirectionChoiceEnabled(true);
        board.setMandatoryCheckpointEnabled(true);

        // tronco iniziale
        board.getSquares().set(3, new RollAgainSquare(3));
        board.getSquares().set(5, new SnackSquare(5, 3));
        board.getSquares().set(7, new DrinkSquare(7, 2));
        board.getSquares().set(9, new CheckpointSquare(9));
        board.getSquares().set(11, new StartOverSquare(11)); // zona molto trafficata, dopo il checkpoint

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
        int maxTurns = 500;

        while (game.getWinner() == null && turnCount < maxTurns) {
            if (currentPlayerIndex == 0) {
                turnCount++;
            }

            Player currentPlayer = players.get(currentPlayerIndex);

            System.out.println();
            System.out.println("=== Turno " + turnCount + " - " + currentPlayer.getName() + " ===");
            System.out.println("Posizione attuale: " + currentPlayer.getPosition()
                    + " (" + describeSquare(board, currentPlayer.getPosition()) + ")"
                    + " - snack: " + currentPlayer.getSnack()
                    + " drink: " + currentPlayer.getDrink()
                    + " checkpoint: " + currentPlayer.getCheckpoint());

            int startPosition = currentPlayer.getPosition();
            int rollValue = game.roll(currentPlayer);
            System.out.println("Tiro: " + rollValue);

            boolean forward = askDirection(scanner, currentPlayer);

            game.clearMovementTrace();
            game.moveByChosenDirection(currentPlayer, forward);

            while (game.isAwaitingChoice()) {
                resolveChoiceInteractively(game, currentPlayer, scanner);
            }

            printMoveSummary(game, board, currentPlayer, startPosition, forward);

            if (game.getWinner() == null) {
                currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
            }
        }

        scanner.close();

        System.out.println();
        if (game.getWinner() != null) {
            System.out.println("Winner: " + game.getWinner().getName());
        } else {
            System.out.println("No winner after " + turnCount + " turns");
        }
    }

    private static boolean askDirection(Scanner scanner, Player player) {
        while (true) {
            System.out.println(player.getName() + ", vuoi andare avanti (a) o indietro (i)? ");
            String line = scanner.nextLine().trim().toLowerCase();
            if (line.equals("a") || line.equals("avanti")) {
                return true;
            }
            if (line.equals("i") || line.equals("indietro")) {
                return false;
            }
            System.out.println("Risposta non valida, scrivi 'a' o 'i'.");
        }
    }

    private static void resolveChoiceInteractively(Game game, Player player, Scanner scanner) {
        List<Integer> options = game.getChoiceOptions();
        System.out.println(player.getName() + " deve scegliere una destinazione! Opzioni: " + options);

        Integer chosen = null;
        while (chosen == null) {
            System.out.println("Scegli una posizione tra " + options + ": ");
            String line = scanner.nextLine().trim();
            try {
                int candidate = Integer.parseInt(line);
                if (options.contains(candidate)) {
                    chosen = candidate;
                } else {
                    System.out.println("Scelta non valida, riprova.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Inserisci un numero valido.");
            }
        }

        game.chooseDestination(player, chosen);
    }

    private static void printMoveSummary(Game game, Board board, Player player, int startPosition, boolean forward) {
        List<Integer> trace = game.getMovementTrace();

        System.out.print("Direzione: " + (forward ? "avanti" : "indietro") + " | Percorso: " + startPosition);
        for (int position : trace) {
            System.out.print(" -> " + position + " (" + describeSquare(board, position) + ")");
        }
        System.out.println();

        System.out.println("Posizione finale: " + player.getPosition()
                + " - snack: " + player.getSnack()
                + " drink: " + player.getDrink()
                + " checkpoint: " + player.getCheckpoint());
    }

    private static String describeSquare(Board board, int position) {
        if (position == board.getFinishPosition()) {
            return "ARRIVO";
        }
        Square square = board.getSquares().get(position);
        return square.getClass().getSimpleName();
    }
}
