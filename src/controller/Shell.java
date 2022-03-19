package controller;

import model.board.PawnBoard;
import model.board.Board;
import model.player.Player;
import model.player.Color;
import model.exception.IllegalMoveException;
import view.GUIView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Pattern;

/**
 * Main class handling the input and executing the (PawnChess)game.
 */
public final class Shell {

    // Prompt of the UserInterface.
    private static final String PROMPT = "pc> ";

    /**
     * Main method starting up the Controller.Shell input.
     *
     * @param args an array of command-line arguments for the application.
     * @throws IOException if the shell-method fails due to input errors.
     */
    public static void main(String[] args) throws IOException {
        Controller controller = new Controller();
        BufferedReader stdin
                = new BufferedReader(new InputStreamReader(System.in));
        shellExecute(stdin);
    }

    /**
     * Method monitoring the input by the user and executing methods based on
     * it.
     *
     * @param stdin the input read.
     * @throws IOException if an error occurs with the commandline input.
     */
    private static void shellExecute(BufferedReader stdin) throws IOException {
        PawnBoard playBoard = null;
        boolean quit = false;
        int difficulty = 3;
        int starter = 0;
        Color humanColor = Color.WHITE;

        // Input loop handling the input and responses.
        while (!quit) {
            System.out.print(PROMPT);
            String input = stdin.readLine();
            if (input == null || input.equals("")) {
                error("Empty command");
            } else {
                String[] parts = input.trim().split("\\s+");

                // Switch handling inputs.
                switch (parts[0].toLowerCase().charAt(0)) {
                    case 'q' -> {
                        quit = true;
                        stdin.close();
                    }
                    case 'n' -> {
                        playBoard = commandNew(starter, difficulty, humanColor);
                        System.out.println("New game started. You are "
                                + humanColor.name().toLowerCase() + ".");
                    }
                    case 'p' -> commandPrint(playBoard);
                    case 'l' -> commandLVLChange(parts, playBoard);
                    case 'm' -> {
                        playBoard = commandMove(parts, playBoard);
                    }
                    case 's' -> {
                        playBoard = commandSwitch(playBoard, difficulty);
                    }
                    case 'h' -> commandHelp();
                    default -> error("Unknown command.");
                }
            }
        }
    }

    /**
     * Executed every time an error statement needs to be shown.
     * Prints the statement with a set prefix on the console.
     *
     * @param errorMsg the errormessage to be shown on the console.
     */
    private static void error(String errorMsg) {
        System.out.println("Error! " + errorMsg);
    }

    /**
     * Validating/checking if the input string has the least necessary
     * amount of parameters/keys.
     *
     * @param inputs the input of the user separated by blank spaces.
     * @param min necessary amount of keys.
     * @return true if at least min amount, false otherwise.
     */
    private static boolean validAmount(String[] inputs, int min) {
        return inputs.length >= min;
    }

    /**
     * Creating a new PawnBoard/Game with the current difficulty, starter
     * and color, which are at the first start set as level 3, human and white.
     *
     * @param starter the player eligible to make the first turn.
     * @param diff the level of difficulty/"power" the bot has.
     * @param human the color of the human player.
     * @return the created PawnBoard.
     */
    private static PawnBoard commandNew(int starter, int diff, Color human) {
        return new PawnBoard(starter, diff, human);
    }

    /**
     * Changing the difficulty/level of the bot "on the fly" changing his
     * depth of prediction.
     * @param inputs the input of the user containing the new level.
     * @param board the game, which difficulty shall be changed.
     */
    private static void commandLVLChange(String[] inputs, PawnBoard board) {
        if (board == null) {
            error("Start a game first!");
            return;
        }

        // Right amount of words in input and an integer entered?
        if (validAmount(inputs, 2)) {
            String checkInt = "-?\\d*";
            Pattern valid = Pattern.compile(checkInt);
            if (!valid.matcher(inputs[1]).matches()) {
                error("Enter a number as difficulty!");
                return;
            }
            int lvl = Integer.parseInt(inputs[1]);
            if (lvl > 8) {
                error("AI not tuned yet! "
                        + "Choose a lower difficulty");
            } else {
                board.setLevel(lvl);
            }
        } else {
            error("No level to be set entered!");
        }
    }

    /**
     * Method ordering the current PawnBoard to perform a certain move and
     * afterwards to perform a move by the machine, if the previous move
     * worked and didn't end the game, nor force the computer to skip his turn.
     *
     * @param inputs the input by the user containing the move as coordinates.
     * @param board the current game played on, where the move shall be done.
     * @return null, if no no game taking place, the old PawnBoard if the
     * move could not be performed along with an error message, the board
     * after the human move, if the computer has to skip or the game is over,
     * or the board after the computers turn.
     */
    private static PawnBoard commandMove(String[] inputs, PawnBoard board) {
        if (board == null) {
            error("Start a game first!");
            return null;
        } else if (validAmount(inputs, 5)) {
            PawnBoard newBoard;
            try {
                newBoard = directMoveHuman(inputs, board);
            } catch (IllegalMoveException | IllegalArgumentException e) {
                error(e.getMessage());
                return board;
            }

            // Game over after humans turn?
            if (newBoard.isGameOver()) {
                if (newBoard.getWinner() == null) {
                    System.out.println("Nobody wins. Draw.");
                } else {
                    System.out.println("Congratulations! You won.");
                }
            }

            // Machine cant move after humans turn?
            if (newBoard.getNextPlayer().getColor()
                    != newBoard.getHumanColor()) {
                return newBoard;
            }
            return directMoveComputer(newBoard);
        } else {
            error("Coordinates not given!");
            return board;
        }
    }

    /**
     * Helper method called by commandMove if a move shall be performed by
     * the human parsing the coordinates and executing them, after checking
     * if all given coordinates are numbers. If that isn't the case, it will
     * direct a move causing a thrown Model.Exception with coordinates out of the
     * board.
     *
     * @param inputs the input of the user containing the coordinates.
     * @param board the board to perform the move on.
     * @return the new board after performing the move.
     */
    private static PawnBoard directMoveHuman(String[] inputs, PawnBoard board) {

        // Checking for Regular expression that matches digits.
        if ((inputs[1].matches("\\d+")) && (inputs[2].matches("\\d+"))
                && (inputs[3].matches("\\d+")) && (inputs[4].matches("\\d+"))) {
            int fromCol = Integer.parseInt(inputs[1]) - 1;
            int fromRow = Board.SIZE - Integer.parseInt(inputs[2]);
            int toCol = Integer.parseInt(inputs[3]) - 1;
            int toRow = Board.SIZE - Integer.parseInt(inputs[4]);
            return board.move(fromCol, fromRow, toCol, toRow);
        } else {
            return board.move(-1, -1, -1, -1);
        }
    }

    /**
     * Helper method called by commandMove to (try to) perform a move by the
     * computer and checking afterwards if the game is over after the move.
     *
     * @param board the current board the computer shall perform the best move.
     * @return the old board if the move couldn't be performed, the board
     * after the move otherwise.
     */
    private static PawnBoard directMoveComputer(PawnBoard board) {

        // Machine working?
        try {
            board = board.machineMove();
        } catch (IllegalMoveException e) {
            error(e.getMessage());
            return board;
        }

        // Game over after machine?
        if (board.isGameOver()) {
            if (board.getWinner() == null) {
                System.out.println("Nobody wins. Draw.");
            } else {
                System.out.println("Sorry! Machine wins.");
            }
        }
        return board;
    }

    /**
     * Method printing out the current game as a String on the input/console.
     * @param board the game/situation to be displayed.
     */
    private static void commandPrint(PawnBoard board) {
        if (board == null) {
            error("No match to print!");
        } else {
            System.out.println(board.toString());
        }
    }

    /**
     * Method printing out a list of available Commands.
     */
    private static void commandHelp() {
        System.out.println("Available commands:");
        System.out.println("-Create a new game: NEW");
        System.out.println("-Change the difficulty: LEVEL <lvl>");
        System.out.println("-Move a pawn: MOVE <fromCol> <fromRow> <toCol> "
                + "toRow");
        System.out.println("-Printing the current board: PRINT");
        System.out.println("-Switch colors and start new: SWITCH");
        System.out.println("Exit the game: QUIT");
    }

    /**
     * Method starting a new game with switched roles/colors and therefore
     * also switching the starter while maintaining the same level of
     * difficulty.
     *
     * @param board the current game taking place.
     * @param diff the current of level of difficulty played on.
     * @return null if no game taking place to flip, a PawnBoard with
     * reversed colors and -if the computer has the color white- a performed
     * opening move by the computer.
     */
    public static PawnBoard commandSwitch(PawnBoard board, int diff) {
        if (board == null) {
            error("Start a game first!");
            return null;
        } else {
            Color oldColor = board.getHumanColor();
            Color newColor;
            if (oldColor == Color.WHITE) {
                newColor = Color.BLACK;
            } else {
                newColor = Color.WHITE;
            }
            Player oldStarter = board.getOpeningPlayer();
            int newStarter;
            if (oldStarter.getColor() == oldColor) {
                newStarter = 1;
            } else {
                newStarter = 0;
            }
            PawnBoard toReturn = new PawnBoard(newStarter, diff, newColor);
            if (newStarter == 1) {
                toReturn = toReturn.machineMove();
            }
            return toReturn;
        }
    }

    /**
     * Utility class constructor preventing instantiation.
     */
    private Shell() {
        throw new UnsupportedOperationException("Illegal calling of "
                + "constructor!");
    }
}