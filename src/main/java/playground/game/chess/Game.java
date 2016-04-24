package playground.game.chess;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import playground.game.chess.infrastructure.*;

import java.util.ArrayList;
import java.util.List;

import static playground.game.chess.infrastructure.ChessPiece.Color.BLACK;
import static playground.game.chess.infrastructure.ChessPiece.Color.WHITE;

/**
 * Created by dragonmf on 4/17/16.
 */
public class Game extends Application {

    //public static final double SCREEN_DIMENSION = 640;
    public static final double SCREEN_DIMENSION = 480;
    public static final double CELL_SIZE = SCREEN_DIMENSION / 8;
    //public static final double SPRITE_SHEET_WIDTH = 792;
    //public static final double SPRITE_SHEET_HEIGHT = 264;

    private long lastRefresh;

    private Canvas chessBoard;
    private GraphicsContext boardGC;
    private Canvas piecesCanvas;
    private GraphicsContext piecesGC;
    private Canvas pawnConversionCanvas;
    private GraphicsContext conversionGC;

    /*private static final double PROMOTION_OPTION_OFFSET_X = 70;
    private static final double PROMOTION_OPTION_OFFSET_Y = 70;
    private static final double PROMOTION_OPTION_IMG_SIZE = 250;*/

    private static final double PROMOTION_OPTION_OFFSET_X = 40;
    private static final double PROMOTION_OPTION_OFFSET_Y = 40;
    private static final double PROMOTION_OPTION_IMG_SIZE = 200;

    private static final double PIECE_IMG_WIDTH = 790 / 6;
    private static final double PIECE_IMG_HEIGHT = 264 / 2;

    private static final double ROOK_IMG_START_X = 1 + 0;
    private static final double KNIGHT_IMG_START_X = 1 + 1 * PIECE_IMG_WIDTH;
    private static final double BISHOP_IMG_START_X = 1 + 2 * PIECE_IMG_WIDTH;
    private static final double QUEEN_IMG_START_X = 1 + 3 * PIECE_IMG_WIDTH;
    private static final double KING_IMG_START_X = 1 + 4 * PIECE_IMG_WIDTH;
    private static final double PAWN_IMG_START_X = 1 + 5 * PIECE_IMG_WIDTH;

    private static final Rectangle2D OPTION_ROOK_BOX = new Rectangle2D(PROMOTION_OPTION_OFFSET_X,
            PROMOTION_OPTION_OFFSET_Y, PROMOTION_OPTION_IMG_SIZE, PROMOTION_OPTION_IMG_SIZE);
    private static final Rectangle2D OPTION_KNIGHT_BOX = new Rectangle2D(
            PROMOTION_OPTION_OFFSET_X + PROMOTION_OPTION_IMG_SIZE, PROMOTION_OPTION_OFFSET_Y,
            PROMOTION_OPTION_IMG_SIZE, PROMOTION_OPTION_IMG_SIZE);
    private static final Rectangle2D OPTION_BISHOP_BOX = new Rectangle2D(PROMOTION_OPTION_OFFSET_X,
            PROMOTION_OPTION_OFFSET_Y + PROMOTION_OPTION_IMG_SIZE, PROMOTION_OPTION_IMG_SIZE,
            PROMOTION_OPTION_IMG_SIZE);
    private static final Rectangle2D OPTION_QUEEN_BOX = new Rectangle2D(
            PROMOTION_OPTION_OFFSET_X + PROMOTION_OPTION_IMG_SIZE,
            PROMOTION_OPTION_OFFSET_Y + PROMOTION_OPTION_IMG_SIZE, PROMOTION_OPTION_IMG_SIZE,
            PROMOTION_OPTION_IMG_SIZE);

    private final Image IMAGE_PIECES = new Image("img/pieces.png");

    private ChessPiece selectedPiece;
    private Pawn toPromote;

    public boolean whiteTurn = true;

    private List<ChessPiece> pieces = new ArrayList<>();

    @Override
    public void start(Stage stage) {

        StackPane root = new StackPane();
        Scene scene = new Scene(root);
        stage.setScene(scene);

        chessBoard = new Canvas(SCREEN_DIMENSION, SCREEN_DIMENSION);
        boardGC = chessBoard.getGraphicsContext2D();

        drawChessBoard();
        initChessPieces();
        initPromotionLayer();

        root.getChildren().add(chessBoard);
        piecesCanvas = new Canvas(SCREEN_DIMENSION, SCREEN_DIMENSION);
        piecesGC = piecesCanvas.getGraphicsContext2D();
        root.getChildren().add(piecesCanvas);
        root.getChildren().add(pawnConversionCanvas);
        initController();

        AnimationTimer loop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (now - lastRefresh < 200_000_000)
                    return;
                lastRefresh = now;
                piecesGC.clearRect(0, 0, SCREEN_DIMENSION, SCREEN_DIMENSION);
                //boardGC.clearRect(0, 0, SCREEN_DIMENSION, SCREEN_DIMENSION);
                drawChessBoard();

                for (ChessPiece piece : pieces) {
                    if (piece == selectedPiece) {
                        //boardGC.clearRect(0, 0, SCREEN_DIMENSION, SCREEN_DIMENSION);
                        //drawChessBoard();
                        boardGC.setFill(Color.MEDIUMVIOLETRED);
                        boardGC.fillRect(getPieceX(piece), getPieceY(piece), CELL_SIZE, CELL_SIZE);
                    }
                    if (piece.isHighlighted()) {
                        boardGC.setFill(Color.INDIANRED);
                        boardGC.fillRect(getPieceX(piece), getPieceY(piece), CELL_SIZE, CELL_SIZE);
                    }
                    drawPiece(piece);
                }
            }
        };

        loop.start();

        stage.setTitle("When You Play the Game of Chess You Win or You Don't.");
        stage.show();
    }

    private void drawChessBoard() {
        double cellSize = SCREEN_DIMENSION / 8;
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if ((i + j) % 2 == 0) {
                    boardGC.setFill(Color.BEIGE);
                } else {
                    boardGC.setFill(Color.GRAY);
                }
                boardGC.fillRect(i * cellSize, j * cellSize, cellSize, cellSize);
            }
        }
    }

    private void drawPiece(ChessPiece piece) {
        double x = 0;
        int imgRow = piece.getColor() == WHITE ? 0 : 1;
        if (piece instanceof Rook)
            x = ROOK_IMG_START_X;
        else if (piece instanceof Knight)
            x = KNIGHT_IMG_START_X;
        else if (piece instanceof Bishop)
            x = BISHOP_IMG_START_X;
        else if (piece instanceof Queen)
            x = QUEEN_IMG_START_X;
        else if (piece instanceof King)
            x = KING_IMG_START_X;
        else if (piece instanceof Pawn)
            x = PAWN_IMG_START_X;
        piecesGC.drawImage(IMAGE_PIECES, x, imgRow * PIECE_IMG_HEIGHT,
                PIECE_IMG_WIDTH, PIECE_IMG_HEIGHT, piece.getCol() * Game.CELL_SIZE,
                piece.getRow() * Game.CELL_SIZE, Game.CELL_SIZE, Game.CELL_SIZE);
    }

    private static Rectangle2D getBoundary(ChessPiece piece) {
        return new Rectangle2D(piece.getCol() * Game.CELL_SIZE, piece.getRow() * Game.CELL_SIZE, Game.CELL_SIZE, Game.CELL_SIZE);
    }

    private static double getPieceX(ChessPiece piece) {
        return piece.getCol() * CELL_SIZE;
    }

    private static double getPieceY(ChessPiece piece) {
        return piece.getRow() * CELL_SIZE;
    }

    private void initPromotionLayer() {
        pawnConversionCanvas = new Canvas(SCREEN_DIMENSION, SCREEN_DIMENSION);
        pawnConversionCanvas.setVisible(false);
        pawnConversionCanvas.setOpacity(0.8);
        conversionGC = pawnConversionCanvas.getGraphicsContext2D();
        conversionGC.setFill(Color.BEIGE);

        pawnConversionCanvas.setOnMouseClicked(e -> {
            double x = e.getX(), y = e.getY();
            if (OPTION_ROOK_BOX.contains(x, y)) {
                pieces.add(toPromote.promoteToRook());
            } else if (OPTION_KNIGHT_BOX.contains(x, y)) {
                pieces.add(toPromote.promoteToKnight());
            } else if (OPTION_BISHOP_BOX.contains(x, y)) {
                pieces.add(toPromote.promoteToBishop());
            } else if (OPTION_QUEEN_BOX.contains(x, y)) {
                pieces.add(toPromote.promoteToQueen());
            } else {
                return;
            }
            pieces.remove(toPromote);
            pawnConversionCanvas.setVisible(false);
            piecesCanvas.toFront();
        });
    }

    private void showPromotionOptions() {
        pawnConversionCanvas.setVisible(true);
        pawnConversionCanvas.toFront();
        int imgRow = toPromote.getColor() == WHITE ? 0 : 1;
        conversionGC.fillRect(0, 0, SCREEN_DIMENSION, SCREEN_DIMENSION);
        conversionGC.drawImage(IMAGE_PIECES, ROOK_IMG_START_X, imgRow * PIECE_IMG_HEIGHT,
                PIECE_IMG_WIDTH, PIECE_IMG_HEIGHT, PROMOTION_OPTION_OFFSET_X, PROMOTION_OPTION_OFFSET_Y,
                PROMOTION_OPTION_IMG_SIZE, PROMOTION_OPTION_IMG_SIZE);
        conversionGC.drawImage(IMAGE_PIECES, KNIGHT_IMG_START_X, imgRow * PIECE_IMG_HEIGHT,
                PIECE_IMG_WIDTH, PIECE_IMG_HEIGHT, PROMOTION_OPTION_OFFSET_X + PROMOTION_OPTION_IMG_SIZE, PROMOTION_OPTION_OFFSET_Y,
                PROMOTION_OPTION_IMG_SIZE, PROMOTION_OPTION_IMG_SIZE);
        conversionGC.drawImage(IMAGE_PIECES, BISHOP_IMG_START_X, imgRow * PIECE_IMG_HEIGHT,
                PIECE_IMG_WIDTH, PIECE_IMG_HEIGHT, PROMOTION_OPTION_OFFSET_X, PROMOTION_OPTION_OFFSET_Y + PROMOTION_OPTION_IMG_SIZE,
                PROMOTION_OPTION_IMG_SIZE, PROMOTION_OPTION_IMG_SIZE);
        conversionGC.drawImage(IMAGE_PIECES, QUEEN_IMG_START_X, imgRow * PIECE_IMG_HEIGHT,
                PIECE_IMG_WIDTH, PIECE_IMG_HEIGHT, PROMOTION_OPTION_OFFSET_X + PROMOTION_OPTION_IMG_SIZE, PROMOTION_OPTION_OFFSET_Y + PROMOTION_OPTION_IMG_SIZE,
                PROMOTION_OPTION_IMG_SIZE, PROMOTION_OPTION_IMG_SIZE);
    }

    private void initController() {
        piecesCanvas.setOnMousePressed(e -> {
            for (ChessPiece piece : pieces) {
                if (getBoundary(piece).contains(e.getX(), e.getY())) {
                    if (piece != selectedPiece) {
                        if ((whiteTurn && piece.getColor() == WHITE) || (!whiteTurn && piece.getColor() == BLACK)) {
                            selectedPiece = piece;
                            //break;
                        }
                    }
                    piece.setHighlighted(false);
                }
            }
            if (selectedPiece != null) {
                int row = getCellIndex(e.getY());
                int col = getCellIndex(e.getX());
                if (selectedPiece.move(row, col, pieces)) {
                    if (selectedPiece instanceof Pawn && ((Pawn) selectedPiece).reachedLastRow()) {
                        toPromote = (Pawn) selectedPiece;
                        showPromotionOptions();
                    }
                    whiteTurn = !whiteTurn;
                    selectedPiece = null;
                }
            }
        });
    }

    private int getCellIndex(double y) {
        int index = 0;
        for (int i = 0; i < 8; i++) {
            if (y > i * CELL_SIZE && y < (i + 1) * CELL_SIZE) {
                index = i;
                break;
            }
        }
        return index;
    }

    private void initChessPieces() {
        for (int i = 0; i < 8; i++) {
            Pawn whitePawn = new Pawn(WHITE, 1, i);
            Pawn blackPawn = new Pawn(BLACK, 6, i);
            pieces.add(whitePawn);
            pieces.add(blackPawn);
        }

        for (int i = 0; i < 2; i++) {
            Rook whiteRook = new Rook(WHITE, 0, i == 0 ? 0 : 7);
            Rook blackRook = new Rook(BLACK, 7, i == 0 ? 0 : 7);
            pieces.add(whiteRook);
            pieces.add(blackRook);
            Knight whiteKnight = new Knight(WHITE, 0, i == 0 ? 1 : 6);
            Knight blackKnight = new Knight(BLACK, 7, i == 0 ? 1 : 6);
            pieces.add(whiteKnight);
            pieces.add(blackKnight);
            Bishop whiteBishop = new Bishop(WHITE, 0, i == 0 ? 2 : 5);
            Bishop blackBishop = new Bishop(BLACK, 7, i == 0 ? 2 : 5);
            pieces.add(whiteBishop);
            pieces.add(blackBishop);
        }

        Queen whiteQueen = new Queen(WHITE, 0, 3);
        Queen blackQueen = new Queen(BLACK, 7, 3);
        pieces.add(whiteQueen);
        pieces.add(blackQueen);

        King whiteKing = new King(WHITE, 0, 4);
        King blackKing = new King(BLACK, 7, 4);
        pieces.add(whiteKing);
        pieces.add(blackKing);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
