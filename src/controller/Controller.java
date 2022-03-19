package controller;

import model.board.Board;
import model.board.PawnBoard;
import model.exception.IllegalMoveException;
import model.player.Color;
import model.player.Player;
import view.BoardTile;
import view.GUIView;

import javax.swing.SwingWorker;
import javax.swing.SwingUtilities;
import java.lang.reflect.InvocationTargetException;
import java.util.Stack;

/**
 * Class responsible for managing model and view and modifying them after
 * interactions.
 */
public class Controller {

    private GUIView view;
    private final Stack<Board> previousSituations;
    private BoardTile pawnHolding;
    private Board game;
    private Color humanColor;
    private int difficulty;
    private int starter;
    private SwingWorker<Board, Void> currentWorker;
    private BoardTile[][] viewBoard;
    private Player human;
    private Player computer;

    /**
     * Main method starting up the Visual interface for user-input.
     *
     * @param args an array of command-line arguments for the application.
     */
    public static void main(String[] args) {
        Controller controller = new Controller();
        controller.start();
    }

    /**
     * Class constructor setting up variables of the controller.
     */
    public Controller() {
        pawnHolding = null;
        previousSituations = new Stack<>();
        humanColor = Color.WHITE;
        starter = 0;
    }

    /**
     * Starts up the view for the user and tells it which listeners are to be
     * added.
     */
    private void start() {
        view = new GUIView();
        Handler handler = new Handler(this, view);
        SwingUtilities.invokeLater(() -> view.addListenerMenu(handler));
        SwingUtilities.invokeLater(() -> view.addListenerBoard(handler));
        SwingUtilities.invokeLater(() -> viewBoard = view.getBoard());
    }

    /**
     * Method called, after the player selected a tile with a pawn on it.
     * Depending on if already a pawn got selected before it reacts by either
     * now "picking" the pawn, or moving the pawn.
     *
     * @param selected the tile the player selected on the view.
     */
    public void pawnSelected(BoardTile selected) {
        if (game.isGameOver()) {
            causeErrorPopUp("Game already over!");
        } else if (pawnHolding == null) {
            pawnHolding = selected;

            // Deactivate all human pawns but the one selected.
            hPawnsSwitch(false);
            SwingUtilities.invokeLater(() -> selected.setEnabled(true));
            cPawnsSwitch(true);
            highlightSwitch(true);
        } else if (pawnHolding == selected) {
            highlightSwitch(false);
            cPawnsSwitch(false);
            hPawnsSwitch(true);
            pawnHolding = null;
        } else {
            allSwitch(false);
            performMove(selected);
            refreshPawnCounter();
        }
    }

    /**
     * Changing the level of difficulty in the model.
     *
     * @param diff the new level to be set.
     */
    public void changeDifficulty(Integer diff) {
        if (game != null && currentWorker == null) {
            game.setLevel(diff);
        }
        difficulty = diff;
    }

    /**
     * Method called, after a field with no pawn on it got selected by the
     * player on the view.
     * If a pawn got selected before, the pawn gets moved to the selected
     * tile (if allowed).
     * Otherwise a popup encourages the player to first pick a pawn to move.
     *
     * @param selected the tile selected by the player on the view.
     */
    public void emptyFieldSelected(BoardTile selected) {
        if (pawnHolding != null) {
            allSwitch(false);
            performMove(selected);
        } else {
            causeErrorPopUp("Select a pawn first!");
            cPawnsSwitch(false);
        }
    }

    /**
     * Method responsible for moving the currently selected pawn to a given
     * tile.
     * Checking first if the move is allowed/accepted by the model and then
     * after evaluating the new situation either ordering the model to
     * perform a move by the machine, or telling the view to list a fitting
     * PopUp.
     *
     * @param targetTile the tile targeted by the player.
     */
    private void performMove(BoardTile targetTile) {
        int[] fromCoords = getCoordinates(pawnHolding, viewBoard);
        int[] toCoords = getCoordinates(targetTile, viewBoard);
        if (fromCoords == null || toCoords == null) {
            causeErrorPopUp("Couldn't perform move!");
        } else {
            Board newBoard;

            // Game situation to remember for undo(s) if the move is legal.
            Board toStack = game.clone();
            try {
                newBoard = game.move(fromCoords[0], fromCoords[1], toCoords[0],
                        toCoords[1]);
            } catch (IllegalMoveException | IllegalArgumentException e) {
                causeErrorPopUp(e.getMessage());
                allSwitch(true);
                cPawnsSwitch(false);
                highlightSwitch(false);
                pawnHolding = null;
                return;
            }
            addToHistory(toStack);

            // Game over after humans turn or machine cant move after turn?
            if (newBoard.isGameOver()) {
                if (newBoard.getWinner() == null) {
                    causePopUp("Nobody wins. Draw.");
                } else {
                    causePopUp("Congratulations! You won.");
                }
                game = newBoard;
                directMoveView(pawnHolding,
                        viewBoard[toCoords[0]][toCoords[1]]);
                hPawnsSwitch(false);
            } else if (newBoard.getNextPlayer().getColor()
                    != newBoard.getHumanColor()) {
                game = newBoard;
                directMoveView(pawnHolding,
                        viewBoard[toCoords[0]][toCoords[1]]);
                causePopUp("Computer has to skip his turn!");
                allSwitch(true);
            } else {
                directMoveComputer();
            }
            highlightSwitch(false);
            pawnHolding = null;
        }
    }

    /**
     * Using the model to find out, which move the computer has to make in a
     * separate Thread via SwingWorker and reacting to the Board given by the
     * model.
     */
    private void directMoveComputer() {
        SwingWorker<Board, Void> worker = new SwingWorker<>() {

            /**
             * Lets the model calculate the best move for the computer and
             * reacts after getting a result.
             *
             * @return the board with the best move performed.
             * @throws InvocationTargetException if the target couldn't get
             *                                   invocation failed.
             * @throws InterruptedException if the Thread got interrupted.
             */
            @Override
            protected Board doInBackground() throws InvocationTargetException,
                    InterruptedException {
                Player human = game.getNextPlayer();

                // Machine(move) working?
                Board board;
                try {
                    board = game.machineMove();
                } catch (IllegalMoveException e) {
                    causeErrorPopUp(e.getMessage()
                            + "Start a new game please!");
                    return null;
                }

                // Game over after machine?
                if (board.isGameOver()) {
                    if (board.getWinner() == null) {
                        causePopUp("Nobody wins. Draw.");
                    } else {
                        causePopUp("Sorry! Machine wins.");
                    }
                    game = board;
                    updateView();
                    allSwitch(false);
                } else if (board.getNextPlayer() == human) {
                    game = board;
                    updateView();
                    SwingUtilities.invokeAndWait(view::skipTurnPopUp);
                    game = doInBackground();
                } else {
                    game = board;
                    updateView();
                    hPawnsSwitch(true);
                }
                refreshPawnCounter();
                return game;
            }

            /**
             * Changing the level in the model if it has been changed.
             */
            @Override
            protected void done() {
                game.setLevel(difficulty);
                cPawnsSwitch(false);
            }
        };
        killCurrentSwingWorker();
        swingWorkerStarting(worker);
        worker.execute();
    }

    /**
     * Instancing a new game/model and updating the view.
     */
    public void startNewGame() {
        if (difficulty == 0) {
            causeErrorPopUp("Select a level!");
        } else {
            allSwitch(false);
            previousSituations.clear();
            if (pawnHolding != null) {
                highlightSwitch(false);
                pawnHolding = null;
            }
            game = new PawnBoard(starter, difficulty, humanColor);
            if (starter == 1) {
                computer = game.getOpeningPlayer();
                human = game.getNextPlayer();
                game = game.machineMove();
            } else {
                computer = game.getNextPlayer();
                human = game.getOpeningPlayer();
            }
            updateView();
            hPawnsSwitch(true);
        }
    }

    /**
     * Checking the stack containing all previous situations and popping the
     * top element and updating the view to display the new situation.
     */
    public void undoMove() {
        if (!previousSituations.isEmpty()) {
            game = previousSituations.pop();
            game.setLevel(difficulty);
            updateView();
            hPawnsSwitch(true);
            cPawnsSwitch(false);
            if (pawnHolding != null) {
                highlightSwitch(false);
                pawnHolding = null;
            }
            if (previousSituations.isEmpty()) {
                enableUndo(false);
            }
        } else {
            causeErrorPopUp("No turns to undo!");
        }
    }

    /**
     * Starting a new game with the starting role and Colors reversed.
     */
    public void switchColors() {

        // 0 = user starts, 1 = computer starts.
        starter = (starter + 1) % 2;
        if (humanColor == Color.WHITE) {
            humanColor = Color.BLACK;
        } else {
            humanColor = Color.WHITE;
        }
        startNewGame();
    }

    /**
     * Calculating/Counting which coordinates a tile has in the board of the
     * view.
     *
     * @param tile the tile whose coordinates shall be returned.
     * @param board the arranging of all tiles.
     * @return array containing the x and y coordinate.
     */
    private int[] getCoordinates(BoardTile tile, BoardTile[][] board) {
        int[] toReturn = new int[2];
        for (int i = 0; i < board.length; i++) {
            BoardTile[] currentArray = board[i];
            for (int j = 0; j < currentArray.length; j++) {
                if (currentArray[j] == tile) {
                    toReturn[0] = i;
                    toReturn[1] = j;
                    return toReturn;
                }
            }
        }
        return null;
    }

    /**
     * Method to get/find out the char resembling the color of the human pawns.
     *
     * @return W, if the color of the player is white, B otherwise.
     */
    private char getHumanSymbol() {
        if (game.getHumanColor() == Color.WHITE) {
            return 'W';
        } else {
            return 'B';
        }
    }

    /**
     * Method adding a model to the stack containing all previous situations
     * to which the user can go back to via "undo".
     * If the model to be added is the first, it also calls another method
     * which enables the "undo" feature for the user.
     *
     * @param board the model/situation to be added to the stack.
     */
    private void addToHistory(Board board) {
        if (previousSituations.isEmpty()) {
            enableUndo(true);
        }
        previousSituations.push(board);
    }

    /**
     * Method converting the String given by the model via its toString()
     * method into a 2D char array to convert the model to the view easier.
     *
     * @return the 2D char Array.
     */
    private char[][] getBoard() {
        String print = game.toString();
        char[] printContent = print.toCharArray();
        char[][] board = new char[Board.SIZE][Board.SIZE];
        int counter = 0;
        for (int i = 0; i < board.length; i++) {
            for (char[] current : board) {
                current[i] = printContent[counter];
                counter++;
            }
        }
        return board;
    }

    /**
     * Method advising the view to update the displayed counter for the
     * amount of each colors pawns.
     */
    private void refreshPawnCounter() {
        if (human.getColor() == Color.WHITE) {
            SwingUtilities.invokeLater(() -> view.decreasePawnCounter(
                    game.getNumberOfTiles(human),
                    game.getNumberOfTiles(computer)));
        } else {
            SwingUtilities.invokeLater(() -> view.decreasePawnCounter(
                    game.getNumberOfTiles(computer),
                    game.getNumberOfTiles(human)));
        }
    }

    /**
     * Advising the view to update/replace the represented board with the given
     * one.
     */
    private void updateView() {
        SwingUtilities.invokeLater(() -> view.placePawns(getBoard()));
    }

    /**
     * Method advising the view to either allow to click or lock all human
     * pawns.
     *
     * @param isToEnable false if pawns shall be locked, true otherwise.
     */
    private void hPawnsSwitch(boolean isToEnable) {
        if (isToEnable) {
            SwingUtilities.invokeLater(
                    () -> view.enableHumanPawns(getHumanSymbol()));
        } else {
            SwingUtilities.invokeLater(
                    () -> view.disableHumanPawns(getHumanSymbol()));
        }
    }

    /**
     * Method advising the view to either allow to click or lock all computer
     * pawns.
     *
     * @param isToEnable false if pawns shall be locked, true otherwise.
     */
    private void cPawnsSwitch(boolean isToEnable) {
        if (isToEnable) {
            SwingUtilities.invokeLater(
                    () -> view.enableComputerPawns(getHumanSymbol()));
        } else {
            SwingUtilities.invokeLater(
                    () -> view.disableComputerPawns(getHumanSymbol()));
        }
    }

    /**
     * Method advising the view to either allow to click or lock all tiles of
     * the board.
     *
     * @param toEnable false if tiles shall be locked, true otherwise.
     */
    private void allSwitch(boolean toEnable) {
        SwingUtilities.invokeLater(() -> view.switchAll(toEnable));
    }

    /**
     * Assigning the SwingWorker currently working to the parameter.
     *
     * @param started is the SwingWorker who (is about to) start(s).
     */
    private void swingWorkerStarting(SwingWorker<Board, Void> started) {
        this.currentWorker = started;
    }

    /**
     * Canceling the execution of the current SwingWorker.
     */
    public void killCurrentSwingWorker() {
        if (currentWorker != null) {
            currentWorker.cancel(true);
            this.currentWorker = null;
        }
    }

    /**
     * Advising to view to either enable or disable the "undo" button,
     * reversing the last action.
     *
     * @param toEnable true if it shall be enabled, false otherwise.
     */
    private void enableUndo(boolean toEnable) {
        SwingUtilities.invokeLater(() -> view.enableUndo(toEnable));
    }

    /**
     * Directing the view to move a pawn off a tile to another one.
     *
     * @param from the tile the pawn currently stands on in the view.
     * @param to the targeted tile.
     */
    private void directMoveView(BoardTile from, BoardTile to) {
        SwingUtilities.invokeLater(() -> view.movePawn(from, to));
    }

    /**
     * Directing the view to highlight a given tile (the pawn currently
     * selected).
     *
     * @param toHighlight the tile to highlight.
     */
    private void highlightSwitch(boolean toHighlight) {
        if (pawnHolding != null) {
            BoardTile cache = pawnHolding;
            SwingUtilities.invokeLater(()
                    -> view.highlightField(cache, toHighlight));

        }
    }

    /**
     * Telling the view to display a PopUp with a certain errormessage.
     *
     * @param message is the content/text to be displayed in the PopUp.
     */
    private void causeErrorPopUp(String message) {
        SwingUtilities.invokeLater(() -> view.errorPopUp(message));
    }

    /**
     * Telling the view to display a PopUp with a certain NONE-errormessage.
     *
     * @param message is the content/text to be displayed in the PopUp.
     */
    private void causePopUp(String message) {
        SwingUtilities.invokeLater(() -> view.popUp(message));
    }
}
