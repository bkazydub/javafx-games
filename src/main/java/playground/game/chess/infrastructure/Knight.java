package playground.game.chess.infrastructure;

import java.util.List;

/**
 * Created by dragonmf on 4/17/16.
 */
public class Knight extends ChessPiece {

    public Knight(Color color, int row, int col) {
        super(color, row, col);
    }

    @Override
    protected boolean isMoveValid(int row, int col, List<ChessPiece> pieces, boolean doesMovement) {
        if ((Math.abs(this.row - row) == 2 && Math.abs(this.col - col) == 1)
                || (Math.abs(this.row - row) == 1 && Math.abs(this.col - col) == 2)) {

            boolean valid = MoveValidator.validate(this, pieces, row, col);
            if (!valid) return false;

            return doesMovement ? (MoveValidator.validateComplete(this, MoveValidator.getKing(this.color, pieces), pieces, row, col) && valid) : valid;
        }
        return false;
    }
}
