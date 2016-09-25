package playground.game.chess.infrastructure;

import java.util.List;

public class Queen extends ChessPiece {

    public Queen(Color color, int rank, int file) {
        super(color, rank, file);
    }

    @Override
    protected boolean isMoveValid(int rank, int file, List<ChessPiece> pieces, boolean doesMovement) {
        if (!(this.rank == rank && this.file == file)) {
            if (MoveValidator.validateDiagonal(rank, file, this, pieces, doesMovement))
                return true;
            else
                return MoveValidator.validateLinear(rank, file, this, pieces, doesMovement);
        }
        return false;
    }
}
