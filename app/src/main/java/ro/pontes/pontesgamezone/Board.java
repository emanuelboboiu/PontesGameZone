package ro.pontes.pontesgamezone;

public class Board {

    /**
     * Disc identifiers
     */
    public final byte NOBODY = 0;
    public final byte PLAYER = 1;
    public final byte AI = 2;

    /**
     * Height of the board
     */
    int height;

    /**
     * Width of the board
     */
    int width;

    /**
     * How many stones have to be connected in order to win
     */
    int winLength;

    /**
     * The board that stores the status of every cell
     */
    byte[][] board;

    /**
     * Keeps track of the number of discs stacked into one column. This is used
     * for efficiency reasons to be able to quickly determine if a player can
     * put his disc into a column.
     */
    int[] columnCounts;

    /**
     * Constructor
     *
     * @param winLength How many stones have to be connected in order to win
     */
    public Board(int height, int width, int winLength) {
        this.height = height;
        this.width = width;
        this.winLength = winLength;

        this.board = new byte[width][height];
        this.columnCounts = new int[width];
    } // end constructor.

    /**
     * @param column Number of the column to check. Index starts at 0
     * @return True if any of the players can put a disc into the given column
     */
    public boolean isValidMove(int column) {
        return columnCounts[column] < height;
    }

    /**
     * Puts player disc into the given column. If the move is invalid, the board
     * remains unchanged.
     */
    public void makeMovePlayer(int column) {
        makeMove(column, true);
    }

    /**
     * Puts AI disc into the given column. If the move is invalid, the board
     * remains unchanged.
     */
    public void makeMoveAI(int column) {
        makeMove(column, false);
    }

    /**
     * Removes Player disc from the given column. If the move is invalid (the
     * top disc is of another player or there are no discs in this column) the
     * board remains unchanged.
     */
    public void undoMovePlayer(int column) {
        undoMove(column, true);
    }

    /**
     * Removes AI disc from the given column. If the move is invalid (the top
     * disc is of another player or there are no discs in this column) the board
     * remains unchanged.
     */
    public void undoMoveAI(int column) {
        undoMove(column, false);
    }

    void makeMove(int column, boolean player) {
        if (columnCounts[column] < height) {
            byte sign = player ? PLAYER : AI;
            board[column][columnCounts[column]++] = sign;
        }
    }

    void undoMove(int column, boolean player) {
        if (columnCounts[column] > 0) {
            byte sign = player ? PLAYER : AI;
            if (board[column][columnCounts[column] - 1] == sign) {
                board[column][columnCounts[column] - 1] = NOBODY;
                columnCounts[column]--;
            }
        }
    }

    /**
     * Returns the width of the board.
     */
    public int getWidth() {
        return width;
    }

    public boolean hasWinner() {
        return getWinner() != NOBODY;
    }

    /**
     * Returns the winner of the current board state.
     *
     * @return Board.NOBODY if there is no winner. Board.AI if the AI is the
     * winner. Board.PLAYER if the player is the winner.
     */
    public byte getWinner() {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y <= height - winLength; y++) {
                boolean playerWin = true;
                boolean aiWin = true;
                for (int o = 0; o < winLength; o++) {
                    if (playerWin && board[x][y + o] != PLAYER) {
                        playerWin = false;
                    }
                    if (aiWin && board[x][y + o] != AI) {
                        aiWin = false;
                    }
                }
                if (playerWin) {
                    return PLAYER;
                } else if (aiWin) {
                    return AI;
                }
            }
        }

        for (int y = 0; y < height; y++) {
            for (int x = 0; x <= width - winLength; x++) {
                boolean playerWin = true;
                boolean aiWin = true;
                for (int o = 0; o < winLength; o++) {
                    if (playerWin && board[x + o][y] != PLAYER) {
                        playerWin = false;
                    }
                    if (aiWin && board[x + o][y] != AI) {
                        aiWin = false;
                    }
                }
                if (playerWin) {
                    return PLAYER;
                } else if (aiWin) {
                    return AI;
                }
            }
        }

        for (int x = 0; x <= width - winLength; x++) {
            for (int y = 0; y <= height - winLength; y++) {
                boolean playerWin = true;
                boolean aiWin = true;
                for (int o = 0; o < winLength; o++) {
                    if (playerWin && board[x + o][y + o] != PLAYER) {
                        playerWin = false;
                    }
                    if (aiWin && board[x + o][y + o] != AI) {
                        aiWin = false;
                    }
                }
                if (playerWin) {
                    return PLAYER;
                } else if (aiWin) {
                    return AI;
                }
            }
        }

        for (int x = width - 1; x >= winLength - 1; x--) {
            for (int y = 0; y <= height - winLength; y++) {
                boolean playerWin = true;
                boolean aiWin = true;
                for (int o = 0; o < winLength; o++) {
                    if (playerWin && board[x - o][y + o] != PLAYER) {
                        playerWin = false;
                    }
                    if (aiWin && board[x - o][y + o] != AI) {
                        aiWin = false;
                    }
                }
                if (playerWin) {
                    return PLAYER;
                } else if (aiWin) {
                    return AI;
                }
            }
        }

        return NOBODY;
    }

    /**
     * Returns if the player has won.
     */
    public boolean playerIsWinner() {
        return getWinner() == PLAYER;
    }

    /**
     * Returns if the board is full and there is no winner.
     */
    public boolean isTie() {
        return isBoardFull() && getWinner() == NOBODY;
    }

    boolean isBoardFull() {
        boolean emptyColumnFound = false;
        for (int x = 0; x < width; x++) {
            if (columnCounts[x] < height) {
                emptyColumnFound = true;
                break;
            }
        }
        return !emptyColumnFound;
    }

} // end Board class.
