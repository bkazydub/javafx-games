package playground.game.chess.infrastructure;

import java.util.List;

/**
 * Created by dragonmf on 4/17/16.
 */
public class Queen extends ChessPiece {

    public Queen(Color color, int row, int col) {
        super(color, row, col);
    }

    @Override
    protected boolean isMoveValid(int row, int col, List<ChessPiece> pieces, boolean doesMovement) {
        if (!(this.row == row && this.col == col)) {
            if (MoveValidator.validateDiagonal(row, col, this, pieces, doesMovement))
                return true;
            else
                return MoveValidator.validateLinear(row, col, this, pieces, doesMovement);
        }
        return false;
    }
}
