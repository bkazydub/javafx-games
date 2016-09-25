package playground.game.chess.infrastructure;

import java.util.ArrayList;
import java.util.List;

public class King extends ChessPiece {

    public King(Color color, int rank, int file) {
        super(color, rank, file);
    }

    @Override
    protected boolean isMoveValid(int rank, int file, List<ChessPiece> pieces, boolean doesMovement) {
            if ((Math.abs(this.file - file) == 1 && this.rank == rank)
                    || (Math.abs(this.rank - rank) == 1 && this.file == file)
                    || (Math.abs(this.rank - rank) == 1 && Math.abs(this.file - file) == 1)) {
                for (ChessPiece piece : pieces) {
                    if (this == piece || piece.captured) continue;
                    if (piece.file == file && piece.rank == rank) {
                        if (isEnemy(piece)) {
                            boolean valid = MoveValidator.validate(this, pieces, rank, file);
                            return doesMovement ? (MoveValidator.validateComplete(this, this, pieces, rank, file) && valid) : MoveValidator.validate(this, pieces, rank, file);
                        } else {
                            return false;
                        }
                    }
                }
                if (doesMovement)
                    return MoveValidator.validateComplete(this, this, pieces, rank, file);
                else return MoveValidator.validate(this, pieces, rank, file);

            } else if (Math.abs(this.file - file) == 2 && this.rank == rank) {

                if (!MoveValidator.validateNotChecked(this, pieces)) return false;

                if (moved) return false;

                for (ChessPiece piece : pieces) {
                    if (piece.captured) continue;

                    if (piece.rank == this.rank) {
                        if (file > this.file) {
                            if (piece.file == this.file + 1 || piece.file == this.file + 2) return false;
                        } else {
                            if (piece.file == this.file - 3 || piece.file == this.file - 2 || piece.file == this.file - 1) return false;
                        }
                    }
                    if (isEnemy(piece)) {
                        // New list is created in order to take pawns into account
                        List<ChessPiece> tempPieces = new ArrayList<>(pieces);
                        tempPieces.remove(this);
                        King tempKing = new King(color, this.rank, this.file + ((file > this.file) ? 1 : -1));
                        tempPieces.add(tempKing);

                        if (piece.isMoveValid(tempKing.rank, tempKing.file, tempPieces, false)) {
                            piece.highlighted = true;
                            return false;
                        }
                    }
                }

                for (ChessPiece piece : pieces) {
                    if (piece.captured) continue;

                    if (piece instanceof Rook && !isEnemy(piece)) {
                        if (piece.moved) return false;
                        if (file > this.file) {
                            if (piece.file == this.file + 3) {
                                if (!MoveValidator.validateComplete(this, this, pieces, rank, file)) {
                                    return false;
                                } else {
                                    if (doesMovement) {
                                        piece.file -= 2;
                                        ((Rook) piece).moved = true;
                                    }
                                    return true;
                                }
                            }
                        } else if (file < this.file) {
                            if (piece.file == 0) {
                                if (!MoveValidator.validateComplete(this, this, pieces, rank, file)) {
                                    return false;
                                } else {
                                    if (doesMovement) {
                                        piece.file += 3;
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
                    moves.add(new Move(rank, file, i, j));
                }
            }
        }
        return moves;
    }
}
