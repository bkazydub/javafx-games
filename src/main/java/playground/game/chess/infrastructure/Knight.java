package playground.game.chess.infrastructure;

import java.util.List;

public class Knight extends ChessPiece {

    public Knight(Color color, int rank, int file) {
        super(color, rank, file);
    }

    @Override
    protected boolean isMoveValid(int rank, int file, List<ChessPiece> pieces, boolean doesMovement) {
        if ((Math.abs(this.rank - rank) == 2 && Math.abs(this.file - file) == 1)
                || (Math.abs(this.rank - rank) == 1 && Math.abs(this.file - file) == 2)) {

            boolean valid = MoveValidator.validate(this, pieces, rank, file);
            if (!valid) return false;

            return doesMovement ? (MoveValidator.validateComplete(this, MoveValidator.getKing(this.color, pieces), pieces, rank, file) && valid) : valid;
        }
        return false;
    }
}
