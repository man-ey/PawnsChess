package model.tree;

import model.board.PawnBoard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Class for a single Node in the tree evaluating/finding the best next move.
 * Each Node has a different Model.Board/Situation assigned on whose basis the
 * Node gets evaluated.
 */
public class Node {
    private final PawnBoard board;
    private final List<Node> children;
    private double evaluation;
    private final double depth;
    private boolean isLeaf;

    /**
     * Constructor creating a new Node.
     *
     * @param brd the board/variation of this node.
     * @param depth the depth in the tree of this node.
     * @param parent of this Node in the tree, null if it is the root parent.
     */
    public Node(PawnBoard brd, Node parent, double depth) {
        this.board = brd.clone();
        this.children = new ArrayList<>();
        this.evaluation = evaluateBoard();
        this.depth = depth;
        isLeaf = parent != null;
    }

    /**
     * Getter method.
     *
     * @return the evaluation assigned/calculated.
     */
    public double getEvaluation() {
        return evaluation;
    }

    /**
     * Method creating new Nodes and assigning them to this Node as children.
     */
    public void createChildren() {
        HashMap<List<Integer>, List<int[]>> availableOptions =
                board.viablePawnMoves(board.getCurrentPlayer());
        List<int[]> pawns
                = board.getPawnsFor(board.getCurrentPlayer().getColor());
        List<int[]> currentMoves;
        Node newChild;

        // Creating a new child for each possible move of each pawn.
        for (int[] currentPawn : pawns) {
            List<Integer> currentKey = new ArrayList<>();
            currentKey.add(currentPawn[0]);
            currentKey.add(currentPawn[1]);
            currentMoves = availableOptions.get(currentKey);
            if (currentMoves != null) {
                for (int[] currentMove : currentMoves) {
                    PawnBoard cloned = board.clone();
                    cloned = cloned.moving(currentPawn[0], currentPawn[1],
                            currentMove[0], currentMove[1]);
                    newChild = new Node(cloned, this, depth + 1);
                    children.add(newChild);
                }
            }
        }
        isLeaf = children.size() == 0;
    }

    /**
     * Getter method.
     *
     * @return the assigned board.
     */
    public PawnBoard getBoard() {
        return board;
    }

    /**
     * Getter method.
     *
     * @return the children of this Node.
     */
    public List<Node> getChildren() {
        return children;
    }

    /**
     * Getter method for the boolean value stating if this Node is a leaf in
     * the tree or not.
     *
     * @return true if it is a leaf, false otherwise.
     */
    public boolean isLeaf() {
        return isLeaf;
    }

    /**
     * Method evaluating the board by calling an Evaluator with the assigned
     * board.
     *
     * @return the calculated value for the given Model.Board.
     */
    private double evaluateBoard() {
        Evaluator evaluator =
                new Evaluator(board, depth);
        return evaluator.evaluateBoard();
    }

    /**
     * Method increasing the evaluation by adding to its amount the
     * evaluation of the given child-Model.Tree.Node in a bottom-up way.
     *
     * @param child the Model.Tree.Node whose value has to be added.
     */
    public void setParentEval(Node child) {
        this.evaluation = evaluation + child.getEvaluation();
    }
}