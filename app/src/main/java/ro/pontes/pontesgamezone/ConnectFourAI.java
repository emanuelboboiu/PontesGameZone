package ro.pontes.pontesgamezone;

public class ConnectFourAI {

    // The AI does think MAX_DEPTH moves ahead.
    public static int MAX_DEPTH = ConnectFourActivity.cfLevel;

    // The score given to a state that leads to a win:
    static final float WIN_REVENUE = 1f;

    // The score given to a state that leads to a lose:
    static final float LOSE_REVENUE = -1f;

    // The score given to a state that leads to a loss in the next turn:
    static final float UNCERTAIN_REVENUE = 0f;

    Board board;

    // Constructor takes as parameter a Board object:
    public ConnectFourAI(Board board) {
        this.board = board;
    } // end constructor.

    // Method which makes a turn.
    // @return The column where the turn was made.
    // Please note that the turn was already made and doesn't have to be done
    // again:
    public int makeTurn() {
        double maxValue = 2. * Integer.MIN_VALUE;
        int move = 0;

        // Search all columns for the one that has the best value
        // The best score possible is WIN_REVENUE.
        // So if we find a move that has this score, the search can be stopped.
        for (int column = 0; column < board.getWidth(); column++) {
            if (board.isValidMove(column)) {
                // Compare the score of this particular move with the previous
                // max
                double value = moveValue(column);
                if (value > maxValue) {
                    maxValue = value;
                    move = column;
                    if (value == WIN_REVENUE) {
                        break;
                    }
                }
            }
        }
        // Make the move
        board.makeMoveAI(move);
        return move;
    }

    double moveValue(int column) {
        // To determine the value of a move, first make the move, estimate that
        // state and then undo the move again.
        board.makeMoveAI(column);
        double val = alphabeta(MAX_DEPTH, Integer.MIN_VALUE, Integer.MAX_VALUE, false);
        board.undoMoveAI(column);
        return val;
    }

    double alphabeta(int depth, double alpha, double beta, boolean maximizingPlayer) {
        boolean hasWinner = board.hasWinner();
        // All these conditions lead to a termination of the recursion
        if (depth == 0 || hasWinner) {
            double score;
            if (hasWinner) {
                score = board.playerIsWinner() ? LOSE_REVENUE : WIN_REVENUE;
            } else {
                score = UNCERTAIN_REVENUE;
            }
            // Note that depth in this implementation starts at a high value and
            // is decreased in every recursive call. /
            // This means that the deeper the recursion is, the greater
            // MAX_DEPTH - depth will become and thus the smaller the result
            // will become.
            // This is done as a tweak, simply spoken, something bad happening
            // in the next turn is worse than it happening in let's say five
            // steps.
            // Analogously something good happening in the next turn is better
            // than it happening in five steps.
            return score / (MAX_DEPTH - depth + 1);
        }

        if (maximizingPlayer) {
            for (int column = 0; column < board.getWidth(); column++) {
                if (board.isValidMove(column)) {
                    board.makeMoveAI(column);
                    alpha = Math.max(alpha, alphabeta(depth - 1, alpha, beta, false));
                    board.undoMoveAI(column);
                    if (beta <= alpha) {
                        break;
                    }
                }
            }
            return alpha;
        } else {
            for (int column = 0; column < board.getWidth(); column++) {
                if (board.isValidMove(column)) {
                    board.makeMovePlayer(column);
                    beta = Math.min(beta, alphabeta(depth - 1, alpha, beta, true));
                    board.undoMovePlayer(column);
                    if (beta <= alpha) {
                        break;
                    }
                }
            }
            return beta;
        }
    }

} // end connect four AI class.
