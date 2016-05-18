package playground.game.chess.infrastructure;

import java.util.ArrayList;
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
                    if (this == piece || piece.captured) continue;
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

                if (!MoveValidator.validateNotChecked(this, pieces)) return false;

                if (moved) return false;

                for (ChessPiece piece : pieces) {
                    if (piece.captured) continue;

                    if (piece.row == this.row) {
                        if (col > this.col) {
                            if (piece.col == this.col + 1 || piece.col == this.col + 2) return false;
                        } else {
                            if (piece.col == this.col - 3 || piece.col == this.col - 2 || piece.col == this.col - 1) return false;
                        }
                    }
                    if (isEnemy(piece)) {
                        // New list is created in order to take pawns into account
                        List<ChessPiece> tempPieces = new ArrayList<>(pieces);
                        tempPieces.remove(this);
                        King tempKing = new King(color, this.row, this.col + ((col > this.col) ? 1 : -1));
                        tempPieces.add(tempKing);

                        if (piece.isMoveValid(tempKing.row, tempKing.col, tempPieces, false)) {
                            piece.highlighted = true;
                            return false;
                        }
                    }
                }

                for (ChessPiece piece : pieces) {
                    if (piece.captured) continue;

                    if (piece instanceof Rook && !isEnemy(piece)) {
                        if (piece.moved) return false;
                        if (col > this.col) {
                            if (piece.col == this.col + 3) {
                                if (!MoveValidator.validateComplete(this, this, pieces, row, col)) {
                                    return false;
                                } else {
                                    if (doesMovement) {
                                        piece.col -= 2;
                                        ((Rook) piece).moved = true;
                                    }
                                    return true;
                                }
                            }
                        } else if (col < this.col) {
                            if (piece.col == 0) {
                                if (!MoveValidator.validateComplete(this, this, pieces, row, col)) {
                                    return false;
                                } else {
                                    if (doesMovement) {
                                        piece.col += 3;
                                        piece.moved = true;
                                    }
                                    return true;
                                }
                            }
                        }
                    }
                }
            }

        return false;
    }

    @Override
    public List<Move> availableMoves(List<ChessPiece> pieces) {
        List<Move> moves = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (isMoveValid(i, j, pieces, false) && MoveValidator.validateComplete(this, this, pieces, i, j)) {
                    moves.add(new Move(row, col, i, j));
                }
            }
        }
        return moves;
    }
}
