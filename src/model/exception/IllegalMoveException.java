package model.exception;

/**
 * Extended RuntimeException used in the machineMove() and move() methods of
 * the PawnBoard class in the Model.Board-package.
 * Thrown with a fitting message if either an illegal move is performed, an
 * illegible player wants to perform a move or a move shall be performed
 * although the game is already over with a fitting message.
 */
public class IllegalMoveException extends RuntimeException {
    /**
     * Constructor for the Model.Exception to throw.
     *
     * @param s carries the reason for the thrown Model.Exception.
     */
    public IllegalMoveException(String s) {
        super(s);
    }
}