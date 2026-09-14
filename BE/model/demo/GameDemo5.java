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

public class GameDemo5 {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        Board board = Board.builder(70)
                .directionChoiceEnabled(true)
                .mandatoryCheckpointEnabled(true)
                // tronco iniziale
                .square(3, new RollAgainSquare(3))
                .square(5, new SnackSquare(5, 3))
                .square(7, new DrinkSquare(7, 2))
                .square(9, new CheckpointSquare(9))        // checkpoint 1
                .square(11, new StartOverSquare(11))
                .square(13, new ObligationSquare(13))
                // primo bivio: ramo principale (15-19) o alternativo (30-34)
                .square(14, new BivioSquare(14, List.of(15, 30), List.of(13)))
                // ramo principale
                .square(15, new MoveForwardSquare(15, 3))  // atterra su 18 -> concatenazione
                .square(18, new CheckpointSquare(18))      // checkpoint 2
                .square(19, new BivioSquare(19, List.of(45), List.of(18)))
                // ramo alternativo (numeri liberi, il principale non ci passa mai)
                .square(30, new MoveBackSquare(30, 5))     // atterra su 25
                .square(32, new PortalSquare(32, List.of(60, 63)))
                .square(34, new BivioSquare(34, List.of(45), List.of(33)))
                // ricongiungimento
                .square(45, new BivioSquare(45, List.of(46), List.of(19, 34)))
                // tronco finale
                .square(48, new PortalSquare(48, List.of(53)))
                .square(53, new SnackSquare(53, -10))      // clamp a 0
                .square(55, new DrinkSquare(55, -10))      // clamp a 0
                .square(57, new MoveForwardSquare(57, 4))  // atterra su 61
                .square(61, new MoveBackSquare(61, 4))     // torna su 57 -> guardrail anti-loop
                .build();

        Player player1 = new Player("Gigi");
        Player player2 = new Player("Dodo");
        List<Player> players = List.of(player1, player2);

        Game game = new Game(board, players, new Dice());

        System.out.println("=== Demo rapida: addSnack/addDrink richiamabili in qualsiasi momento ===");
        System.out.println(player1.getName() + " iniziali -> snack: " + player1.getSnack()
                + " drink: " + player1.getDrink());
        game.addSnack(player1, 2);
        game.addDrink(player1, 1);
        System.out.println(player1.getName() + " dopo addSnack(+2)/addDrink(+1) -> snack: " + player1.getSnack()
                + " drink: " + player1.getDrink());
        game.addDrink(player1, -10);
        System.out.println(player1.getName() + " dopo addDrink(-10), clamp a 0 -> snack: " + player1.getSnack()
                + " drink: " + player1.getDrink());
        System.out.println();

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