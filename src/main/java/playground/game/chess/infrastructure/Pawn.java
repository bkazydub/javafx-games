package playground.game.chess.infrastructure;

import java.util.Iterator;
import java.util.List;

/**
 * Created by dragonmf on 4/17/16.
 */
public class Pawn extends ChessPiece {

    private final int startRow;

    public Pawn(Color color, int row, int col) {
        super(color, row, col);
        startRow = row;
    }

    @Override
    protected boolean isMoveValid(int row, int col, List<ChessPiece> pieces, boolean doesMovement) {
            if (Math.abs(col - this.col) == 1) {
                Iterator<ChessPiece> iter = pieces.iterator();
                while (iter.hasNext()) {
                    ChessPiece piece = iter.next();
                    if (this == piece) {
                        continue;
                    }
                    if (row - this.row == (this.startRow == 1 ? 1 : -1)) {
                        if (piece.row == row && piece.col == col) {
                            if (isEnemy(piece)) {
                                if (doesMovement)
                                    return MoveValidator.validateComplete(this, MoveValidator.getKing(this.color, pieces), pieces, row, col);
                                else return MoveValidator.validate(this, pieces, row, col);
                            }
                        }
                    }
                }
            }

            if (col - this.col != 0)
                return false;

            Iterator<ChessPiece> iter = pieces.iterator();
            while (iter.hasNext()) {
                ChessPiece piece = iter.next();
                // disable moving to occupied cells.
                if (piece.row == row && piece.col == col)
                    return false;
                // disable 'jumping' over other pieces
                if (piece.row == (this.startRow == 1 ? this.row + 1 : this.row - 1) && this.col == piece.col)
                    return false;
            }

            if (startRow == 1) {
                if (row - this.row > 0 && row - this.row < (moved ? 2 : 3)) {
                    if (doesMovement)
                        return MoveValidator.validateComplete(this, MoveValidator.getKing(this.color, pieces), pieces, row, col);
                    else return MoveValidator.validate(this, pieces, row, col);
                }
            } else if (startRow == 6) {
                if (row - this.row < 0 && row - this.row > (moved ? -2 : -3)) {
                    if (doesMovement)
                        return MoveValidator.validateComplete(this, MoveValidator.getKing(this.color, pieces), pieces, row, col);
                    else return MoveValidator.validate(this, pieces, row, col);
                }
            }

            return false;
    }

    public ChessPiece convertToRook() {
        return new Rook(color, row, col);
    }

    public ChessPiece convertToKnight() {
        return new Knight(color, row, col);
    }

    public ChessPiece convertToBishop() {
        return new Bishop(color, row, col);
    }

    public ChessPiece convertToQueen() {
        return new Queen(color, row, col);
    }

    public boolean reachedLastRow() {
        return startRow == 1 ? row == 7 : row == 0;
    }
}
