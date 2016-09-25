package playground.game.chess.infrastructure;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Utility class to validate movement common to few pieces
 * and other validations needed to ensure any of two kings are not checked or to
 * validate that a king is checkmated.
 */
public class MoveValidator {

    private MoveValidator() {
    }

    /**
     * Show whether diagonal movement, characteristic to {@code Bishop} and {@code Queen}, is valid.
     * @param rank a new rank to step onto
     * @param file a new file to step onto
     * @param selectedPiece piece to validate against
     * @param pieces all the pieces currently on the board
     * @param doesMovement flag, if set to true specifies that this piece does movement and not
     *                     used for non-check validation
     * @return true if the move is valid
     */
    public static boolean validateDiagonal(int rank, int file, ChessPiece selectedPiece, List<ChessPiece> pieces, boolean doesMovement) {
        if (Math.abs(selectedPiece.rank - rank) == Math.abs(selectedPiece.file - file) && selectedPiece.rank - rank != 0) {
            Iterator<ChessPiece> iter = pieces.iterator();
            while (iter.hasNext()) {
                ChessPiece piece = iter.next();
                if (selectedPiece == piece || piece.captured)
                    continue;
                if (rank - selectedPiece.rank > 0) {
                    if (piece.rank < rank && piece.rank > selectedPiece.rank) {
                        if (piece.file > file && selectedPiece.file > piece.file && piece.rank - selectedPiece.rank == -(piece.file - selectedPiece.file)) {
                            return false;
                        }
                        if (piece.file < file && selectedPiece.file < piece.file && piece.rank - selectedPiece.rank == piece.file - selectedPiece.file) {
                            return false;
                        }
                    }
                } else if (rank - selectedPiece.rank < 0) {
                    if (piece.rank > rank && piece.rank < selectedPiece.rank) {
                        if (piece.file > file && selectedPiece.file > piece.file && piece.rank - selectedPiece.rank == piece.file - selectedPiece.file) {
                            return false;
                        }
                        if (piece.file < file && selectedPiece.file < piece.file && piece.rank - selectedPiece.rank == -(piece.file - selectedPiece.file)) {
                            return false;
                        }
                    }
                } else {
                    return false;
                }
            }

            boolean valid = validate(selectedPiece, pieces, rank, file);
            if (!valid) return false;

            King king = getKing(selectedPiece.color, pieces);
            return doesMovement ? validateComplete(selectedPiece, king, new ArrayList<>(pieces), rank, file) : valid;
        }
        return false;
    }

    /**
     * Show whether linear movement, characteristic to {@code Queen} and {@code Rook}, is valid.
     * @param rank a new rank to step onto
     * @param file a new file to step onto
     * @param selectedPiece piece to validate against
     * @param pieces all the pieces currently on the board
     * @param doesMovement flag, if set to true specifies that this piece does movement and not
     *                     used for non-check validation
     * @return true if the move is valid
     */
    public static boolean validateLinear(int rank, int file, ChessPiece selectedPiece, List<ChessPiece> pieces, boolean doesMovement) {
        if ((selectedPiece.rank - rank != 0 && selectedPiece.file - file == 0) || selectedPiece.rank - rank == 0 && selectedPiece.file - file != 0) {
            for (ChessPiece piece : pieces) {
                if (selectedPiece == piece || piece.captured)
                    continue;
                if (rank - selectedPiece.rank > 0) {
                    if (piece.file == selectedPiece.file && piece.rank < rank && piece.rank > selectedPiece.rank) {
                        return false;
                    }
                } else if (rank - selectedPiece.rank < 0) {
                    if (piece.file == selectedPiece.file && piece.rank > rank && piece.rank < selectedPiece.rank) {
                        return false;
                    }
                } else {
                    if (piece.rank == selectedPiece.rank) {
                        if ((piece.file > file && selectedPiece.file > piece.file) || (piece.file < file && selectedPiece.file < piece.file)) {
                            return false;
                        }
                    }
                }
            }

            boolean valid = validate(selectedPiece, pieces, rank, file);
            if (!valid) return false;

            King king = getKing(selectedPiece.color, pieces);
            return doesMovement ? validateComplete(selectedPiece, king, new ArrayList<>(pieces), rank, file) : valid;
        }
        return false;
    }

    /**
     * Validates that the King is not in check.
     * @param king king in question
     * @param pieces pieces currently on the board
     * @return false if the king is not in check
     */
    public static boolean validateNotChecked(King king, List<ChessPiece> pieces) {
        boolean notChecked = true;

        for (ChessPiece piece : pieces) {
            if (piece.captured) continue;

            if (piece.isMoveValid(king.rank, king.file, pieces, false)) {
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
     * @param selected piece in question
     * @param pieces pieces currently on the board
     * @param rank new rank for selected piece
     * @param file new rank for selected piece
     * @return true if the move is valid
     */
    public static boolean validate(ChessPiece selected, List<ChessPiece> pieces, int rank, int file) {
        for (ChessPiece piece : pieces) {
            if (selected == piece || piece.captured) continue;
            if (piece.file == file && piece.rank == rank)
                return selected.isEnemy(piece);
        }
        return true;
    }

    // todO: remove @param king and retrieve it in the method body
    /**
     * Validates whether the move is possible:
     * after the move is done the king of the same color as the moving piece
     * should not be in check.
     * @param selected piece that performs movement
     * @param king
     */
    public static boolean validateComplete(ChessPiece selected, King king, List<ChessPiece> pieces, int newRank, int newFile) {
        int prevFile = selected.file;
        int prevRank = selected.rank;
        selected.file = newFile;
        selected.rank = newRank;

        ChessPiece deleted = null;

        Iterator<ChessPiece> iter = pieces.iterator();
        while (iter.hasNext()) {
            ChessPiece piece = iter.next();
            if (piece.captured) continue;

            if (selected.isEnemy(piece) && piece.file == selected.file && piece.rank == selected.rank) {
                iter.remove();
                // delete piece temporarily
                deleted = piece;
                break;
            }
        }

        boolean notChecked = validateNotChecked(king, pieces);

        selected.file = prevFile;
        selected.rank = prevRank;

        if (deleted != null) {
            // restore deleted piece
            pieces.add(deleted);
        }

        return notChecked;
    }

    /**
     * Make sure the king is not checkmated.
     * @param color king's color
     * @param pieces pieces currently on the board
     * @return true if there is no checkmate
     */
    public static boolean validateNotMate(ChessPiece.Color color, List<ChessPiece> pieces) {
        // if there is a valid (complete) move - it's not a mate.
        for (ChessPiece piece : pieces) {
            if (piece.captured || piece.color != color) continue;
            if (piece.availableMoves(pieces).size() > 0) return true;
        }
        return false;
    }

    /**
     * Retrieve king of the color specified {@code (king.color == color)}.
     * @param color color of a king to retrieve
     * @param pieces pieces currently on the board
     * @return king
     */
    protected static King getKing(ChessPiece.Color color, List<ChessPiece> pieces) {
        for (ChessPiece piece : pieces) {
            if (piece instanceof King && piece.color == color)
                return (King) piece;
        }
        return null;
    }
}
