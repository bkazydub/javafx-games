package playground.game.chess.infrastructure;

import java.io.Serializable;

/**
 * The class is used to represent piece's move.
 * Instances of the class are used as DTO over socket connection for multiplayer mode.
 */
public class Move implements Serializable {

    private final int rank;
    private final int file;
    private final int newRank;
    private final int newFile;

    private ChessPiece promotedTo;
    private boolean capture;

    public Move(int rank, int file, int newRank, int newFile) {
        this.rank = rank;
        this.file = file;
        this.newRank = newRank;
        this.newFile = newFile;
    }

    public int getRank() {
        return rank;
    }

    public int getFile() {
        return file;
    }

    public int getNewRank() {
        return newRank;
    }

    public int getNewFile() {
        return newFile;
    }

    public ChessPiece getPromotedTo() {
        return promotedTo;
    }

    public void setPromotedTo(ChessPiece promotedTo) {
        this.promotedTo = promotedTo;
    }

    public boolean isCapture() {
        return capture;
    }

    public void setCapture(boolean capture) {
        this.capture = capture;
    }
}
