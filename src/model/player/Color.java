package model.player;

/**
 * Enum containing the playable colors, as well as a NONE-color for unused
 * fields of the board.
 */
public enum Color {
    /**
     * The color of all black pawns.
     */
    BLACK,
    /**
     * The color of all white pawns.
     */
    WHITE,
    /**
     * The color assigned to all fields without a pawn on it.
     */
    NONE;
}
