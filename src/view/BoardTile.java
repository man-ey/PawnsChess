package view;

import javax.swing.JButton;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.RenderingHints;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.image.BufferedImage;

/**
 * Class representing a tile on the board to be played on. Changes its Icon
 * which represents the pawn if ordered to and highlights its border if
 * wanted.
 */
public class BoardTile extends JButton {
    private boolean occupied;
    private Color pawnColor;
    private ComponentListener listener;

    /**
     * Constructor setting up a tile with the given parameters/values.
     *
     * @param tileColor is the background-color fo the tile.
     * @param occupation true if a pawn is on the tile, false otherwise.
     * @param playerColor is the color of the pawn standing on the tile, null
     *                   otherwise.
     */
    public BoardTile(Color tileColor, boolean occupation, Color playerColor) {
        this.setBackground(tileColor);
        this.occupied = occupation;
        if (occupied) {
            this.setEnabled(true);
            this.pawnColor = playerColor;
        } else {
            this.setEnabled(false);
            this.pawnColor = null;
        }
        this.setBorder(BorderFactory.createEmptyBorder());
    }

    /**
     * Method returning the occupation-status of the tile.
     *
     * @return true if a pawn is standing on the tile, false otherwise.
     */
    public boolean isOccupied() {
        return occupied;
    }

    /**
     * Getter method for the color of the pawn standing on the tile.
     *
     * @return Color.White, Color.Black or null.
     */
    public Color getPawnColor() {
        return pawnColor;
    }

    /**
     * Highlighting the tile by putting a green border.
     */
    public void highlight() {
        this.setBorder(BorderFactory.createLineBorder(Color.green));
    }

    /**
     * Removing the highlighting of the tile by removing the (green) border.
     */
    public void unhighlight() {
        this.setBorder(null);
    }

    /**
     * Method executed when a new pawn gets moved onto the tile, or the pawn
     * standing on it gets removed.
     *
     * @param pawn W if the new pawn is white, B if black, or ' ' otherwise.
     */
    public void moveNewPawnOnto(char pawn) {
        if (pawn == 'B') {
            occupied = true;
            pawnColor = Color.BLACK;
            this.setEnabled(true);
            paintPawn();
            removeListener();
            addListener();
        } else if (pawn == 'W') {
            occupied = true;
            pawnColor = Color.WHITE;
            paintPawn();
            removeListener();
            addListener();
        } else {
            this.setIcon(null);
            this.setDisabledIcon(null);
            this.setEnabled(true);
            occupied = false;
            pawnColor = null;
            removeListener();
        }
    }

    /**
     * Paints a pawn-like figure and sets it as the tiles icon.
     */
    public void paintPawn() {
        int w = getWidth();
        int h = getHeight();
        BufferedImage resizedImg =
                new BufferedImage(w, h, BufferedImage.TRANSLUCENT);
        Graphics2D g2 = resizedImg.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        if (pawnColor == Color.BLACK) {
            g2.setColor(Color.BLACK);
        } else if (pawnColor == Color.WHITE) {
            g2.setColor(Color.WHITE);
        }
        g2.fillOval(w / 4, 0, w / 2, h / 2);
        g2.fillOval(w / 60, h / 3, w, h);
        Icon icon = new ImageIcon(resizedImg);
        this.setIcon(icon);
        this.setDisabledIcon(icon);
        g2.dispose();
    }

    /**
     * Removing the assigned ComponentListener, if existing.
     */
    private void removeListener() {
        if (listener != null) {
            this.removeComponentListener(listener);
            listener = null;
        }
    }

    /**
     * Adds a ComponentListener to the tile, which monitors the size of tile
     * and repaints the pawn if the size of the tile changes due to e.g. the
     * user changing the size of the window.
     */
    private void addListener() {
        listener = new ComponentListener() {

            /**
             * Action if the tile gets resized.
             *
             * @param e is resizing.
             */
            @Override
            public void componentResized(ComponentEvent e) {
                paintPawn();
            }

            /**
             * Empty method, necessary for a ComponentListener.
             *
             * @param e moving of the tile.
             */
            @Override
            public void componentMoved(ComponentEvent e) {
            }

            /**
             * Empty method, necessary for a ComponentListener.
             *
             * @param e showing of the tile.
             */
            @Override
            public void componentShown(ComponentEvent e) {
            }

            /**
             * Empty method, necessary for a ComponentListener.
             *
             * @param e hiding of tile.
             */
            @Override
            public void componentHidden(ComponentEvent e) {
            }
        };
        this.addComponentListener(listener);
    }
}
