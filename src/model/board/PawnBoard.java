package model.board;

import model.exception.IllegalMoveException;
import model.player.Color;
import model.player.Player;
import model.tree.DecisionTree;

import java.util.List;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.HashMap;
import java.util.Arrays;

/**
 * Implementation of the Model.Board-interface realizing a pawn-chess-game (only
 * pawns on the board)
 */
public class PawnBoard implements Board {

    // Array filled with ' ' = free tile, 'W' = white pawn, 'B' = black pawn.
    private char[][] board;
    private Player human;
    private Player computer;
    private Player starter;
    private Player currentPlayer;
    private Player winner;
    private int diffLevel;
    private final char humanSymbol;
    private final char botSymbol;

    /**
     * Constructor for a new Model.Board.
     *
     * @param starter player to start with 0 = human, 1 = bot.
     * @param difficulty level of prediction of the bot.
     * @param humanColor used by the human on the bottom of the board.
     */
    public PawnBoard(int starter, int difficulty, Color humanColor) {
        this.diffLevel = difficulty;
        this.human = new Player(humanColor);
        this.winner = null;
        this.board = new char[Board.SIZE][Board.SIZE];

        if (humanColor == Color.WHITE) {
            this.computer = new Player(Color.BLACK);
            botSymbol = 'B';
            humanSymbol = 'W';
        } else {
            this.computer = new Player(Color.WHITE);
            botSymbol = 'W';
            humanSymbol = 'B';
        }
        fillBoard();
        if (starter > 0) {
            this.starter = computer;
            this.currentPlayer = computer;
        } else {
            this.starter = human;
            this.currentPlayer = human;
        }
    }

    /**
     * Method filling the board/char array with all pawns before a game starts.
     */
    private void fillBoard() {
        for (int i = 0; i < board.length; i++) {
            board[i][0] = botSymbol;
            board[i][Board.SIZE - 1] = humanSymbol;
        }
        for (int i = 0; i < board.length; i++) {
            for (int j = 1; j < (board.length - 1); j++) {
                board[i][j] = ' ';
            }
        }
    }

    /**
     * Getter method for the current player object.
     *
     * @return the player whose turn it is right now.
     */
    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    /**
     * Getter method for the char array containing the positioning of all pawns.
     *
     * @return the char array with all pawns.
     */
    public char[][] getBoard() {
        return board;
    }

    /**
     * Filtering the board for pawns and collecting them.
     *
     * @param color of the pawns who shall be collected/located.
     * @return an ArrayList of arrays each containing the coordinates of one
     * pawn of the wanted color.
     */
    public List<int[]> getPawnsFor(Color color) {
        List<int[]> toReturn = new ArrayList<>();
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board.length; j++) {
                if (getSlot(i, j) == color) {
                    int[] toAdd = new int[2];
                    toAdd[0] = i;
                    toAdd[1] = j;
                    toReturn.add(toAdd);
                }
            }
        }
        return toReturn;
    }

    /**
     * Getter method for the color of the pawns of the computer in this game.
     *
     * @return the color of the computers pawns.
     */
    public Color getComputerColor() {
        if (getHumanColor() == Color.WHITE) {
            return Color.BLACK;
        } else {
            return Color.WHITE;
        }
    }

    /**
     * Method moving a pawn to a given spot and replacing the old spot with a
     * blank char.
     *
     * @param colFrom is the y-coordinate of the pawn to be moved.
     * @param rowFrom is the x-coordinate of the pawn to be moved.
     * @param colTo is the y-coordinate where the pawn has to be placed.
     * @param rowTo is the x-coordinate where the pawn has to be placed.
     */
    private void movePawn(int colFrom, int rowFrom, int colTo, int rowTo) {
        char pawn = board[colFrom][rowFrom];
        board[colTo][rowTo] = pawn;
        board[colFrom][rowFrom] = ' ';
    }

    /**
     * Method checking all available moves for a pawn of the computer
     * standing on the left border column.
     *
     * @param row is the x-coordinate of the pawn to check.
     * @return an ArrayList of Arrays containing all new possible tiles to be
     * possibly accessed.
     */
    private List<int[]> pawnMoveSetLeftBorderComputer(int row) {
        List<int[]> toReturn = new ArrayList<>();
        Color pawnColor = getSlot(0, row);

        if (getSlot(0, row + 1) == Color.NONE) {
            int[] toAdd = new int[] {0, row + 1};
            toReturn.add(toAdd);
        }

        Color across = getSlot(1, row + 1);
        if (across != pawnColor && across != Color.NONE) {
            int[] toAdd = new int[] {1, row + 1};
            toReturn.add(toAdd);
        }
        return toReturn;
    }

    /**
     * Method checking all available moves for a pawn of the computer
     * standing on the right border column.
     *
     * @param row is the x-coordinate of the pawn to check.
     * @return an ArrayList of Arrays containing all new possible tiles
     * to be possibly accessed.
     */
    private List<int[]> pawnMoveSetRightBorderComputer(int row) {
        List<int[]> toReturn = new ArrayList<>();
        Color pawnColor = getSlot((Board.SIZE - 1), row);

        if (getSlot((Board.SIZE - 1), row + 1) == Color.NONE) {
            int[] toAdd = new int[] {(Board.SIZE - 1), row + 1};
            toReturn.add(toAdd);
        }

        Color across = getSlot((Board.SIZE - 2), row + 1);
        if (across != pawnColor && across != Color.NONE) {
            int[] toAdd = new int[] {(Board.SIZE - 2), row + 1};
            toReturn.add(toAdd);
        }
        return toReturn;
    }

    /**
     * Method checking all available moves for a pawn of the computer
     * standing on the start row.
     *
     * @param col is the y-coordinate of the pawn to check.
     * @return an ArrayList of Arrays containing all new possible tiles to be
     * possibly accessed.
     */
    private List<int[]> pawnMoveSetStartComputer(int col) {
        List<int[]> toReturn;

        if (col == 0) {
            toReturn = pawnMoveSetLeftBorderComputer(0);
        } else if (col == 7) {
            toReturn = pawnMoveSetRightBorderComputer(0);
        } else {
            toReturn = pawnMoveSetRegularComputer(col, 0);
        }

        if (getSlot(col, 2) == Color.NONE) {
            toReturn.add(0, new int[] {col, 2});
        }
        return toReturn;
    }

    /**
     * Method checking all available moves for a pawn of the computer
     * NOT standing on the starting row, or the border columns.
     *
     * @param col is the y-coordinate of the pawn.
     * @param row is the x-coordinate of the pawn.
     * @return an ArrayList of Arrays containing all new possible tiles to
     * be possibly accessed.
     */
    private List<int[]> pawnMoveSetRegularComputer(int col, int row) {
        List<int[]> toReturn = new ArrayList<>();
        Color pawnColor = getSlot(col, row);

        if (getSlot(col, row + 1) == Color.NONE) {
            int[] toAdd = new int[] {col, row + 1};
            toReturn.add(toAdd);
        }

        Color across = getSlot(col - 1, row + 1);
        if (across != pawnColor && across != Color.NONE) {
            int[] toAdd = new int[] {col - 1, row + 1};
            toReturn.add(toAdd);
        }

        across = getSlot(col + 1, row + 1);
        if (across != pawnColor && across != Color.NONE) {
            int[] toAdd = new int[] {col + 1, row + 1};
            toReturn.add(toAdd);
        }
        return toReturn;
    }

    /**
     * Method gathering all pawns of the computer, reversing the order so it
     * starts with the pawn on the far right and then calling the
     * collectMoves method to fill the HashMap with he previously collected
     * pawns.
     *
     * @return a HashMap with all arrays containing the coordinates of a pawn
     * as key and an ArrayList containing all possible destinations of the
     * pawn as value.
     */
    private LinkedHashMap<List<Integer>, List<int[]>>
    pawnMoveSetComputer() {
        List<int[]> pawns = getPawnsFor(getComputerColor());
        LinkedHashMap<List<Integer>, List<int[]>> toReturn;

        for (int i = pawns.size() - 1; i >= 0; i--) {
            int[] temp = pawns.get(i);
            pawns.add(temp);
            pawns.remove(i);
        }
        toReturn = collectMovesComputer(pawns);
        return toReturn;
    }

    /**
     * Method filling a HashMap with an ArrayList containing the coordinates of
     * all pawns on the board of the computer, setting its Arrays as keys and
     * calling for each pawn the responsible "check" method returning an
     * ArrayList of possible destinations, which is then set as values.
     *
     * @param pawns are all pawns for whom all possible moves shall be
     *              collected.
     * @return the LinkedHashMap using the pawns as Key and destinations as
     * values.
     */
    private LinkedHashMap<List<Integer>, List<int[]>>
    collectMovesComputer(List<int[]> pawns) {
        LinkedHashMap<List<Integer>, List<int[]>> toReturn
                = new LinkedHashMap<>();
        List<int[]> moves;
        for (int i = pawns.size() - 1; i >= 0; i--) {
            int[] current = pawns.get(i);
            List<Integer> currentKey = new ArrayList<>();
            currentKey.add(current[0]);
            currentKey.add(current[1]);
            if (current[1] == 0) {
                moves = pawnMoveSetStartComputer(current[0]);
                toReturn.put(currentKey, moves);
            } else if (current[0] == 0) {
                moves = pawnMoveSetLeftBorderComputer(current[1]);
                toReturn.put(currentKey, moves);
            } else if (current[0] == (Board.SIZE - 1)) {
                moves = pawnMoveSetRightBorderComputer(current[1]);
                toReturn.put(currentKey, moves);
            } else {
                moves = pawnMoveSetRegularComputer(current[0], current[1]);
                toReturn.put(currentKey, moves);
            }
        }

        return toReturn;
    }

    /**
     * Method gathering all allowed/possible moves for the human player
     * in a LinkedHashMap using the coordinates of each pawn as key matching the
     * moves stored in an ArrayList.
     *
     * @param pawns is an ArrayList containing the coordinates of all pawns on
     *              the board of the human player.
     * @return the LinkedHashMap using the pawns as Key and destinations as
     * values.
     */
    private LinkedHashMap<List<Integer>, List<int[]>>
    collectMovesHuman(List<int[]> pawns) {
        LinkedHashMap<List<Integer>, List<int[]>> toReturn =
                new LinkedHashMap<>();
        List<int[]> moves;

        // Call correct "check"-method for each pawn.
        for (int[] current : pawns) {
            List<Integer> currentKey = new ArrayList<>();
            currentKey.add(current[0]);
            currentKey.add(current[1]);
            if (current[0] >= 0 && current[1] >= 0) {
                if (current[1] == Board.SIZE - 1) {
                    moves = pawnMoveSetStartPlayer(current[0]);
                    toReturn.put(currentKey, moves);
                } else if (current[0] == 0) {
                    moves = pawnMoveSetLeftBorderPlayer(current[1]);
                    toReturn.put(currentKey, moves);
                } else if (current[0] == (Board.SIZE - 1)) {
                    moves = pawnMoveSetRightBorderPlayer(current[1]);
                    toReturn.put(currentKey, moves);
                } else {
                    moves = pawnMoveSetRegularPlayer(current[0], current[1]);
                    toReturn.put(currentKey, moves);
                }
            }
        }
        return toReturn;
    }

    /**
     * Method gathering all pawns of the human player, reversing the order so it
     * starts with the pawn on the far right and then calling the
     * collectMoves method to fill the HashMap with he previously collected
     * pawns.
     *
     * @return a HashMap with all arrays containing the coordinates of a pawn
     * as key and an ArrayList containing all possible destinations of the
     * pawn as value.
     */
    private LinkedHashMap<List<Integer>, List<int[]>>
    pawnMoveSetPlayer() {
        List<int[]> pawns = getPawnsFor(getHumanColor());
        return collectMovesHuman(pawns);
    }

    /**
     * Method checking all available moves for a Humans pawn standing on
     * the left border column.
     *
     * @param row is the x-coordinate of the pawn to check.
     * @return an ArrayList of Arrays containing all new possible tiles to be
     * possibly moved to.
     */
    private List<int[]> pawnMoveSetLeftBorderPlayer(int row) {
        List<int[]> toReturn = new ArrayList<>();
        Color pawnColor = getSlot(0, row);

        if (getSlot(0, row - 1) == Color.NONE) {
            int[] toAdd = new int[] {0, row - 1};
            toReturn.add(toAdd);
        }

        Color across = getSlot(1, row - 1);
        if (across != pawnColor && across != Color.NONE) {
            int[] toAdd = new int[] {1, row - 1};
            toReturn.add(toAdd);
        }
        return toReturn;
    }

    /**
     * Method checking all available moves for a pawn of the human player
     * standing on the right border column.
     *
     * @param row is the x-coordinate of the pawn to check.
     * @return an ArrayList of Arrays containing all new possible tiles to be
     * possibly moved to.
     */
    private List<int[]> pawnMoveSetRightBorderPlayer(int row) {
        List<int[]> toReturn = new ArrayList<>();
        Color pawnColor = getSlot((Board.SIZE - 1), row);

        if (getSlot((Board.SIZE - 1), row - 1) == Color.NONE) {
            int[] toAdd = new int[] {(Board.SIZE - 1), row - 1};
            toReturn.add(toAdd);
        }

        Color across = getSlot((Board.SIZE - 2), row - 1);
        if (across != pawnColor && across != Color.NONE) {
            int[] toAdd = new int[] {(Board.SIZE - 2), row - 1};
            toReturn.add(toAdd);
        }
        return toReturn;
    }

    /**
     * Method checking all available moves for a pawn of the human player
     * standing on the start row.
     *
     * @param col is the y-coordinate of the pawn to be checked.
     * @return an ArrayList of Arrays containing all new possible tiles to be
     * possibly accessed.
     */
    private List<int[]> pawnMoveSetStartPlayer(int col) {
        List<int[]> toReturn;

        if (col == 0) {
            toReturn = pawnMoveSetLeftBorderPlayer(Board.SIZE - 1);
        } else if (col == 7) {
            toReturn = pawnMoveSetRightBorderPlayer(Board.SIZE - 1);
        } else {
            toReturn = pawnMoveSetRegularPlayer(col, Board.SIZE - 1);
        }

        if (getSlot(col, (Board.SIZE - 3)) == Color.NONE) {
            toReturn.add(0, new int[] {col, (Board.SIZE - 3)});
        }
        return toReturn;
    }

    /**
     * Method checking all available moves for a pawn of the human player
     * NOT standing on the starting row, or the border columns.
     *
     * @param col is the y-coordinate of the current pawn to check.
     * @param row is the x-coordinate of the current pawn to check.
     * @return an ArrayList of Arrays containing all new possible tiles to be
     * possibly accessed.
     */
    private List<int[]> pawnMoveSetRegularPlayer(int col, int row) {
        List<int[]> toReturn = new ArrayList<>();
        Color pawnColor = getSlot(col, row);

        if (row >= 0) {
            if (getSlot(col, row - 1) == Color.NONE) {
                int[] toAdd = new int[] {col, row - 1};
                toReturn.add(toAdd);
            }

            Color across = getSlot(col - 1, row - 1);
            if (across != pawnColor && across != Color.NONE) {
                int[] toAdd = new int[] {col - 1, row - 1};
                toReturn.add(toAdd);
            }

            across = getSlot(col + 1, row - 1);
            if (across != pawnColor && across != Color.NONE) {
                int[] toAdd = new int[] {col + 1, row - 1};
                toReturn.add(toAdd);
            }
        }

        return toReturn;
    }

    /**
     * Checking if a certain move by the current player would be a legal/viable
     * move on the current board/situation.
     *
     * @param colFrom is the y-coordinate of the pawn to be possibly moved.
     * @param rowFrom is the x-coordinate of the pawn to be possibly moved.
     * @param colTo is the y-coordinate of the targeted tile.
     * @param rowTo is the x-coordinate of the targeted tile.
     * @return true if the move is legal, false otherwise.
     */
    private boolean isViableMove(int colFrom, int rowFrom, int colTo,
                                 int rowTo) {
        List<Integer> fromLoc = new ArrayList<>();
        fromLoc.add(colFrom);
        fromLoc.add(rowFrom);
        int[] targetLoc = new int[] {colTo, rowTo};
        HashMap<List<Integer>, List<int[]>> allMoves =
                viablePawnMoves(currentPlayer);
        if (allMoves.containsKey(fromLoc)) {
            List<int[]> moves =
                    viablePawnMoves(currentPlayer).get(fromLoc);
            for (int[] current : moves) {
                if (current[0] == targetLoc[0] && current[1] == targetLoc[1]) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Helper method calling the right method to gather all moves possible
     * for a given player.
     *
     * @param current the player whose legal moves shall be collected.
     * @return a LinkedHashMap with an ArrayList of integer Arrays, each
     * containing the coordinates of one pawn, as key and and an ArrayList
     * containing possible target coordinates for the pawn in an integer
     * Array as values.
     */
    public LinkedHashMap<List<Integer>, List<int[]>>
    viablePawnMoves(Player current) {
        if (current == human) {
            return pawnMoveSetPlayer();
        } else {
            return pawnMoveSetComputer();
        }
    }

    /**
     * Checking if every pawn of the computer has no possibility of
     * moving and therefore if the human player has to skip his turn.
     *
     * @return true if the computer has to skip his turn, false otherwise.
     */
    private boolean mustSkipComputer() {
        List<int[]> pawns = getPawnsFor(getComputerColor());

        boolean mustSkip = true;
        for (int[] current : pawns) {
            if (current[1] != Board.SIZE - 1) {
                if (current[1] == 0
                        && board[current[0]][current[1] + 2] == ' ') {
                    mustSkip = false;
                    break;
                }
                if (board[current[0]][current[1] + 1] != humanSymbol) {
                    mustSkip = false;
                    break;
                } else if (current[0] > 0 && current[0] <= Board.SIZE - 1) {
                    if (board[current[0] - 1][current[1] + 1] == humanSymbol) {
                        mustSkip = false;
                        break;
                    }
                } else if (current[0] < Board.SIZE - 1) {
                    if (board[current[0] + 1][current[1] + 1] == humanSymbol) {
                        mustSkip = false;
                        break;
                    }
                }
            }
        }
        return mustSkip;
    }

    /**
     * Checking if every pawn of the human player has no possibility of
     * moving and therefore if the human player has to skip his turn.
     *
     * @return true if no move is possible, false otherwise.
     */
    private boolean mustSkipHuman() {
        List<int[]> pawnsHum = getPawnsFor(getHumanColor());

        boolean mustSkip = true;
        for (int[] current : pawnsHum) {
            if (current[1] != 0) {
                if (current[1] == Board.SIZE - 1
                        && board[current[0]][current[1] - 2] == ' ') {
                    mustSkip = false;
                    break;
                }
                if (board[current[0]][current[1] - 1] != botSymbol) {
                    mustSkip = false;
                    break;
                } else if (current[0] > 0 && current[0] <= Board.SIZE - 2) {
                    if (board[current[0] - 1][current[1] - 1] == botSymbol) {
                        mustSkip = false;
                        break;
                    }
                } else if (current[0] < Board.SIZE - 1) {
                    if (board[current[0] + 1][current[1] - 1] == botSymbol) {
                        mustSkip = false;
                        break;
                    }
                }
            }
        }
        return mustSkip;
    }

    /**
     * Moving a pawn from a given location to a specific new tile and
     * checking after performing the move if the game is over and setting
     * whose players turn it is afterwards.
     *
     * @param colFrom is the y-coordinate of the pawn to be moved.
     * @param rowFrom is the x-coordinate of the pawn to be moved.
     * @param colTo is the y-coordinate of the targeted tile.
     * @param rowTo is the x-coordinate of the targeted tile.
     * @return the same object with a now update board and currentPlayer.
     */
    public PawnBoard moving(int colFrom, int rowFrom, int colTo, int rowTo) {
        movePawn(colFrom, rowFrom, colTo, rowTo);
        if (isGameOver()) {
            return this;
        }
        currentPlayer = getNextPlayer();
        if (currentPlayer == human) {
            if (mustSkipHuman()) {
                currentPlayer = getNextPlayer();
            }
        } else if (mustSkipComputer()) {
            currentPlayer = getNextPlayer();
        }
        return this;
    }

    /**
     * Method returning the player who has currently most pawns placed on the
     * board.
     *
     * @return the player with most pawns, null if both have an equal amount
     * of pawns.
     */
    private Player mostPawns() {
        if (getPawnsFor(Color.WHITE).size()
                > getPawnsFor(Color.BLACK).size()) {
            if (getHumanColor() == Color.WHITE) {
                return human;
            } else {
                return computer;
            }
        } else if (getPawnsFor(Color.WHITE).size()
                < getPawnsFor(Color.BLACK).size()) {
            if (getHumanColor() == Color.WHITE) {
                return computer;
            } else {
                return human;
            }
        } else {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Player getOpeningPlayer() {
        return starter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Color getHumanColor() {
        return human.getColor();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Player getNextPlayer() {
        if (currentPlayer == human) {
            return computer;
        } else {
            return human;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PawnBoard move(int colFrom, int rowFrom, int colTo, int rowTo) {
        if (!isViableMove(colFrom, rowFrom, colTo, rowTo)) {
            throw new IllegalArgumentException("Illegal move or coordinates!");
        } else if (isGameOver()) {
            throw new IllegalMoveException("Game already over!");
        } else if (currentPlayer != human) {
            throw new IllegalMoveException("Not your turn!");
        }
        return moving(colFrom, rowFrom, colTo, rowTo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PawnBoard machineMove() {
        if (isGameOver()) {
            throw new IllegalMoveException("Game already over!");
        }
        DecisionTree tree =
                new DecisionTree(this, diffLevel);
        PawnBoard toReturn = tree.bestMove();
        if (toReturn.isGameOver()) {
            if (toReturn.mustSkipHuman() && toReturn.mustSkipComputer()) {
                toReturn.winner = null;
            }
            return toReturn;
        }
        currentPlayer = getNextPlayer();
        if (toReturn.mustSkipHuman()) {
            currentPlayer = getNextPlayer();
        }
        return toReturn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setLevel(int level) {
        if (level < 0) {
            this.diffLevel = level;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isGameOver() {
        for (int i = 0; i < board.length; i++) {
            if (getSlot(i, 0) == getHumanColor()) {
                winner = human;
                return true;
            }
        }
        for (int i = 0; i < board.length; i++) {
            if (getSlot(i, Board.SIZE - 1) == computer.getColor()) {
                winner = computer;
                return true;
            }
        }
        if (getPawnsFor(getHumanColor()).size() == 0
                || getPawnsFor(getComputerColor()).size() == 0) {
            return true;
        } else if (mustSkipComputer() && mustSkipHuman()) {
            winner = mostPawns();
            return true;
        } else {
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Player getWinner() {
        return winner;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNumberOfTiles(Player player) {
        return getPawnsFor(player.getColor()).size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Color getSlot(int col, int row) {
        char pawn = board[col][row];
        if (pawn == 'W') {
            return Color.WHITE;
        } else if (pawn == 'B') {
            return Color.BLACK;
        } else {
            return Color.NONE;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PawnBoard clone() {
        PawnBoard cloned;
        try {
            cloned = (PawnBoard) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            return null;
        }

        cloned.board =
                Arrays.stream(board).map(char[]::clone).toArray(char[][]::new);

        cloned.human = human;
        cloned.computer = computer;
        if (currentPlayer == human) {
            cloned.currentPlayer = cloned.human;
        } else {
            cloned.currentPlayer = cloned.computer;
        }
        if (starter == human) {
            cloned.starter = cloned.human;
        } else {
            cloned.starter = cloned.computer;
        }

        return cloned;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuilder toReturn = new StringBuilder();
        for (int i = 0; i < board.length; i++) {
            for (char[] currentCol : board) {
                toReturn.append(currentCol[i]);
            }
        }
        return toReturn.toString();
    }
}