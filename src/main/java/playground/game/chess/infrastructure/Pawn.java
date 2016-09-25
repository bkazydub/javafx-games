package playground.game.chess.infrastructure;

import java.util.List;

public class Pawn extends ChessPiece {

    // Starting rank for the piece (for detecting the last rank)
    private final int startRank;

    public Pawn(Color color, int rank, int file) {
        super(color, rank, file);
        startRank = rank;
    }

    // todo: add en passant option
    @Override
    protected boolean isMoveValid(int rank, int file, List<ChessPiece> pieces, boolean doesMovement) {
            if (Math.abs(file - this.file) == 1) {
                for (ChessPiece piece : pieces) {
                    if (this == piece || piece.captured) continue;
                    if (rank - this.rank == (this.startRank == 1 ? 1 : -1)) {
                        if (piece.rank == rank && piece.file == file) {
                            if (isEnemy(piece)) {
                                if (doesMovement)
                                    return MoveValidator.validateComplete(this, MoveValidator.getKing(this.color, pieces), pieces, rank, file);
                                else return MoveValidator.validate(this, pieces, rank, file);
                            }
                        }
                    }
                }
            }

            if (file - this.file != 0)
                return false;

            for (ChessPiece piece : pieces) {
                if (piece.captured) continue;

                // disable movement to occupied cells.
                if (piece.rank == rank && piece.file == file)
                    return false;
                // disable 'jumping' over other pieces
                if (piece.rank == (this.startRank == 1 ? this.rank + 1 : this.rank - 1) && this.file == piece.file)
                    return false;
            }

            if (startRank == 1) {
                if (rank - this.rank > 0 && rank - this.rank < (moved ? 2 : 3)) {
                    if (doesMovement)
                        return MoveValidator.validateComplete(this, MoveValidator.getKing(this.color, pieces), pieces, rank, file);
                    else return MoveValidator.validate(this, pieces, rank, file);
                }
            } else if (startRank == 6) {
                if (rank - this.rank < 0 && rank - this.rank > (moved ? -2 : -3)) {
                    if (doesMovement)
                        return MoveValidator.validateComplete(this, MoveValidator.getKing(this.color, pieces), pieces, rank, file);
                    else return MoveValidator.validate(this, pieces, rank, file);
                }
            }

            return false;
    }

    public Rook promoteToRook() {
        return new Rook(color, rank, file);
    }

    public Knight promoteToKnight() {
        return new Knight(color, rank, file);
    }

    public Bishop promoteToBishop() {
        return new Bishop(color, rank, file);
    }

    public Queen promoteToQueen() {
        return new Queen(color, rank, file);
    }

    /**
     * Check if this pawn has reached the last rank and is capable of performing promotion.
     * @return true if the last rank has been reached
     */
    public boolean reachedLastRank() {
        return rank == (startRank == 1 ? 7 : 0);
    }
}
