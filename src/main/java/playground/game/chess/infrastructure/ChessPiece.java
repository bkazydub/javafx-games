package playground.game.chess.infrastructure;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public abstract class ChessPiece implements Serializable {

    public enum Color {
        WHITE, BLACK
    }

    protected Color color;
    protected int row;
    protected int col;

    // Used to check whether piece moved or not
    protected boolean moved = false;

    // If set to true marks piece as inactive
    protected boolean captured = false;

    // The property is intended to be used as a marker
    // for specific reasons (e.g. the piece checking a king).
    // It is not required.
    protected boolean highlighted = false;

    public ChessPiece(Color color, int row, int col) {
        this.color = color;
        this.row = row;
        this.col = col;
    }

    public boolean move(int row, int col, List<ChessPiece> pieces) {
        if (isMoveValid(row, col, pieces, true)) {
            this.row = row;
            this.col = col;
            capture(pieces);
            moved = true;
            return true;
        }
        return false;
    }

    // Determine if the move is valid.
    protected abstract boolean isMoveValid(int row, int col, List<ChessPiece> pieces, boolean doesMovement);

    public boolean isEnemy(ChessPiece another) {
        return this.color != another.color;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " row = " + row + ", col = " + col;
    }

    /**
     * The method to be called after the move IS validated.
     * Capture the piece - remove it from the board.
     */
    protected void capture(List<ChessPiece> pieces) {
        for (ChessPiece piece : pieces) {
            if (this == piece || piece.captured) continue;
            if (piece.row == this.row && piece.col == this.col) {
                piece.captured = true;
                break;
            }
        }
    }

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

    public int getCol() {
        return col;
    }

    public int getRow() {
        return row;
    }

    public List<Move> availableMoves(List<ChessPiece> pieces) {
        List<Move> moves = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (isMoveValid(i, j, pieces, true)) moves.add(new Move(row, col, i, j));
            }
        }
        return moves;
    }
}
