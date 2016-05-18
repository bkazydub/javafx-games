package playground.game.chess.infrastructure;

import java.io.Serializable;

/**
 * The class is used to represent piece's move.
 * Instances of the class are used as DTO over socket connection for multiplayer mode.
 */
public class Move implements Serializable {

    private final int row;
    private final int col;
    private final int newRow;
    private final int newCol;

    private ChessPiece promotedTo;
    private boolean capture;

    public Move(int row, int col, int newRow, int newCol) {
        this.row = row;
        this.col = col;
        this.newRow = newRow;
        this.newCol = newCol;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public int getNewRow() {
        return newRow;
    }

    public int getNewCol() {
        return newCol;
    }

    public ChessPiece getPromotedTo() {
        return promotedTo;
    }

    public void setPromotedTo(ChessPiece promotedTo) {
        this.promotedTo = promotedTo;
    }

    public boolean isCapture() {
        return capture;
    }

    public void setCapture(boolean capture) {
        this.capture = capture;
    }
}
