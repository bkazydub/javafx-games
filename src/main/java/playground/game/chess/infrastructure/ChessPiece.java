package playground.game.chess.infrastructure;

import playground.game.chess.Game;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

public abstract class ChessPiece implements Serializable {

    public enum Color {
        WHITE, BLACK
    }

    protected Color color;
    protected int row;
    protected int col;

    protected boolean moved = false;
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
            destroy(pieces);
            moved = true;
            return true;
        }
        return false;
    }

    protected abstract boolean isMoveValid(int row, int col, List<ChessPiece> pieces, boolean doesMovement);

    public boolean isEnemy(ChessPiece another) {
        return this.color != another.color;
    }

    public double getX() {
        return col * Game.CELL_SIZE;
    }

    public double getY() {
        return row * Game.CELL_SIZE;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " row = " + row + ", col = " + col;
    }

    // The method to be called after the move IS validated.
    protected void destroy(List<ChessPiece> pieces) {
        Iterator<ChessPiece> iter = pieces.iterator();
        while (iter.hasNext()) {
            ChessPiece piece = iter.next();
            if (this == piece) continue;
            if (piece.row == this.row && piece.col == this.col)
                iter.remove();
        }
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
}
