package playground.game.chess.infrastructure;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * The base class for all types of pieces.
 */
public abstract class ChessPiece implements Serializable {

    public enum Color {
        WHITE, BLACK
    }

    protected Color color;
    protected int rank;
    protected int file;

    // Used to check whether the piece has moved
    protected boolean moved = false;

    // If set to true marks piece as inactive.
    // Inactive pieces are ignored.
    protected boolean captured = false;

    // The property is intended to be used as a marker
    // for specific reasons (e.g. the piece checking a king).
    // It is not required.
    protected boolean highlighted = false;

    public ChessPiece(Color color, int rank, int file) {
        this.color = color;
        this.rank = rank;
        this.file = file;
    }

    /**
     * Try moving this piece to specified {@code rank} and {@code file}. If {@link #isMoveValid(int, int, List, boolean)}
     * returns true the move is performed.
     * @param rank a rank to move the piece to
     * @param file a file to move the piece to
     * @param pieces all the pieces currently in the game
     * @return true if the move is legal and was performed
     */
    public boolean move(int rank, int file, List<ChessPiece> pieces) {
        if (isMoveValid(rank, file, pieces, true)) {
            this.rank = rank;
            this.file = file;
            capture(pieces);
            moved = true;
            return true;
        }
        return false;
    }

    /**
     * Determine if the move is valid.
     * @param row a rank this piece to be moved to
     * @param col a file this piece to be moved to
     * @param pieces all the pieces currently in the game
     * @param doesMovement flag which indicates whether this piece is the one
     *                     actually moving. {@code doesMovement == false} if
     *                     this is some sort of check (like determining if a king is being checked).
     * @return true if move does not violate chess rules
     */
    protected abstract boolean isMoveValid(int row, int col, List<ChessPiece> pieces, boolean doesMovement);

    /**
     * Check if the piece is of opposite color compared to another.
     * @param another a piece to check against
     * @return true if {@code this} and {@code another} pieces are of different colors
     */
    public boolean isEnemy(ChessPiece another) {
        return this.color != another.color;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " rank = " + rank + ", file = " + file;
    }

    /**
     * Capture the piece - render it inactive.
     * The method to be called after the move IS validated.
     */
    protected void capture(List<ChessPiece> pieces) {
        for (ChessPiece piece : pieces) {
            if (this == piece || piece.captured) continue;
            if (piece.rank == this.rank && piece.file == this.file) {
                piece.captured = true;
                break;
            }
        }
    }

    /**
     * Shows if the piece has been captured. Captured pieces do not participate in the game.
     * @return true if it was captured
     */
    public boolean isCaptured() {
        return captured;
    }

    public boolean isHighlighted() {
        return highlighted;
    }

    public void setHighlighted(boolean highlighted) {
        this.highlighted = highlighted;
    }

    public Color getColor() {
        return color;
    }

    /**
     * Get file the piece is currently on
     * @return current file
     */
    public int getFile() {
        return file;
    }

    /**
     * Get rank the piece is currently on
     * @return current rank
     */
    public int getRank() {
        return rank;
    }

    /**
     * Show all the available moves for this piece.
     * @param pieces all the pieces
     * @return list of available moves
     */
    public List<Move> availableMoves(List<ChessPiece> pieces) {
        List<Move> moves = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (isMoveValid(i, j, pieces, true)) moves.add(new Move(rank, file, i, j));
            }
        }
        return moves;
    }
}
