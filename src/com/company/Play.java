package com.company;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import com.company.utils.WaitForEnterKey;

public class Play {

    private static final Player[] players = {
            new Player(1, new Board(10, 10)),
            new Player(2, new Board(10, 10)),
    };

    private static final String[][] ships = {
            {"Aircraft Carrier", "5"},
            {"Battleship", "4"},
            {"Submarine", "3"},
            {"Cruiser", "3"},
            {"Destroyer", "2"}
    };

    private static Player winner = null;

    public static void main(String[] args) {
        gameOn();
    }

    static void gameOn() {
        System.out.println("Welcome to Battleship!\n\nInput two coordinates in {A-Z}{n} format separated by a single space to place the ships!\nex: A7 A10\n");
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            fillBoard(reader);
            System.out.println("The game starts!\n");
            fireAway(reader);
            System.out.printf("\nYou (Player %s) sank the last ship. You won. Congratulations!\n", winner.getNumber());
            System.out.println("\n Game Over");
        } catch (IOException ioe) {
            System.out.println(ioe.getMessage());
        }
    }

   private static void fillBoard(BufferedReader reader) throws IOException {
        for (Player player : players) {
            Board board = player.getBoard();
            System.out.printf("Player %s, place your ships on the game field\n%n", player.getNumber());
            board.print();
            for (int i = 0; i < ships.length; ) {
                String shipName = ships[i][0];
                int requiredCells = Integer.parseInt(ships[i][1]);
                Ship ship = new Ship(shipName);
                System.out.printf("\nEnter the coordinates of the %s (%d cells):%n", shipName, requiredCells);
                String input = reader.readLine();
                try {
                    board.takePosition(input, requiredCells, ship);
                    board.fillPlaceholders(ship);
                    board.addShip(ship);
                    board.print();
                    i++;
                } catch (RuntimeException re) {
                    System.out.println(re.getMessage());
                    board.removePlaceholders();
                }
            }
            board.foggyBoard = new Board(10, 10);
            WaitForEnterKey.wait(reader);
        }
    }

    static void fireAway(BufferedReader reader) throws IOException {
        int i = 0;
        while (winner == null) {
            Player current = players[i];
            Player other = players[i == 0 ? 1 : 0];

            other.getBoard().foggyBoard.print();
            System.out.println("---------------------");
            current.getBoard().print();
            System.out.printf("\nPlayer %s, it's your turn.  Take a shot!\n%n", current.getNumber());

            String input = reader.readLine();
            try {
                other.getBoard().takeShot(input);
                if (other.getBoard().sunkShips == ships.length){
                    winner = current;
                    break;
                }
            } catch (RuntimeException re) {
                System.out.println(re.getMessage());
            }
            WaitForEnterKey.wait(reader);
            i = i == 0 ? 1 : 0;
        }
    }

}