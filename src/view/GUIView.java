package view;

import model.board.Board;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.InputMap;
import javax.swing.ComponentInputMap;
import javax.swing.ActionMap;
import javax.swing.AbstractAction;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.plaf.ActionMapUIResource;
import javax.swing.JButton;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import java.awt.GridLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

/**
 * Class presenting the game viewable to the user, providing interactive
 * elements.
 */
public class GUIView {
    private JFrame frame;
    private JPanel panel;
    private JPanel gameBoardPanel;
    private final BoardTile[][] board;
    private final JToolBar tools;
    private JButton[] toolBarButtons;
    private JComboBox<Integer> difficulty;
    private JLabel pawnCounterWhite;
    private JLabel pawnCounterBlack;

    /**
     * Class constructor setting up the view.
     */
    public GUIView() {
        board = new BoardTile[Board.SIZE][Board.SIZE];
        tools = new JToolBar();
        setToolBar();
        setBoard();
        setPanel();
        setFrame();
    }

    /**
     * Setting up the menu/toolbar.
     */
    private void setToolBar() {
        Border emptyBorder = BorderFactory.createEmptyBorder(10, 10, 10, 10);
        tools.setFloatable(false);
        toolBarButtons = new JButton[4];
        JButton button = new JButton("New Game");
        button.setBorder(emptyBorder);
        tools.add(button);
        toolBarButtons[0] = button;

        button = new JButton("Switch Colors");
        button.setBorder(emptyBorder);
        tools.add(button);
        toolBarButtons[1] = button;

        button = new JButton("Undo");
        button.setBorder(emptyBorder);
        button.setEnabled(false);
        tools.add(button);
        toolBarButtons[2] = button;

        JLabel level = new JLabel("Level: ", SwingConstants.CENTER);
        Integer[] difficulties
                = new Integer[] {1, 2, 3, 4, 5};
        difficulty = new JComboBox<>(difficulties);
        difficulty.setSelectedItem(null);
        difficulty.setMaximumSize(button.getMaximumSize());
        tools.add(level);
        tools.add(difficulty);

        button = new JButton("Quit");
        button.setBorder(emptyBorder);
        tools.add(button);
        toolBarButtons[3] = button;
        addKeyStrokeListeners();
    }

    /**
     * Setting up listeners to perform actions if a certain combination of
     * keys is pressed (shortcuts).
     */
    private void addKeyStrokeListeners() {
        String[] actionNames = new String[] {"action_new", "action_switch",
                "action_undo", "action_exit"};
        int[] events = new int[] {KeyEvent.VK_N, KeyEvent.VK_S, KeyEvent.VK_U,
                KeyEvent.VK_Q};
        InputMap keyMap = new ComponentInputMap(tools);
        ActionMap actionMap = new ActionMapUIResource();

        for (int i = 0; i < actionNames.length; i++) {
            int finalI = i;
            actionMap.put(actionNames[i], new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    toolBarButtons[finalI].doClick();
                }
            });
            keyMap.put(KeyStroke.getKeyStroke(events[i],
                    InputEvent.ALT_DOWN_MASK), actionNames[i]);
        }
        SwingUtilities.replaceUIActionMap(tools, actionMap);
        SwingUtilities.replaceUIInputMap(tools,
                JComponent.WHEN_IN_FOCUSED_WINDOW, keyMap);
    }

    /**
     * Method creating/setting up the board with its colored squares and
     * rulers on the sides utilising a GridLayout.
     */
    private void setBoard() {
        gameBoardPanel = new JPanel();
        gameBoardPanel.setBorder(BorderFactory.createEmptyBorder());
        gameBoardPanel.setLayout(new GridLayout(Board.SIZE + 2, Board.SIZE + 2,
                0, 0));
        fillRulers(true);
        fillTiles();
        fillRulers(false);
    }

    /**
     * Filling the tiles of the GirdLayout with BoardTiles.
     */
    private void fillTiles() {
        Color brownTile = new Color(153, 102, 0);
        Color greyTile = new Color(204, 204, 204);
        BoardTile current;
        int colorCounter = 0;
        for (int i = 0; i < Board.SIZE; i++) {
            for (int j = 0; j <= Board.SIZE + 1; j++) {
                if (j == 0 || j == Board.SIZE + 1) {
                    JLabel ruling = new JLabel(String.valueOf(i + 1),
                            SwingConstants.CENTER);
                    setGrayBackground(ruling);
                    gameBoardPanel.add(ruling);
                } else {
                    if (colorCounter % 2 == 0) {
                        current = new BoardTile(brownTile, false, null);
                    } else {
                        current = new BoardTile(greyTile, false, null);
                    }
                    colorCounter++;
                    current.setActionCommand(i + " " + (j - 1));
                    gameBoardPanel.add(current);
                    board[j - 1][i] = current;
                }
            }
            colorCounter++;
        }
    }

    /**
     * Creating the rulers on the top and bottom of the board and adding two
     * "pawncounters" for each player to the top left and right.
     *
     * @param addCounters true if counters shall be created, false otherwise.
     */
    private void fillRulers(boolean addCounters) {
        for (int i = 0; i <= Board.SIZE + 1; i++) {
            JLabel ruling;
            if (i == 0) {
                if (addCounters) {
                    pawnCounterWhite = new JLabel(String.valueOf(Board.SIZE),
                            SwingConstants.CENTER);
                    pawnCounterWhite.setForeground(Color.WHITE);
                    pawnCounterWhite.setFont(new Font("", Font.BOLD, 20));
                    setGrayBackground(pawnCounterWhite);
                    gameBoardPanel.add(pawnCounterWhite);
                } else {
                    ruling = new JLabel("");
                    setGrayBackground(ruling);
                    gameBoardPanel.add(ruling);
                }
            } else if (i == Board.SIZE + 1) {
                if (addCounters) {
                    pawnCounterBlack = new JLabel(String.valueOf((Board.SIZE)),
                            SwingConstants.CENTER);
                    pawnCounterBlack.setForeground(Color.BLACK);
                    pawnCounterBlack.setFont(new Font("", Font.BOLD, 20));
                    setGrayBackground(pawnCounterBlack);
                    gameBoardPanel.add(pawnCounterBlack);
                } else {
                    ruling = new JLabel("");
                    ruling.setFont(new Font("", Font.BOLD,
                            ruling.getHeight() / 2));
                    setGrayBackground(ruling);
                    gameBoardPanel.add(ruling);
                }
            } else {
                ruling = new JLabel(String.valueOf(i), SwingConstants.CENTER);
                setGrayBackground(ruling);
                gameBoardPanel.add(ruling);
            }
        }
    }

    /**
     * Method creating the frame to be displayed and setting it up.
     */
    private void setFrame() {
        frame = new JFrame();
        frame.add(panel, BorderLayout.CENTER);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setTitle("PawnChess");
        frame.setMinimumSize(new Dimension(600, 600));
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    /**
     * Setting up the main panel holding the board in the frame.
     */
    private void setPanel() {
        panel = new JPanel();
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        panel.setLayout(new BorderLayout(0, 0));
        panel.add(tools, BorderLayout.PAGE_START);
        panel.add(gameBoardPanel);
        panel.setPreferredSize(new Dimension(300, 300));
    }

    /**
     * Outsourced method changing the background of a given JLabel to
     * Color.Gray.
     *
     * @param label is the label whose background shall be changed.
     */
    private void setGrayBackground(JLabel label) {
        label.setOpaque(true);
        label.setBackground(Color.gray);
    }

    /**
     * Method equipping each BoardTile of the GridLayout to enable
     * interactions with it.
     *
     * @param listener is the ActionListener monitoring the tiles.
     */
    public void addListenerBoard(ActionListener listener) {
        for (JButton[] currentArray : board) {
            for (JButton currentButton : currentArray) {
                currentButton.addActionListener(listener);
            }
        }
    }

    /**
     * Equipping the menu/toolbar with a given ActionListener to enable
     * interactions with them.
     *
     * @param listener the ActionListener to monitor.
     */
    public void addListenerMenu(ActionListener listener) {
        for (JButton current : toolBarButtons) {
            current.addActionListener(listener);
        }
        difficulty.addActionListener(listener);
    }

    /**
     * Changes the currently displayed pawns according to a given Array with
     * the new positions.
     *
     * @param board the 2D Array now to be displayed.
     */
    public void placePawns(char[][] board) {
        for (int i = 0; i < board.length; i++) {
            char[] currentCol = board[i];
            for (int j = 0; j < currentCol.length; j++) {
                char currentTile = currentCol[j];
                if (currentTile == 'W' || currentTile == 'B') {
                    this.board[i][j].moveNewPawnOnto(currentTile);
                } else {
                    this.board[i][j].moveNewPawnOnto(' ');
                }
            }
        }
    }

    /**
     * Highlights or "unhighlights" a certain field by either setting a green
     * border around it, or an empty border.
     *
     * @param tile the tile whose border shall be changed.
     * @param bool true if the tile is to highlight, false otherwise.
     */
    public void highlightField(BoardTile tile, boolean bool) {
        if (bool) {
            tile.highlight();
        } else {
            tile.unhighlight();
        }
    }

    /**
     * Method enabling all BoardTiles with pawns of the human player
     * on them for interacting with them.
     *
     * @param toEnable is the letter of the color of the human pawns.
     */
    public void enableHumanPawns(char toEnable) {
        Color toCheck;
        if (toEnable == 'W') {
            toCheck = Color.WHITE;
        } else {
            toCheck = Color.BLACK;
        }
        for (BoardTile[] currentArray : board) {
            for (BoardTile tile : currentArray) {
                if (tile.getPawnColor() == toCheck) {
                    tile.setEnabled(true);
                }
            }
        }
    }

    /**
     * Method disabling all BoardTiles with pawns of the human player
     * on them to forbid interacting with them.
     *
     * @param toDisable is the letter of the color of the human pawns.
     */
    public void disableHumanPawns(char toDisable) {
        Color toCheck;
        if (toDisable == 'W') {
            toCheck = Color.WHITE;
        } else {
            toCheck = Color.BLACK;
        }
        for (BoardTile[] currentArray : board) {
            for (BoardTile tile : currentArray) {
                if (tile.getPawnColor() == toCheck) {
                    tile.setEnabled(false);
                }
            }
        }
    }

    /**
     * Method disabling all BoardTiles with pawns of the computer on them to
     * forbid interacting with them.
     *
     * @param humanChar is the letter of the color of the human pawns.
     */
    public void disableComputerPawns(char humanChar) {
        Color toCheck;
        if (humanChar == 'W') {
            toCheck = Color.BLACK;
        } else {
            toCheck = Color.WHITE;
        }
        for (BoardTile[] currentArray : board) {
            for (BoardTile tile : currentArray) {
                if (tile.getPawnColor() == toCheck) {
                    tile.setEnabled(false);
                }
            }
        }
    }

    /**
     * Method enabling all BoardTiles with pawns of the computer on them for
     * interacting with them.
     *
     * @param humanChar is the letter of the color of the human pawns.
     */
    public void enableComputerPawns(char humanChar) {
        Color toCheck;
        if (humanChar == 'W') {
            toCheck = Color.BLACK;
        } else {
            toCheck = Color.WHITE;
        }
        for (BoardTile[] currentArray : board) {
            for (BoardTile tile : currentArray) {
                if (tile.getPawnColor() == toCheck) {
                    tile.setEnabled(true);
                }
            }
        }
    }

    /**
     * Method changing the status of all BoardTiles from enabled to disabled,
     * or vice-versa.
     *
     * @param toEnable true if the tiles shall be enabled, false otherwise.
     */
    public void switchAll(boolean toEnable) {
        for (BoardTile[] currentArray : board) {
            for (BoardTile tile : currentArray) {
                tile.setEnabled(toEnable);
            }
        }
    }

    /**
     * Moves one pawn from one BoardTile to another.
     *
     * @param from the tile the pawn originates.
     * @param to the targeted location.
     */
    public void movePawn(BoardTile from, BoardTile to) {
        char pawnColor;
        if (from.getPawnColor() == Color.WHITE) {
            pawnColor = 'W';
        } else {
            pawnColor = 'B';
        }
        to.moveNewPawnOnto(pawnColor);
        from.moveNewPawnOnto(' ');
    }

    /**
     * Method returning the currently selected Item of the
     * JComboBox/selected difficulty.
     *
     * @return the currently selected Integer.
     */
    public JComboBox<Integer> getDifficulty() {
        return difficulty;
    }

    /**
     * Returns all BoardTiles, to able to locate desired moves by the player.
     *
     * @return the 2D Array with all BoardTiles.
     */
    public BoardTile[][] getBoard() {
        return board;
    }

    /**
     * Opens a PopUp via JOptionPane to inform the user what went wrong.
     *
     * @param errorMessage telling the user what went wrong/what to do.
     */
    public void errorPopUp(String errorMessage) {
        JOptionPane.showMessageDialog(frame, "Error! " + errorMessage);
    }

    /**
     * Opens a PopUp via JOptionPane informing the user about an issue.
     *
     * @param message to be displayed to the user.
     */
    public void popUp(String message) {
        JOptionPane.showMessageDialog(frame, message);
    }

    /**
     * Updates the "pawncounter" so it correctly displays the amount of pawns
     * of each player currently standing on the board.
     *
     * @param whitePawns the current amount of white pawns.
     * @param blackPawns the current amount of black pawns.
     */
    public void decreasePawnCounter(int whitePawns, int blackPawns) {
        pawnCounterWhite.setText(String.valueOf(whitePawns));
        pawnCounterBlack.setText(String.valueOf(blackPawns));
    }

    /**
     * Making the "undo" button available to click for the user, or locking
     * the usage of it.
     *
     * @param toEnable true if the button shall be clickable, false otherwise.
     */
    public void enableUndo(boolean toEnable) {
        toolBarButtons[2].setEnabled(toEnable);
    }

    /**
     * Pops up a MessageDialog signalising the user he has to skip his turn.
     */
    public void skipTurnPopUp() {
        JOptionPane.showMessageDialog(frame, "You have to skip!");
    }
}