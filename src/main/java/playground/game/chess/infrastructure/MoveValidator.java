package playground.game.chess.infrastructure;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by dragonmf on 4/17/16.
 */
public class MoveValidator {
    
    public static boolean validateDiagonal(int row, int col, ChessPiece selectedPiece, List<ChessPiece> pieces, boolean doesMovement) {
        if (Math.abs(selectedPiece.row - row) == Math.abs(selectedPiece.col - col) && selectedPiece.row - row != 0) {
            Iterator<ChessPiece> iter = pieces.iterator();
            while (iter.hasNext()) {
                ChessPiece piece = iter.next();
                if (selectedPiece == piece || piece.captured)
                    continue;
                if (row - selectedPiece.row > 0) {
                    if (piece.row < row && piece.row > selectedPiece.row) {
                        if (piece.col > col && selectedPiece.col > piece.col && piece.row - selectedPiece.row == -(piece.col - selectedPiece.col)) {
                            return false;
                        }
                        if (piece.col < col && selectedPiece.col < piece.col && piece.row - selectedPiece.row == piece.col - selectedPiece.col) {
                            return false;
                        }
                    }
                } else if (row - selectedPiece.row < 0) {
                    if (piece.row > row && piece.row < selectedPiece.row) {
                        if (piece.col > col && selectedPiece.col > piece.col && piece.row - selectedPiece.row == piece.col - selectedPiece.col) {
                            return false;
                        }
                        if (piece.col < col && selectedPiece.col < piece.col && piece.row - selectedPiece.row == -(piece.col - selectedPiece.col)) {
                            return false;
                        }
                    }
                } else {
                    return false;
                }
            }

            boolean valid = validate(selectedPiece, pieces, row, col);
            if (!valid) return false;

            King king = getKing(selectedPiece.color, pieces);
            return doesMovement ? validateComplete(selectedPiece, king, new ArrayList<>(pieces), row, col) : valid;
        }
        return false;
    }

    public static boolean validateLinear(int row, int col, ChessPiece selectedPiece, List<ChessPiece> pieces, boolean doesMovement) {
        if ((selectedPiece.row - row != 0 && selectedPiece.col - col == 0) || selectedPiece.row - row == 0 && selectedPiece.col - col != 0) {
            Iterator<ChessPiece> iter = pieces.iterator();
            while (iter.hasNext()) {
                ChessPiece piece = iter.next();
                if (selectedPiece == piece || piece.captured)
                    continue;
                if (row - selectedPiece.row > 0) {
                    if (piece.col == selectedPiece.col && piece.row < row && piece.row > selectedPiece.row) {
                        return false;
                    }
                } else if (row - selectedPiece.row < 0) {
                    if (piece.col == selectedPiece.col && piece.row > row && piece.row < selectedPiece.row) {
                        return false;
                    }
                } else {
                    if (piece.row == selectedPiece.row) {
                        if ((piece.col > col && selectedPiece.col > piece.col) || (piece.col < col && selectedPiece.col < piece.col)) {
                            return false;
                        }
                    }
                }
            }

            boolean valid = validate(selectedPiece, pieces, row, col);
            if (!valid) return false;

            King king = getKing(selectedPiece.color, pieces);
            return doesMovement ? validateComplete(selectedPiece, king, new ArrayList<>(pieces), row, col) : valid;
        }
        return false;
    }

    /**
     * Validates that the King is not in check.
     */
    public static boolean validateNotChecked(King king, List<ChessPiece> pieces) {
        boolean notChecked = true;

        Iterator<ChessPiece> iter = pieces.iterator();
        while (iter.hasNext()) {
            ChessPiece piece = iter.next();

            if (piece.captured) continue;

            if (piece.isMoveValid(king.row, king.col, pieces, false)) {
                if (king.isEnemy(piece)) {
                    piece.highlighted = true;
                    notChecked = false;
                }
            }
        }
        return notChecked;
    }

    /**
     * Is called to validate the new cell for selected piece
     * is vacant or has an enemy piece (which is going to be captured).
     */
    public static boolean validate(ChessPiece selected, List<ChessPiece> pieces, int row, int col) {
        Iterator<ChessPiece> iter = pieces.iterator();
        while (iter.hasNext()) {
            ChessPiece piece = iter.next();
            if (selected == piece || piece.captured) continue;
            if (piece.col == col && piece.row == row)
                return selected.isEnemy(piece);
        }
        return true;
    }

    /**
     * Validates whether the ('physically' valid) move is possible:
     * after the move is done the king of the same color as the piece to be moved
     * shouldn't be in check.
     */
    public static boolean validateComplete(ChessPiece selected, King king, List<ChessPiece> pieces, int newRow, int newCol) {
        int prevCol = selected.col;
        int prevRow = selected.row;
        selected.col = newCol;
        selected.row = newRow;

        ChessPiece deleted = null;

        Iterator<ChessPiece> iter = pieces.iterator();
        while (iter.hasNext()) {
            ChessPiece piece = iter.next();

            if (piece.captured) continue;

            if (selected.isEnemy(piece) && piece.col == selected.col && piece.row == selected.row) {
                iter.remove();
                deleted = piece;
                break;
            }
        }

        boolean notChecked = validateNotChecked(king, pieces);

        selected.col = prevCol;
        selected.row = prevRow;

        if (deleted != null) {
            pieces.add(deleted);
        }

        return notChecked;
    }

    public static boolean validateNotMate(ChessPiece.Color color, List<ChessPiece> pieces) {
        // if there is a valid (complete) move - it's not a mate.
        for (ChessPiece piece : pieces) {
            if (piece.captured || piece.color != color) continue;
            if (piece.availableMoves(pieces).size() > 0) return true;
        }
        return false;
    }

    protected static King getKing(ChessPiece.Color color, List<ChessPiece> pieces) {
        for (ChessPiece piece : pieces) {
            if (piece instanceof King && piece.color == color)
                return (King) piece;
        }
        return null;
    }
}
