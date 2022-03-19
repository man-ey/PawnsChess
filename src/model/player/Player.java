package model.player;

/**
 * Class for the Players of the game, holding a assigned color (the actual
 * Model.Player.Model.Player class of a game, black or white).
 */
public class Player {

    private final Color color;

    /**
     * Basic Constructor for creating a new Model.Player.
     *
     * @param givenColor the color assigned.
     */
    public Player(Color givenColor) {
        this.color = givenColor;
    }

    /**
     * Getter method for the assigned color.
     *
     * @return the color.
     */
    public Color getColor() {
        return color;
    }
}