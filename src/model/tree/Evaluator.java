package model.tree;

import model.board.Board;
import model.board.PawnBoard;
import model.player.Color;


import java.util.ArrayList;
import java.util.List;

/**
 * Class acting as Model.Tree.Evaluator with the tools to evaluate a given
 * board/situation for further use/evaluation in a decision-tree.
 */
public class Evaluator {
    private final char[][] board;
    private final PawnBoard currentGame;
    private final double depth;

    /**
     * Constructor setting up a new Evaluator for:
     *
     * @param game a given game/situation/outcome.
     * @param depth of the Node holding the PawnBoard in the DecisionTree.
     */
    public Evaluator(PawnBoard game, double depth) {
        PawnBoard copy = game.clone();
        this.board = copy.getBoard();
        this.currentGame = copy;
        this.depth = depth;
    }

    /**
     * Method checking if a pawn is isolated, if it is currently standing on
     * the start row of the computer, but not in one of the corners.
     *
     * @param pawn the coordinates of the pawn currently checked.
     * @return true if the pawn is isolated,false otherwise.
     */
    private boolean checkIsoRowZeroReg(int[] pawn) {
        int curCol = pawn[0];
        int curRow = 0;
        char check = board[curCol][curRow];
        return board[curCol][curRow + 1] != check
                && board[curCol + 1][curRow] != check
                && board[curCol + 1][curRow + 1] != check
                && board[curCol - 1][curRow] != check
                && board[curCol - 1][curRow + 1] != check;
    }

    /**
     * Method checking if a pawn is isolated, if it is currently standing on
     * the start row of the human, but not in one of the corners.
     *
     * @param pawn the coordinates of the pawn to be checked.
     * @return true if the pawn is isolated, false otherwise.
     */
    private boolean checkIsoRowTopReg(int[] pawn) {
        int curCol = pawn[0];
        int curRow = pawn[1];
        char check = board[curCol][curRow];
        return board[curCol][curRow - 1] != check
                && board[curCol + 1][curRow] != check
                && board[curCol + 1][curRow - 1] != check
                && board[curCol - 1][curRow] != check
                && board[curCol - 1][curRow - 1] != check;
    }

    /**
     * Method checking if a pawn is isolated, while it currently is standing
     * on the far left column of the board, but not in one of the corners.
     *
     * @param pawn the coordinates of teh pawn to be checked.
     * @return true if the pawn is isolated, false otherwise.
     */
    private boolean checkIsoLeftReg(int[] pawn) {
        int curCol = pawn[0];
        int curRow = pawn[1];
        char check = board[curCol][curRow];
        return board[curCol][curRow + 1] != check
                && board[curCol + 1][curRow] != check
                && board[curCol][curRow - 1] != check
                && board[curCol + 1][curRow - 1] != check
                && board[curCol + 1][curRow + 1] != check;
    }

    /**
     * Method checking if a pawn is isolated, while it currently is standing
     * on the far right column of the board, but not in one of the corners.
     *
     * @param pawn the coordinates of the pawn to be checked.
     * @return true if the pawn is isolated, false otherwise.
     */
    private boolean checkIsoRightReg(int[] pawn) {
        int curCol = pawn[0];
        int curRow = pawn[1];
        char check = board[curCol][curRow];
        return board[curCol][curRow + 1] != check
                && board[curCol][curRow - 1] != check
                && board[curCol - 1][curRow] != check
                && board[curCol - 1][curRow - 1] != check
                && board[curCol - 1][curRow + 1] != check;
    }

    /**
     * Checking all fields around the given pawn for other pawns/isolation
     * with the pawn not standing on one of the outer rows/columns.
     *
     * @param pawn the coordinates of the pawn to be checked.
     * @return true if the pawn is isolated, false otherwise.
     */
    private boolean checkIsoRegular(int[] pawn) {
        int curCol = pawn[0];
        int curRow = pawn[1];
        char check = board[curCol][curRow];
        return board[curCol][curRow + 1] != check
                && board[curCol][curRow - 1] != check
                && board[curCol + 1][curRow] != check
                && board[curCol - 1][curRow] != check
                && board[curCol + 1][curRow + 1] != check
                && board[curCol - 1][curRow + 1] != check
                && board[curCol - 1][curRow - 1] != check
                && board[curCol + 1][curRow - 1] != check;
    }

    /**
     * Checking the isolation of a pawn when it is standing in one of the
     * four corners of the board.
     *
     * @param pawn the coordinates of the pawn to check.
     * @return true if the pawn is isolated, false otherwise.
     */
    private boolean checkIsoCorners(int[] pawn) {
        int curCol = pawn[0];
        int curRow = pawn[1];
        char check = board[curCol][curRow];
        // Left or right corner?
        if (curCol == 0) {
            // Upper or lower corner?
            if (curRow == 0) {
                return board[curCol][curRow + 1] != check
                        && board[curCol + 1][curRow] != check
                        && board[curCol + 1][curRow + 1] != check;
            } else {
                return board[curCol][curRow - 1] == ' '
                        && board[curCol + 1][curRow - 1] == ' '
                        && board[curCol + 1][curRow] == ' ';
            }
        } else {
            // Upper or lower corner?
            if (curRow == 0) {
                return board[curCol][curRow + 1] == ' '
                        && board[curCol - 1][curRow] == ' '
                        && board[curCol - 1][curRow + 1] == ' ';
            } else {
                return board[curCol][curRow - 1] == ' '
                        && board[curCol - 1][curRow] == ' '
                        && board[curCol - 1][curRow - 1] == ' ';
            }
        }
    }

    /**
     * Method called when a pawn located on one of the side columns has to be
     * checked for isolation, calling the fitting method for the pawn.
     *
     * @param pawn the coordinates of the pawn to check.
     * @return true if the pawn is isolated, false otherwise.
     */
    private boolean checkSidesIso(int[] pawn) {
        int curCol = pawn[0];
        if (curCol == 0) {
            return checkIsoLeftReg(pawn);
        } else {
            return checkIsoRightReg(pawn);
        }
    }

    /**
     * Method called when a pawn has to be checked for isolation and is
     * standing on either the top or bottom row.
     * Calls the fitting method for the pawns location.
     *
     * @param pawn the coordinates of the pawn to be checked.
     * @return true if the pawn is isolated, false otherwise.
     */
    private boolean checkTopBottomRowsIso(int[] pawn) {
        int curRow = pawn[1];
        if (curRow == 0) {
            return checkIsoRowZeroReg(pawn);
        } else {
            return checkIsoRowTopReg(pawn);
        }
    }

    /**
     * Counting the amount of isolated pawns of the human player by calling
     * for each pawn a fitting method to check its status.
     *
     * @return the amount of isolated pawns of the human player as a double.
     */
    private double getAllIsolated(Color color) {
        List<int[]> pawns = currentGame.getPawnsFor(color);
        double amountIsolated = 0;
        // Check all pawns with fitting methods.
        for (int[] current : pawns) {
            int curCol = current[0];
            int curRow = current[1];
            if (curCol == Board.SIZE - 1 || curCol == 0) {
                if (curRow == 0 || curRow == Board.SIZE - 1) {
                    if (checkIsoCorners(current)) {
                        amountIsolated++;
                    }
                } else {
                    if (checkSidesIso(current)) {
                        amountIsolated++;
                    }
                }
            } else {
                if (curRow == 0 || curRow == Board.SIZE - 1) {
                    if (checkTopBottomRowsIso(current)) {
                        amountIsolated++;
                    }
                } else {
                    if (checkIsoRegular(current)) {
                        amountIsolated++;
                    }
                }
            }
        }

        return amountIsolated;
    }

    /**
     * Checking if a pawn of the computer/bot is in danger (whilst not being
     * covered) of being beaten by a pawn of the human player, with its
     * location not being one of the border columns/rows.
     *
     * @param pawn the coordinates of the pawn to check.
     * @param check the char of the humans pawns on the board.
     * @return true if it is endangered, false otherwise.
     */
    private boolean isEndangeredComputerReg(int[] pawn, char check) {
        boolean danger = board[pawn[0] + 1][pawn[1] + 1] == check
                || board[pawn[0] - 1][pawn[1] + 1] == check;
        boolean coverRight = board[pawn[0] + 1][pawn[1] - 1] != check
                && board[pawn[0] + 1][pawn[1] - 1] != ' ';
        boolean coverLeft = board[pawn[0] - 1][pawn[1] - 1] != check
                && board[pawn[0] - 1][pawn[1] - 1] != ' ';
        return danger && !coverLeft && !coverRight;
    }

    /**
     * Checking if a pawn of the computer/bot is in danger (whilst not being
     * covered) of being beaten by a pawn of the human player, with its
     * location on the left border of the board, but not one of the corners.
     *
     * @param pawn the coordinates of the pawn to check.
     * @param check the char of the humans pawns on the board.
     * @return true if the pawn is endangered, false otherwise.
     */
    private boolean isEndangeredComputerLeft(int[] pawn, char check) {
        boolean danger = board[pawn[0] + 1][pawn[1] + 1] == check;
        boolean cover = board[pawn[0] + 1][pawn[1] - 1] != check
                && board[pawn[0] + 1][pawn[1] - 1] != ' ';
        return danger && !cover;
    }

    /**
     * Checking if a pawn of the computer/bot is in danger (whilst not being
     * covered) of being beaten by a pawn of the human player, with its
     * location on the right border of the board, but not one of the corners.
     *
     * @param pawn the coordinates of the pawn to check.
     * @param check the char of the humans pawns on the board.
     * @return true if the pawn is endangered, false otherwise.
     */
    private boolean isEndangeredComputerRight(int[] pawn, char check) {
        boolean danger = board[pawn[0] - 1][pawn[1] + 1] == check;
        boolean cover = board[pawn[0] - 1][pawn[1] - 1] != check
                && board[pawn[0] - 1][pawn[1] - 1] != ' ';
        return danger && !cover;
    }

    /**
     * Checking if a pawn of the computer/bot is in danger (whilst not being
     * covered) of being beaten by a pawn of the human player, with its
     * location on the starting row, but not one of the corners.
     *
     * @param pawn the coordinates of the pawn to check.
     * @param check the char of the humans pawns on the board.
     * @return true if the pawn is endangered, false otherwise.
     */
    private boolean isEndangeredComputerStart(int[] pawn, char check) {
        return board[pawn[0] - 1][pawn[1] + 1] == check
                || board[pawn[0] + 1][pawn[1] + 1] == check;
    }

    /**
     * Checking if a pawn of the computer/bot is in danger (whilst not being
     * covered) of being beaten by a pawn of the human player, with its
     * location being of the corners.
     *
     * @param pawn the coordinates of the pawn to check.
     * @param check the char of the humans pawns on the board.
     * @return true if the pawn is endangered, false otherwise.
     */
    private boolean isEndangeredComputerCorner(int[] pawn, char check) {
        if (pawn[0] == 0) {
            return board[pawn[0] + 1][pawn[1] + 1] == check;
        } else {
            return board[pawn[0] - 1][pawn[1] + 1] == check;
        }
    }

    /**
     * Gathering the location of all endangered pawns of the computer/bot
     * player on the current board/situation, by calling the responsible
     * method for checking the endangerment for each pawn on the board.
     *
     * @return an ArrayList of integer arrays containing the coordinates of
     * all endangered pawns.
     */
    private List<int[]> getAllEndangeredComputer() {
        char check = getEndangeringChar(currentGame.getComputerColor());
        List<int[]> toReturn = new ArrayList<>();
        List<int[]> toIterate =
                currentGame.getPawnsFor(currentGame.getComputerColor());
        for (int[] current : toIterate) {
            // Pawn at goal row?
            if (current[1] < Board.SIZE - 1) {
                if ((current[0] == 0 || current[0] == Board.SIZE - 1)
                        && current[1] == 0) {
                    if (isEndangeredComputerCorner(current, check)) {
                        toReturn.add(current);
                    }
                } else if (current[1] == 0) {
                    if (isEndangeredComputerStart(current, check)) {
                        toReturn.add(current);
                    }
                } else if (current[0] == 0) {
                    if (isEndangeredComputerLeft(current, check)) {
                        toReturn.add(current);
                    }
                } else if (current[0] == Board.SIZE - 1) {
                    if (isEndangeredComputerRight(current, check)) {
                        toReturn.add(current);
                    }
                } else {
                    if (isEndangeredComputerReg(current, check)) {
                        toReturn.add(current);
                    }
                }
            }
        }
        return toReturn;
    }

    /**
     * Gathering the location of all endangered pawns of the human player
     * on the current board/situation, by calling the responsible
     * method for checking the endangerment for each pawn on the board.
     *
     * @return an ArrayList of integer arrays containing the coordinates of
     * all endangered pawns.
     */
    private List<int[]> getAllEndangeredHuman() {
        char check = getEndangeringChar(currentGame.getHumanColor());
        List<int[]> toReturn = new ArrayList<>();
        List<int[]> toIterate
                = currentGame.getPawnsFor(currentGame.getHumanColor());
        for (int[] current : toIterate) {
            // Pawn at goal row?
            if (current[1] > 0) {
                if ((current[0] == 0 || current[0] == Board.SIZE - 1)
                        && current[1] == Board.SIZE - 1) {
                    if (isEndangeredHumanCorner(current, check)) {
                        toReturn.add(current);
                    }
                } else if (current[1] == Board.SIZE - 1) {
                    if (isEndangeredHumanStart(current, check)) {
                        toReturn.add(current);
                    }
                } else if (current[0] == 0) {
                    if (isEndangeredHumanLeft(current, check)) {
                        toReturn.add(current);
                    }
                } else if (current[0] == Board.SIZE - 1) {
                    if (isEndangeredHumanRight(current, check)) {
                        toReturn.add(current);
                    }
                } else {
                    if (isEndangeredHumanReg(current, check)) {
                        toReturn.add(current);
                    }
                }
            }
        }
        return toReturn;
    }

    /**
     * Checking if a pawn of the human player is in danger (whilst not being
     * covered) of being beaten by a pawn of the computer/bot player, with its
     * location not being one of the border columns/rows.
     *
     * @param pawn the coordinates of the pawn to check.
     * @param check the char of the computers/bots pawns on the board.
     * @return true if it is endangered, false otherwise.
     */
    private boolean isEndangeredHumanReg(int[] pawn, char check) {
        boolean danger = board[pawn[0] + 1][pawn[1] - 1] == check
                || board[pawn[0] - 1][pawn[1] - 1] == check;
        boolean coverLeft = board[pawn[0] - 1][pawn[1] + 1] != check
                && board[pawn[0] - 1][pawn[1] + 1] != ' ';
        boolean coverRight = board[pawn[0] + 1][pawn[1] + 1] != check
                && board[pawn[0] + 1][pawn[1] + 1] != ' ';
        return danger && !coverLeft && !coverRight;
    }

    /**
     * Checking if a pawn of the human player is in danger (whilst not being
     * covered) of being beaten by a pawn of the computer/bot player, with its
     * location on the left column, but not one of the two corners.
     *
     * @param pawn the coordinates of the pawn to check.
     * @param check the char of the computers/bots pawns on the board.
     * @return true if it is endangered, false otherwise.
     */
    private boolean isEndangeredHumanLeft(int[] pawn, char check) {
        boolean danger = board[pawn[0] + 1][pawn[1] - 1] == check;
        boolean cover = board[pawn[0] + 1][pawn[1] + 1] != check
                && board[pawn[0] + 1][pawn[1] + 1] != ' ';
        return danger && !cover;
    }

    /**
     * Checking if a pawn of the human player is in danger (whilst not being
     * covered) of being beaten by a pawn of the computer/bot player, with its
     * location on the right column, but none of the two corners.
     *
     * @param pawn the coordinates of the pawn to check.
     * @param check the char of the computers/bots pawns on the board.
     * @return true if it is endangered, false otherwise.
     */
    private boolean isEndangeredHumanRight(int[] pawn, char check) {
        boolean danger = board[pawn[0] - 1][pawn[1] - 1] == check;
        boolean cover = board[pawn[0] - 1][pawn[1] + 1] != check
                && board[pawn[0] - 1][pawn[1] + 1] != ' ';
        return danger && !cover;
    }

    /**
     * Checking if a pawn of the human player is in danger (whilst not being
     * covered) of being beaten by a pawn of the computer/bot player, with its
     * location on the start line (0), but none of the two corners.
     *
     * @param pawn the coordinates of the pawn to check.
     * @param check the char of the computers/bots pawns on the board.
     * @return true if it is endangered, false otherwise.
     */
    private boolean isEndangeredHumanStart(int[] pawn, char check) {
        return board[pawn[0] - 1][pawn[1] - 1] == check
                || board[pawn[0] + 1][pawn[1] - 1] == check;
    }

    /**
     * Checking if a pawn of the human player is in danger (whilst not being
     * covered) of being beaten by a pawn of the computer/bot player, with its
     * location being one of the corners.
     *
     * @param pawn the coordinates of the pawn to check.
     * @param check the char of the computers/bots pawns on the board.
     * @return true if it is endangered, false otherwise.
     */
    private boolean isEndangeredHumanCorner(int[] pawn, char check) {
        if (pawn[0] == 0) {
            return board[pawn[0] + 1][pawn[1] - 1] == check;
        } else {
            return board[pawn[0] - 1][pawn[1] - 1] == check;
        }
    }

    /**
     * Finding out, which char is the endangering char for a player on placed
     * ont he board.
     *
     * @param color the color NOT being the endangering.
     * @return 'W' if the players color is Black and vice-versa.
     */
    private char getEndangeringChar(Color color) {
        if (color == Color.WHITE) {
            return 'B';
        } else {
            return 'W';
        }
    }

    /**
     * Calculating the summed distance all of the humans pawns have traversed
     * on the board.
     *
     * @return the calculated double value.
     */
    private double getDistanceHuman() {
        double toReturn = 0;
        List<int[]> pawns =
                currentGame.getPawnsFor(currentGame.getHumanColor());
        for (int[] current : pawns) {
            toReturn += (Board.SIZE - 1) - current[1];
        }
        return toReturn;
    }

    /**
     * Calculating the summed distance all of pawns of the computer/bot have
     * traversed on the board.
     *
     * @return the calculated double value.
     */
    private double getDistanceComputer() {
        double toReturn = 0;
        List<int[]> pawns =
                currentGame.getPawnsFor(currentGame.getComputerColor());
        for (int[] current : pawns) {
            toReturn += current[1];
        }
        return toReturn;
    }

    /**
     * Checking if the game would be over after the made move and calculating
     * an evaluation depending on the depth in the decision-tree.
     *
     * @return 0 if game isn't over its a draw, teh calculation otherwise.
     */
    private double checkWin() {
        if (currentGame.isGameOver()) {
            //Computer, human win or draw?
            if (currentGame.getWinner().getColor()
                    == currentGame.getComputerColor()) {
                return (5000 / depth);
            } else if (currentGame.getWinner().getColor()
                    == currentGame.getHumanColor()) {
                return 0 - 1.5 * (5000 / depth);
            } else {
                return 0;
            }
        } else {
            return 0;
        }
    }

    /**
     * Evaluating the assigned Model.Board.Model.Board.PawnBoard on based on:
     * -amount of pawns.
     * -amount of endangered pawns.
     * -amount of isolated pawns.
     * -distance the pawns have traversed.
     * -if the game is over or not.
     *
     * @return the calculated evaluation as a double.
     */
    public double evaluateBoard() {
        // Amount of pawns on the board.
        double nHuman =
                currentGame.getPawnsFor(currentGame.getHumanColor()).size();
        double nComp =
                currentGame.getPawnsFor(currentGame.getComputerColor()).size();
        double n = nComp - (1.5 * nHuman);
        // Amount of endangered pawns.
        double cHuman = getAllEndangeredHuman().size();
        double cComp = getAllEndangeredComputer().size();
        double c = cHuman - (1.5 * cComp);
        // Amount of isolated pawns.
        double iHuman = getAllIsolated(currentGame.getHumanColor());
        double iComp = getAllIsolated(currentGame.getComputerColor());
        double i = iHuman - (1.5 * iComp);
        // Distance traversed.
        double dHuman = getDistanceHuman();
        double dComputer = getDistanceComputer();
        double d = dComputer - (1.5 * dHuman);
        // Game over?
        double w = checkWin();

        return (d + i + c + n + w);
    }
}