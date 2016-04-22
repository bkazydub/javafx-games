package playground.game.chess.infrastructure;

import java.util.List;

/**
 * Created by dragonmf on 4/17/16.
 */
public class Bishop extends ChessPiece {

    public Bishop(Color color, int row, int col) {
        super(color, row, col);
    }

    @Override
    protected boolean isMoveValid(int row, int col, List<ChessPiece> pieces, boolean doesMovement) {
        return MoveValidator.validateDiagonal(row, col, this, pieces, doesMovement);
    }
}
