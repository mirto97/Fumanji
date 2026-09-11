// tolti i runnable è diventata obsoleta




// import java.util.List;
// import java.util.Scanner;

// import entity.Board;
// import entity.Dice;
// import entity.Game;
// import entity.Player;
// import entity.squares.BivioSquare;
// import entity.squares.CheckpointSquare;
// import entity.squares.ObligationSquare;

// public class GameDemo3 {

//     public static void main(String[] args) {
//         Scanner scanner = new Scanner(System.in);

//         Board board = new Board(30);

//         // Bivio in 5: percorso principale (6,7,8-checkpoint,9) o percorso alternativo (10,11-obbligo,12)
//         board.getSquares().set(5, new BivioSquare(5, List.of(6, 10), List.of(4)));

//         // Percorso principale: 3 caselle proprie, poi redirect esplicito al ricongiungimento (20)
//         board.getSquares().set(8, new CheckpointSquare(8));
//         board.getSquares().set(9, new BivioSquare(9, List.of(20), List.of(8)));

//         // Percorso alternativo: 3 caselle proprie (numeri liberi, il principale non ci passa mai), poi redirect a 20
//         board.getSquares().set(11, new ObligationSquare(11));
//         board.getSquares().set(12, new BivioSquare(12, List.of(20), List.of(11)));

//         // Ricongiungimento: riceve da entrambi i percorsi, poi prosegue normale verso l'arrivo
//         board.getSquares().set(20, new BivioSquare(20, List.of(21), List.of(9, 12)));
        
//         Player player1 = new Player("Gigi");
//         Player player2 = new Player("Dodo");
//         List<Player> players = List.of(player1, player2);

//         Game game = new Game(board, players, new Dice());

//         int currentPlayerIndex = 0;
//         int turnCount = 0;
//         int maxTurns = 200;

//         while (game.getWinner() == null && turnCount < maxTurns) {

//             if (currentPlayerIndex == 0) {
//                 turnCount++;
//             }

//             Player currentPlayer = players.get(currentPlayerIndex);
//             game.rollAndMove(currentPlayer);

//             while (game.isAwaitingBivioChoice()) {
//                 resolveBivioChoice(game, currentPlayer, scanner);
//             }

//             System.out.println(currentPlayer.getName()
//                     + " -> position " + currentPlayer.getPosition()
//                     + " (dado: " + game.getLastRoll() + ")"
//                     + " - turno " + turnCount);

//             if (game.getWinner() == null) {
//                 currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
//             }
//         }

//         scanner.close();

//         if (game.getWinner() != null) {
//             System.out.println("Winner: " + game.getWinner().getName());
//         } else {
//             System.out.println("No winner after " + turnCount + " turns");
//         }
//     }

//     private static void resolveBivioChoice(Game game, Player player, Scanner scanner) {
//         List<Integer> options = game.getBivioOptions();
//         System.out.println(player.getName() + " e' a un bivio! Opzioni: " + options);

//         Integer chosen = null;
//         while (chosen == null) {
//             System.out.print("Scegli una posizione tra " + options + ": ");
//             String line = scanner.nextLine().trim();
//             try {
//                 int candidate = Integer.parseInt(line);
//                 if (options.contains(candidate)) {
//                     chosen = candidate;
//                 } else {
//                     System.out.println("Scelta non valida, riprova.");
//                 }
//             } catch (NumberFormatException e) {
//                 System.out.println("Inserisci un numero valido.");
//             }
//         }

//         game.chooseBivioPath(player, chosen);
//     }
// }