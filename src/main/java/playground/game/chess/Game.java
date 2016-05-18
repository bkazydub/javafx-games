package playground.game.chess;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;
import playground.game.chess.infrastructure.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static playground.game.chess.infrastructure.ChessPiece.Color.BLACK;
import static playground.game.chess.infrastructure.ChessPiece.Color.WHITE;

/**
 * Created by dragonmf on 4/17/16.
 */
public class Game extends Application {

    private enum State {
        MENU, STANDALONE, SERVER, CLIENT
    }

    //public static final double SCREEN_DIMENSION = 640;
    public static final double SCREEN_DIMENSION = 480;
    public static final double CELL_SIZE = SCREEN_DIMENSION / 8;
    //public static final double SPRITE_SHEET_WIDTH = 792;
    //public static final double SPRITE_SHEET_HEIGHT = 264;

    private long lastRefresh;

    private Stage stage;
    private Stage connectingStage;
    private Stage waitingStage;
    private Scene menuScene;
    private Scene gameScene;
    private Scene connectingScene;
    private Scene waitingScene;

    private Label conErrorLabel;

    private State state = State.MENU;

    private Canvas chessBoard;
    private GraphicsContext boardGC;
    private Canvas piecesCanvas;
    private GraphicsContext piecesGC;
    private Canvas pawnPromotionCanvas;
    private GraphicsContext promotionGC;
    private Canvas infoCanvas;
    private GraphicsContext infoGC;

    private boolean colorShown = false;
    private boolean showDCMessage = false;

    private Canvas highlightCanvas;
    private GraphicsContext highlightGC;

    private long gameStart;
    private static final long SHOW_COLOR_DURATION_NS = 5_000_000_000L;

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
    private volatile boolean connected = false;

    private Move lastMove = null;
    private Move opponentMove = null;

    private List<ChessPiece> pieces = new ArrayList<>();

    private List<Move> yourMoves = new ArrayList<>();
    private List<Move> opponentMoves = new ArrayList<>();

    // menu
    private VBox menuBox;
    private int currentItem = 0;

    // Multiplayer
    private static final int PORT = 8888;
    private String host;
    private Socket socket;
    private ServerSocket server;

    private ObjectInputStream in;
    private ObjectOutputStream out;

    private ChessPiece.Color yourColor;

    @Override
    public void start(Stage stage) {
        this.stage = stage;

        stage.setOnCloseRequest(e -> System.exit(1));

        initMenu();
        initConnectDialog();
        initWaitDialog();

        highlightCanvas = new Canvas(SCREEN_DIMENSION, SCREEN_DIMENSION);
        highlightGC = highlightCanvas.getGraphicsContext2D();
        highlightGC.setGlobalAlpha(0.5);

        stage.setScene(menuScene);
        stage.show();

        StackPane root = new StackPane();
        gameScene = new Scene(root, SCREEN_DIMENSION, SCREEN_DIMENSION);

        chessBoard = new Canvas(SCREEN_DIMENSION, SCREEN_DIMENSION);
        boardGC = chessBoard.getGraphicsContext2D();

        drawChessBoard();
        initChessPieces();
        initPromotionLayer();

        piecesCanvas = new Canvas(SCREEN_DIMENSION, SCREEN_DIMENSION);
        piecesGC = piecesCanvas.getGraphicsContext2D();
        initController();

        infoCanvas = new Canvas(SCREEN_DIMENSION, SCREEN_DIMENSION);
        infoCanvas.setVisible(false);
        infoCanvas.setOpacity(0.7);
        infoGC = infoCanvas.getGraphicsContext2D();
        infoGC.setTextAlign(TextAlignment.CENTER);
        infoGC.setFont(Font.font("Arial", FontWeight.BOLD, 50));

        root.getChildren().add(chessBoard);
        root.getChildren().add(highlightCanvas);
        root.getChildren().add(piecesCanvas);
        root.getChildren().add(pawnPromotionCanvas);
        root.getChildren().add(infoCanvas);

        AnimationTimer loop = new AnimationTimer() {
            @Override
            public void handle(long now) {

                if (state == State.MENU && stage.getScene() != menuScene) {
                    stage.setScene(menuScene);
                }

                if (state == State.STANDALONE || state == State.SERVER || state == State.CLIENT) {

                    boolean sc = state == State.SERVER || state == State.CLIENT;
                    if (sc && !connected) return;

                    if (sc && !MoveValidator.validateNotMate(yourColor, pieces))
                        showMate(Color.RED);

                    if (showDCMessage) {
                        showOpponentDisconnectedMessage();
                        showDCMessage = false;
                    }

                    long dur = now - gameStart;
                    if (sc && !colorShown && dur <= SHOW_COLOR_DURATION_NS) {
                        showColor();
                        colorShown = true;
                    }

                    if (sc && colorShown && dur > 5_000_000_000L) {
                        hideColor();
                        colorShown = false;
                    }

                    if (state == State.SERVER) {
                        if (stage.getScene() != gameScene) {
                            stage.setScene(gameScene);
                            waitingStage.close();
                        }
                    }

                    if (state == State.CLIENT) {
                        if (stage.getScene() != gameScene) {
                            stage.setScene(gameScene);
                            connectingStage.close();
                        }
                    }

                    if (now - lastRefresh < 200_000_000) return;

                    lastRefresh = now;

                    piecesGC.clearRect(0, 0, SCREEN_DIMENSION, SCREEN_DIMENSION);
                    //drawChessBoard();

                    highlightGC.clearRect(0, 0, SCREEN_DIMENSION, SCREEN_DIMENSION);

                    synchronized (pieces) {
                        for (ChessPiece piece : pieces) {
                            if (!piece.isCaptured()) drawPiece(piece);
                            if (opponentMove != null) {
                                highlightCell(opponentMove.getRow(), opponentMove.getCol(), Color.RED);
                                highlightCell(opponentMove.getNewRow(), opponentMove.getNewCol(), Color.DARKRED);
                            }
                            if (piece == selectedPiece) {
                                highlightCell(piece.getRow(), piece.getCol(), Color.MEDIUMVIOLETRED);
                                for (Move available : piece.availableMoves(new ArrayList<>(pieces))) {
                                    highlightCell(available.getNewRow(), available.getNewCol(), Color.GREEN);
                                }
                            }
                            /*if (piece.isHighlighted()) {
                                boardGC.setFill(Color.INDIANRED);
                                boardGC.fillRect(getPieceX(piece), getPieceY(piece), CELL_SIZE, CELL_SIZE);
                            }*/
                        }
                    }
                }
            }
        };

        loop.start();

        stage.setTitle("Chess");
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

    private void initChessPieces() {
        for (int i = 0; i < 8; i++) {
            Pawn whitePawn = new Pawn(WHITE, 6, i);
            Pawn blackPawn = new Pawn(BLACK, 1, i);
            pieces.add(whitePawn);
            pieces.add(blackPawn);
        }

        for (int i = 0; i < 2; i++) {
            Rook whiteRook = new Rook(WHITE, 7, i == 0 ? 0 : 7);
            Rook blackRook = new Rook(BLACK, 0, i == 0 ? 0 : 7);
            pieces.add(whiteRook);
            pieces.add(blackRook);
            Knight whiteKnight = new Knight(WHITE, 7, i == 0 ? 1 : 6);
            Knight blackKnight = new Knight(BLACK, 0, i == 0 ? 1 : 6);
            pieces.add(whiteKnight);
            pieces.add(blackKnight);
            Bishop whiteBishop = new Bishop(WHITE, 7, i == 0 ? 2 : 5);
            Bishop blackBishop = new Bishop(BLACK, 0, i == 0 ? 2 : 5);
            pieces.add(whiteBishop);
            pieces.add(blackBishop);
        }

        Queen whiteQueen = new Queen(WHITE, 7, 3);
        Queen blackQueen = new Queen(BLACK, 0, 3);
        pieces.add(whiteQueen);
        pieces.add(blackQueen);

        King whiteKing = new King(WHITE, 7, 4);
        King blackKing = new King(BLACK, 0, 4);
        pieces.add(whiteKing);
        pieces.add(blackKing);
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

    private void initPromotionLayer() {
        pawnPromotionCanvas = new Canvas(SCREEN_DIMENSION, SCREEN_DIMENSION);
        pawnPromotionCanvas.setVisible(false);
        pawnPromotionCanvas.setOpacity(0.8);
        promotionGC = pawnPromotionCanvas.getGraphicsContext2D();
        promotionGC.setFill(Color.BEIGE);

        pawnPromotionCanvas.setOnMouseClicked(e -> {
            ChessPiece promotedTo = null;
            synchronized (pieces) {
                double x = e.getX(), y = e.getY();
                if (OPTION_ROOK_BOX.contains(x, y)) {
                    promotedTo = toPromote.promoteToRook();
                } else if (OPTION_KNIGHT_BOX.contains(x, y)) {
                    promotedTo = toPromote.promoteToKnight();
                } else if (OPTION_BISHOP_BOX.contains(x, y)) {
                    promotedTo = toPromote.promoteToBishop();
                } else if (OPTION_QUEEN_BOX.contains(x, y)) {
                    promotedTo = toPromote.promoteToQueen();
                } else {
                    return;
                }
                pieces.remove(toPromote);
                pieces.add(promotedTo);
                if (state == State.SERVER || state == State.CLIENT) {
                    lastMove.setPromotedTo(promotedTo);
                    selectedPiece = null;
                    whiteTurn = !whiteTurn;
                    opponentMove = null;
                    try {
                        out.writeObject(lastMove);
                    } catch (IOException ie) {
                        System.err.println("IOException: " + ie.getMessage());
                    }
                }
                pawnPromotionCanvas.setVisible(false);
                piecesCanvas.toFront();
            }
        });
    }

    private void initController() {
        piecesCanvas.setOnMousePressed(e -> {
            synchronized (pieces) {
                for (ChessPiece piece : pieces) {
                    if (getBoundary(piece).contains(e.getX(), e.getY())) {
                        if (piece != selectedPiece && !piece.isCaptured()) {
                            if (state == State.STANDALONE) {
                                if ((whiteTurn && piece.getColor() == WHITE) || (!whiteTurn && piece.getColor() == BLACK)) {
                                    selectedPiece = piece;
                                }
                            } else if ((whiteTurn && piece.getColor() == WHITE && yourColor == WHITE) || (!whiteTurn && piece.getColor() == BLACK && yourColor == BLACK)) {
                                selectedPiece = piece;
                            }
                        }
                    }
                }
                if (selectedPiece != null) {
                    int row = selectedPiece.getRow();
                    int col = selectedPiece.getCol();
                    int newRow = getCellIndex(e.getY());
                    int newCol = getCellIndex(e.getX());
                    if (selectedPiece.move(newRow, newCol, pieces)) {
                        if (state == State.STANDALONE) {
                            if (selectedPiece instanceof Pawn && ((Pawn) selectedPiece).reachedLastRow()) {
                                toPromote = (Pawn) selectedPiece;
                                showPromotionOptions();
                            }
                            selectedPiece = null;
                            whiteTurn = !whiteTurn;
                        } else if (state == State.SERVER || state == State.CLIENT) {
                            lastMove = new Move(row, col, newRow, newCol);
                            yourMoves.add(lastMove);
                            if (selectedPiece instanceof Pawn && ((Pawn) selectedPiece).reachedLastRow()) {
                                toPromote = (Pawn) selectedPiece;
                                showPromotionOptions();
                                return;
                            }
                            selectedPiece = null;
                            whiteTurn = !whiteTurn;
                            opponentMove = null;

                            synchronized (pieces) {
                                if (!MoveValidator.validateNotMate(yourColor == WHITE ? BLACK : WHITE, pieces)) showMate(Color.GREEN);
                            }

                            try {
                                out.writeObject(lastMove);
                            } catch (IOException e1) {
                                System.err.println("IOException: " + e1.getMessage());
                            }
                        }
                    }
                }
            }
        });
    }

    private void showPromotionOptions() {
        pawnPromotionCanvas.setVisible(true);
        pawnPromotionCanvas.toFront();
        int imgRow = toPromote.getColor() == WHITE ? 0 : 1;
        promotionGC.fillRect(0, 0, SCREEN_DIMENSION, SCREEN_DIMENSION);
        promotionGC.drawImage(IMAGE_PIECES, ROOK_IMG_START_X, imgRow * PIECE_IMG_HEIGHT,
                PIECE_IMG_WIDTH, PIECE_IMG_HEIGHT, PROMOTION_OPTION_OFFSET_X, PROMOTION_OPTION_OFFSET_Y,
                PROMOTION_OPTION_IMG_SIZE, PROMOTION_OPTION_IMG_SIZE);
        promotionGC.drawImage(IMAGE_PIECES, KNIGHT_IMG_START_X, imgRow * PIECE_IMG_HEIGHT,
                PIECE_IMG_WIDTH, PIECE_IMG_HEIGHT, PROMOTION_OPTION_OFFSET_X + PROMOTION_OPTION_IMG_SIZE, PROMOTION_OPTION_OFFSET_Y,
                PROMOTION_OPTION_IMG_SIZE, PROMOTION_OPTION_IMG_SIZE);
        promotionGC.drawImage(IMAGE_PIECES, BISHOP_IMG_START_X, imgRow * PIECE_IMG_HEIGHT,
                PIECE_IMG_WIDTH, PIECE_IMG_HEIGHT, PROMOTION_OPTION_OFFSET_X, PROMOTION_OPTION_OFFSET_Y + PROMOTION_OPTION_IMG_SIZE,
                PROMOTION_OPTION_IMG_SIZE, PROMOTION_OPTION_IMG_SIZE);
        promotionGC.drawImage(IMAGE_PIECES, QUEEN_IMG_START_X, imgRow * PIECE_IMG_HEIGHT,
                PIECE_IMG_WIDTH, PIECE_IMG_HEIGHT, PROMOTION_OPTION_OFFSET_X + PROMOTION_OPTION_IMG_SIZE, PROMOTION_OPTION_OFFSET_Y + PROMOTION_OPTION_IMG_SIZE,
                PROMOTION_OPTION_IMG_SIZE, PROMOTION_OPTION_IMG_SIZE);
    }

    private static Rectangle2D getBoundary(ChessPiece piece) {
        return new Rectangle2D(piece.getCol() * CELL_SIZE, piece.getRow() * CELL_SIZE, CELL_SIZE, CELL_SIZE);
    }

    private static double getPieceX(ChessPiece piece) {
        return piece.getCol() * CELL_SIZE;
    }

    private static double getPieceY(ChessPiece piece) {
        return piece.getRow() * CELL_SIZE;
    }

    private int getCellIndex(double z) {
        int index = 0;
        for (int i = 0; i < 8; i++) {
            if (z > i * CELL_SIZE && z < (i + 1) * CELL_SIZE) {
                index = i;
                break;
            }
        }
        return index;
    }

    private void startServer() {
        try {
            server = new ServerSocket(PORT);
            host = "localhost";
            socket = server.accept();
            in = new ObjectInputStream(socket.getInputStream());
            out = new ObjectOutputStream(socket.getOutputStream());

            Random rand = new Random(System.nanoTime());
            int r = rand.nextInt(2);
            yourColor = r == 0 ? WHITE : BLACK;
            ChessPiece.Color opponentColor = yourColor == WHITE ? BLACK : WHITE;
            out.writeObject(opponentColor);

            gameStart = System.nanoTime();

            connected = true;
            enableReading();
            state = State.SERVER;
        } catch (IOException e) {
            System.err.println("SERVER IOException: " + e.getMessage());
        }
    }

    private void startClient() {
            try {
                socket = new Socket(host, PORT);
                out = new ObjectOutputStream(socket.getOutputStream());
                in = new ObjectInputStream(socket.getInputStream());
                enableReading();
                connected = true;
                state = State.CLIENT;
                conErrorLabel.setVisible(false);
            } catch (IOException e) {
                System.err.println("Client start IOException: " + e.getMessage());
                conErrorLabel.setVisible(true);
            }
    }

    private void enableReading() {
        ExecutorService service = Executors.newSingleThreadExecutor();
        service.execute(() -> {
            while (!socket.isClosed()) {
                try {
                    Object data = in.readObject();

                    if (data instanceof ChessPiece.Color) {
                        yourColor = (ChessPiece.Color) data;
                        gameStart = System.nanoTime();
                    } else if (data instanceof Move) {
                        Move move = (Move) data;
                        if (move != null) {
                            opponentMove = move;
                            opponentMoves.add(opponentMove);

                            synchronized (pieces) {
                                for (ChessPiece piece : pieces) {
                                    if (piece.getCol() == move.getCol() && piece.getRow() == move.getRow() && !piece.isCaptured()) {
                                        piece.move(move.getNewRow(), move.getNewCol(), pieces);
                                        if (move.getPromotedTo() != null) {
                                            pieces.remove(piece);
                                            pieces.add(move.getPromotedTo());
                                        }
                                        whiteTurn = !whiteTurn;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                } catch (IOException e) {
                    System.err.println("Client read IOException: " + e.getMessage());
                    showDCMessage = true;
                    try {
                        socket.close();
                    } catch (IOException ie) {
                        System.err.println("IOException while trying closing socket: " + ie.getMessage());
                    }

                } catch (ClassNotFoundException e) {
                    System.err.println("Client read ClassNotFoundException: " + e.getMessage());
                }
            }
        });
    }

    private void highlightCell(int row, int col, Color color) {
        highlightGC.setFill(color);
        highlightGC.fillRect(col * CELL_SIZE, row * CELL_SIZE, CELL_SIZE, CELL_SIZE);
    }

    private void showMate(Color color) {
        double w = infoCanvas.getWidth(), h = infoCanvas.getHeight();
        infoGC.setFill(color);
        infoGC.clearRect(0, 0, w, h);
        infoGC.fillText("Mate!", SCREEN_DIMENSION / 2, SCREEN_DIMENSION / 2 - 10);
        infoCanvas.setVisible(true);
        infoCanvas.toFront();
    }

    private void showColor() {
        if (yourColor != null) {
            double w = infoCanvas.getWidth(), h = infoCanvas.getHeight();
            infoGC.clearRect(0, 0, w, h);
            infoGC.setFill(Color.LIGHTGRAY);
            infoGC.fillRect(0, 0, w, h);
            infoGC.setFill(Color.BLACK);
            infoGC.fillText("You Play", SCREEN_DIMENSION / 2, 60);
            int imgRow = yourColor == WHITE ? 0 : 1;
            infoGC.drawImage(IMAGE_PIECES, PAWN_IMG_START_X, imgRow * PIECE_IMG_HEIGHT,
                    PIECE_IMG_WIDTH, PIECE_IMG_HEIGHT,
                    (SCREEN_DIMENSION - PROMOTION_OPTION_IMG_SIZE) / 2, (SCREEN_DIMENSION - PROMOTION_OPTION_IMG_SIZE) / 2,
                    PROMOTION_OPTION_IMG_SIZE, PROMOTION_OPTION_IMG_SIZE);
            infoCanvas.setVisible(true);
            infoCanvas.toFront();
        }
    }

    private void hideColor() {
        infoCanvas.setVisible(false);
        piecesCanvas.toFront();
    }

    private void showOpponentDisconnectedMessage() {
        double w = infoCanvas.getWidth(), h = infoCanvas.getHeight();
        infoCanvas.setVisible(true);
        infoCanvas.toFront();
        infoGC.setFill(Color.LIGHTGRAY);
        infoGC.fillRect(0, 0, w, h);
        infoGC.setFill(Color.BLACK);
        infoGC.fillText("It looks as if your opponent got disconnected.", SCREEN_DIMENSION / 2, SCREEN_DIMENSION / 2 - 5, 0.8 * SCREEN_DIMENSION);
    }

    // menu related
    private static class MenuItem extends HBox {
        private Text text;
        private Runnable script;

        public MenuItem(String title) {
            super(15);
            setAlignment(Pos.CENTER);

            text = new Text(title);
            text.setFont(Font.font(18));

            getChildren().addAll(text);
            setActive(false);
            setOnActivate(() -> System.out.println(title + " activated"));
        }

        private void setActive(boolean active) {
            text.setFill(active ? Color.BLUEVIOLET : Color.GRAY);
        }

        public void setOnActivate(Runnable r) {
            script = r;
        }

        public void activate() {
            if (script != null) {
                script.run();
            }
        }
    }

    private Parent createMenuContent() {

        GridPane root = new GridPane();
        root.setAlignment(Pos.CENTER);
        root.setPrefSize(SCREEN_DIMENSION, SCREEN_DIMENSION);

        Label title = new Label("Game of Chess");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 30));

        MenuItem exit = new MenuItem("Exit");
        exit.setOnActivate(() -> System.exit(1));

        MenuItem standalone = new MenuItem("Standalone");
        standalone.setOnActivate(() -> {
            state = State.STANDALONE;
            stage.setScene(gameScene);
        });

        MenuItem create = new MenuItem("Create Game");
        MenuItem connect = new MenuItem("Connect");
        menuBox = new VBox(10, standalone, create, connect, exit);
        menuBox.setAlignment(Pos.CENTER);

        create.setOnActivate(() -> {
            ExecutorService service = Executors.newSingleThreadExecutor();
            service.execute(() -> startServer());
            waitingStage.show();
        });
        connect.setOnActivate(() -> connectingStage.show());

        getMenuItem(0).setActive(true);

        root.add(title, 0, 0);
        root.add(menuBox, 0, 1);

        RowConstraints row1 = new RowConstraints();
        row1.setPercentHeight(30);
        RowConstraints row2 = new RowConstraints();
        row2.setPercentHeight(50);
        root.getRowConstraints().addAll(row1, row2);

        return root;
    }

    private MenuItem getMenuItem(int index) {
        return (MenuItem) menuBox.getChildren().get(index);
    }

    private void initConnectDialog() {
        Label label = new Label("Enter IP to connect to: ");
        label.setFont(Font.font("Arial", FontWeight.NORMAL, 12));
        conErrorLabel = new Label("Can not connect to specified IP.");
        conErrorLabel.setVisible(false);
        conErrorLabel.setTextFill(Color.FIREBRICK);
        TextField tf = new TextField();
        Button cancel = new Button("Cancel");
        Button connect = new Button("Connect");

        EventHandler cancelEvent = e -> {
            conErrorLabel.setVisible(false);
            connectingStage.close();
            tf.setText("");
        };

        cancel.setOnAction(cancelEvent);

        connect.setOnAction(e -> {
            String ip = tf.getText();
            if (ip != null && !ip.isEmpty()) {
                host = ip;
                ExecutorService service = Executors.newSingleThreadExecutor();
                service.execute(() -> startClient());
            }
        });

        GridPane root = new GridPane();
        root.setPrefSize(400, 100);

        root.setAlignment(Pos.CENTER);

        root.add(label, 0, 0);
        root.add(tf, 1, 0);
        root.add(conErrorLabel, 0, 1, 2, 1);
        root.add(cancel, 0, 2);
        root.add(connect, 1, 2);

        connectingScene = new Scene(root);
        connectingStage = new Stage();
        connectingStage.setOnCloseRequest(cancelEvent);
        connectingStage.setScene(connectingScene);

        connectingStage.initOwner(stage);
        connectingStage.initModality(Modality.WINDOW_MODAL);
    }

    private void initWaitDialog() {
        Label waitingLabel = new Label("Waiting for worthy opponent");
        Button cancel = new Button("Cancel");

        EventHandler cancelEvent = e -> {
            waitingStage.close();
            state = State.MENU;
            try {
                server.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        };

        cancel.setOnAction(cancelEvent);

        VBox vb = new VBox(10, waitingLabel, cancel);
        vb.setAlignment(Pos.CENTER);
        vb.setPrefSize(200, 100);
        waitingScene = new Scene(vb);

        waitingStage = new Stage();
        waitingStage.setOnCloseRequest(cancelEvent);
        waitingStage.setScene(waitingScene);

        waitingStage.initOwner(stage);
        waitingStage.initModality(Modality.WINDOW_MODAL);
    }

    private void initMenu() {
        menuScene = new Scene(createMenuContent());

        menuScene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.UP) {
                if (currentItem > 0) {
                    getMenuItem(currentItem).setActive(false);
                    getMenuItem(--currentItem).setActive(true);
                }
            }

            if (e.getCode() == KeyCode.DOWN) {
                if (currentItem < menuBox.getChildren().size() - 1) {
                    getMenuItem(currentItem).setActive(false);
                    getMenuItem(++currentItem).setActive(true);
                }
            }

            if (e.getCode() == KeyCode.ENTER) {
                getMenuItem(currentItem).activate();
            }
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
