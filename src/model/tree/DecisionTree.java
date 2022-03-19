package model.tree;

import model.board.PawnBoard;

import java.util.List;

/**
 * A class creating a tree consisting of nodes and able to evaluate which
 * child is the cheapest/most expensive and therefore to take as next turn.
 * Simulates the decision making of a simple PawnChess-ai.
 */
public class DecisionTree {
    // Starting Model.Tree.Node of with the current board.
    private final Node root;

    /**
     * Constructor to create a new tree with children.
     *
     * @param currentBoard is the current situation and starting point/node.
     * @param diff is the level of difficulty entered by the player and
     *             therefore the maximum height of the tree.
     */
    public DecisionTree(PawnBoard currentBoard, int diff) {
        root = new Node(currentBoard.clone(), null, 0);
        root.createChildren();
        this.setHeight(diff, 1, root);
        setEdges(root);
    }

    /**
     * Recursive method creating children and appending them in the tree.
     *
     * @param difficulty is the maximum height of the tree to not trespass.
     * @param currentHeight is the height of the current leaf.
     * @param parent is the node whose children shall be created and appended.
     */
    private void setHeight(int difficulty, int currentHeight, Node parent) {
        List<Node> currentChildren = parent.getChildren();
        for (Node current : currentChildren) {
            if (currentHeight < difficulty
                    && !current.getBoard().isGameOver()) {
                current.createChildren();
                setHeight(difficulty, currentHeight + 1, current);
            }
        }
    }

    /**
     * Method evaluating recursive bottom up the edges of the tree by adding
     * the either most expensive or cheapest children evaluation to its own
     * evaluation.
     *
     * @param node is the current Model.Tree.Node whose weight has to be set.
     */
    private void setEdges(Node node) {
        List<Node> currentChildren = node.getChildren();
        for (Node current : currentChildren) {
            if (!current.isLeaf()) {
                setEdges(current);
            }
        }
        Node cheapest = currentChildren.get(0);
        for (Node current : currentChildren) {
            if (current.getEvaluation() < cheapest.getEvaluation()) {
                cheapest = current;
            }
        }
        Node costly = currentChildren.get(0);
        for (Node current : currentChildren) {
            if (current.getEvaluation() > costly.getEvaluation()) {
                costly = current;
            }
        }
        // Model.Player.Model.Player or computers turn?
        if (node.getBoard().getNextPlayer().getColor()
                == node.getBoard().getHumanColor()) {
            node.setParentEval(costly);
        } else {
            node.setParentEval(cheapest);
        }
    }

    /**
     * Method to return the best move for the current situation of the board.
     *
     * @return the board with the best outcome/evaluation by performing a
     * certain move.
     */
    public PawnBoard bestMove() {
        Node current;
        // Grab the best/highest rated move from the right to the left pawn.
        current = root.getChildren().get(root.getChildren().size() - 1);
        for (int i = root.getChildren().size() - 2; i >= 0; i--) {
            if (root.getChildren().get(i).getEvaluation()
                    > current.getEvaluation()) {
                current = root.getChildren().get(i);
            }
        }

        return current.getBoard();
    }
}