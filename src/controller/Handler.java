package controller;

import view.BoardTile;
import view.GUIView;

import javax.swing.SwingUtilities;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Class implementing ActionListener monitoring the interactions of the user
 * with the view representing the game and calling the fitting methods for the
 * interaction.
 */
public class Handler implements ActionListener {
    private final Controller controller;
    private final GUIView view;

    /**
     * Basic Constructor class.
     *
     * @param controller the controller managing view and model.
     * @param view the representation for the user.
     */
    public Handler(Controller controller, GUIView view) {
        this.controller = controller;
        this.view = view;
    }

    /**
     * Checking which action has been performed by the user and calling the
     * fitting method of the controller.
     *
     * @param e the interaction of the user.
     */
    @Override
    public void actionPerformed(ActionEvent e) {

        // Change of difficulty, clicking on the board or using the menu?
        if (e.getSource() == view.getDifficulty()) {
            Integer entered = (Integer) view.getDifficulty().getSelectedItem();
            controller.changeDifficulty(entered);
        } else if (e.getSource().getClass() == BoardTile.class) {
            BoardTile selected = (BoardTile) e.getSource();
            if (selected.isOccupied()) {
                controller.killCurrentSwingWorker();
                controller.pawnSelected(selected);
            } else {
                controller.killCurrentSwingWorker();
                controller.emptyFieldSelected(selected);
            }
        } else {
            String name = e.getActionCommand();
            switch (name) {
                case "New Game" -> {
                    controller.killCurrentSwingWorker();
                    controller.startNewGame();
                }
                case "Switch Colors" -> {
                    controller.killCurrentSwingWorker();
                    controller.switchColors();
                }
                case "Undo" -> {
                    controller.killCurrentSwingWorker();
                    controller.undoMove();
                }
                case "Quit" -> {
                    System.exit(0);
                }
                default -> {
                    SwingUtilities.invokeLater(() -> view.popUp("This click "
                            + "is not supported yet!"));
                }
            }
        }
    }
}
