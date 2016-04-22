package playground.game.chess.infrastructure;

import java.util.Iterator;
import java.util.List;

/**
 * Created by dragonmf on 4/17/16.
 */
public class King extends ChessPiece {

    public King(Color color, int row, int col) {
        super(color, row, col);
    }

    @Override
    protected boolean isMoveValid(int row, int col, List<ChessPiece> pieces, boolean doesMovement) {
            if ((Math.abs(this.col - col) == 1 && this.row == row)
                    || (Math.abs(this.row - row) == 1 && this.col == col)
                    || (Math.abs(this.row - row) == 1 && Math.abs(this.col - col) == 1)) {
                Iterator<ChessPiece> iter = pieces.iterator();
                while (iter.hasNext()) {
                    ChessPiece piece = iter.next();
                    if (this == piece) continue;
                    if (piece.col == col && piece.row == row) {
                        if (isEnemy(piece)) {
                            boolean valid = MoveValidator.validate(this, pieces, row, col);
                            return doesMovement ? (MoveValidator.validateComplete(this, this, pieces, row, col) && valid) : MoveValidator.validate(this, pieces, row, col);
                        } else {
                            return false;
                        }
                    }
                }
                if (doesMovement)
                    return MoveValidator.validateComplete(this, this, pieces, row, col);
                else return MoveValidator.validate(this, pieces, row, col);

            } else if (Math.abs(this.col - col) == 2 && this.row == row) {

                if (!doesMovement)
                    return false;

                if (!MoveValidator.validateNotChecked(this, pieces)) return false;

                if (moved) return false;
                Iterator<ChessPiece> iter = pieces.iterator();
                while (iter.hasNext()) {
                    ChessPiece piece = iter.next();
                    if (piece.row == this.row && (piece.col == this.col + 1 || piece.col == this.col + 2) && col > this.col) {
                        return false;
                    }
                    if (piece.row == this.row && col < this.col &&
                            (piece.col == this.col - 3 || piece.col == this.col - 2 || piece.col == this.col - 1)) {
                        return false;
                    }
                    if (isEnemy(piece)) {
                        for (int i = 1; i < 3; i++) {
                            if (piece.isMoveValid(row, col + i, pieces, false)) return false;
                        }
                        for (int i = 1; i < 4; i++) {
                            if (piece.isMoveValid(row, col - i, pieces, false)) return false;
                        }
                    }
                }

                iter = pieces.iterator();
                while (iter.hasNext()) {
                    ChessPiece piece = iter.next();
                    if (piece instanceof Rook && !isEnemy(piece)) {
                        if (piece.moved)
                            return false;
                        if (col > this.col) {
                            if (piece.col == this.col + 3) {
                                if (!MoveValidator.validateComplete(this, this, pieces, row, col)) {
                                    return false;
                                } else {
                                    piece.col -= 2;
                                    ((Rook) piece).moved = true;
                                    return true;
                                }
                            }
                        } else if (col < this.col) {
                            if (piece.col == 0) {
                                if (!MoveValidator.validateComplete(this, this, pieces, row, col)) {
                                    return false;
                                } else {
                                    piece.col += 3;
                                    piece.moved = true;
                                    return true;
                                }
                            }
                        }
                    }
                }
            }

        return false;
    }
}
