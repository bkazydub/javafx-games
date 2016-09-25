package playground.game.chess.infrastructure;

import java.util.List;

public class Rook extends ChessPiece {

    public Rook(Color color, int rank, int file) {
        super(color, rank, file);
    }

    @Override
    protected boolean isMoveValid(int rank, int file, List<ChessPiece> pieces, boolean doesMovement) {
        return MoveValidator.validateLinear(rank, file, this, pieces, doesMovement);
    }
}
