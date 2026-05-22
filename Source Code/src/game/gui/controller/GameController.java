package game.gui.controller;

import game.engine.Role;
import game.engine.monsters.*;
import game.engine.cells.*;
import game.gui.SoundManager;
import game.gui.model.GameModel;
import game.gui.view.*;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.*;
import javafx.scene.image.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.scene.text.*;
import javafx.stage.*;
import javafx.util.Duration;

import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Random;

public class GameController {

    private final Stage   mainStage;
    private final Role    playerRole;
    private final boolean multiplayer;
    private GameModel model;

    private BorderPane root;
    private StackPane  rootStack;
    private GameBoardView    boardView;
    private MonsterInfoPanel playerPanel, opponentPanel;
    private VBox leftBox, rightBox;
    private TextArea eventLog;
    private Label turnLabel, diceLabel, currentPlayerLabel;
    private ImageView diceImageView;
    private final Image[] diceImages = new Image[6];
    private final Random diceRandom = new Random();
    private Button rollBtn, powerupBtn;

    // In-scene overlays
    private StackPane cardOverlay;
    private StackPane shieldOverlay;
    private boolean   waitingForOverlay = false;
    private Runnable  afterOverlay      = null;

    private int turnNumber = 1;
    private final Deque<String> logLines = new ArrayDeque<>();

    private static final double SIDE_FRAC = 0.16;
    private static final double SIDE_MIN  = 190.0;

    public GameController(Stage stage, Role role, boolean multiplayer) {
        this.mainStage  = stage;
        this.playerRole = role;
        this.multiplayer = multiplayer;
    }

    // =========================================================================
    //  STARTUP
    // =========================================================================

    public void startGame() {
        try {
            model = new GameModel(playerRole, multiplayer);
        } catch (Exception e) {
            safeMessageStage("Failed to Load Game",
                "Could not load game data:\n" + e.getMessage(), "#3a0a0a", "#FF4444");
            return;
        }
        buildUI();
        refreshAll();
    }

    // =========================================================================
    //  UI CONSTRUCTION
    // =========================================================================

    private void buildUI() {
        root = new BorderPane();
        root.setStyle("-fx-background-color: #0a0a14;");
        loadDiceImages();
        root.setTop(buildTopBar());

        // LEFT
        playerPanel = new MonsterInfoPanel(true);

        // Card deck image - bottom of left panel
        StackPane deckPane = new StackPane();
        deckPane.setAlignment(Pos.CENTER);
        deckPane.setPadding(new Insets(4));
        try {
            InputStream deckIs = getClass().getResourceAsStream(
                "/game/gui/resources/images/card_deck.jpg");
            if (deckIs != null) {
                Image deckImg = new Image(deckIs);
                if (!deckImg.isError()) {
                    ImageView deckView = new ImageView(deckImg);
                    deckView.setFitWidth(220);
                    deckView.setFitHeight(220);
                    deckView.setPreserveRatio(true);
                    deckView.setSmooth(true);
                    deckPane.getChildren().add(deckView);
                }
            }
        } catch (Exception ignored) {}

        leftBox = new VBox(8, buildActionsPanel(), playerPanel, deckPane);
        leftBox.setPadding(new Insets(8));
        leftBox.setStyle("-fx-background-color: #0d0d1f;");
        leftBox.setMinWidth(SIDE_MIN);
        leftBox.setMaxWidth(260);
        VBox.setVgrow(playerPanel, Priority.ALWAYS);
        root.setLeft(leftBox);

        // CENTER
        boardView = new GameBoardView(model);
        boardView.setStyle("-fx-background-color: #07070f;");

        StackPane centerBox = new StackPane(boardView);
        centerBox.setPadding(new Insets(4, 4, 22, 4));
        centerBox.setStyle("-fx-background-color: #07070f;");
        root.setCenter(centerBox);

        // RIGHT
        opponentPanel = new MonsterInfoPanel(false);
        VBox logPanel = buildLogPanel();
        rightBox = new VBox(8, opponentPanel, buildLegendPanel(), logPanel);
        rightBox.setPadding(new Insets(8));
        rightBox.setStyle("-fx-background-color: #0d0d1f;");
        rightBox.setMinWidth(SIDE_MIN);
        rightBox.setMaxWidth(260);
        VBox.setVgrow(logPanel, Priority.ALWAYS);
        root.setRight(rightBox);

        // Overlays
        cardOverlay   = buildCardOverlay();
        shieldOverlay = buildShieldOverlay();
        cardOverlay.setVisible(false);
        shieldOverlay.setVisible(false);

        rootStack = new StackPane(root, shieldOverlay, cardOverlay);

        // Swap root inside the permanent scene with a soft transition.
        game.gui.Main.setRootAnimated(rootStack);

        // Bind sidebar widths to the permanent scene's width
        game.gui.Main.mainScene.widthProperty().addListener((obs, ov, nv) -> {
            double sw    = nv.doubleValue();
            double sideW = Math.max(SIDE_MIN, Math.min(260, sw * SIDE_FRAC));
            leftBox.setPrefWidth(sideW);
            rightBox.setPrefWidth(sideW);
        });

        // Key handler on the permanent scene (replace any previous handler)
        game.gui.Main.mainScene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.F11) {
                mainStage.setFullScreen(!mainStage.isFullScreen());
            } else if (e.getCode() == KeyCode.W) {
                Monster current = model.getCurrent();
                current.setPosition(99);
                addLog("CHEAT: " + current.getName() + " moved to cell 99!");
                refreshAll();
                checkWin();
            } else if (e.getCode() == KeyCode.E) {
                Monster current = model.getCurrent();
                current.setEnergy(current.getEnergy() + 500);
                addLog("CHEAT: " + current.getName() + " gained 500 energy!");
                refreshAll();
                checkWin();
            }
        });

        Platform.runLater(() -> {
            double sw    = game.gui.Main.mainScene.getWidth();
            double sideW = Math.max(SIDE_MIN, Math.min(260, sw * SIDE_FRAC));
            leftBox.setPrefWidth(sideW);
            rightBox.setPrefWidth(sideW);
        });
    }

    // =========================================================================
    //  TOP BAR
    // =========================================================================

    private HBox buildTopBar() {
        HBox bar = new HBox(14);
        bar.setMinHeight(82);
        bar.setPrefHeight(82);
        bar.setMaxHeight(82);
        bar.setPadding(new Insets(14, 18, 10, 18));
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setStyle(
            "-fx-background-color: linear-gradient(to right, #1a0a2e, #0a1f0a, #1a0a2e);" +
            "-fx-border-color: #333366; -fx-border-width: 0 0 2 0;");

        Text title = new Text("DooR DasH: Scare vs Laugh Touchdown");
        title.setFont(Font.font("Impact", 24));
        title.setFill(Color.web("#FFD700"));
        title.setEffect(new Glow(0.4));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        currentPlayerLabel = new Label("");
        currentPlayerLabel.setFont(Font.font("Georgia", FontWeight.BOLD, 14));
        currentPlayerLabel.setTextFill(Color.web("#00FF88"));

        turnLabel = new Label("Turn 1");
        turnLabel.setFont(Font.font("Impact", 18));
        turnLabel.setTextFill(Color.web("#AAAAFF"));

        diceLabel = new Label("Dice: --");
        diceLabel.setFont(Font.font("Impact", 20));
        diceLabel.setTextFill(Color.WHITE);

        diceImageView = new ImageView();
        diceImageView.setFitWidth(54);
        diceImageView.setFitHeight(54);
        diceImageView.setPreserveRatio(true);
        diceImageView.setSmooth(true);
        diceImageView.setImage(diceImages[0]);
        diceImageView.setEffect(new DropShadow(12, Color.web("#FFD700", 0.55)));

        // --- Mute buttons ---
        Button muteMusicBtn = buildMuteButton(
            SoundManager.get().isMusicMuted() ? "Unmute Music" : "Mute Music",
            SoundManager.get().isMusicMuted() ? "#882222" : "#443388");
        muteMusicBtn.setOnAction(e -> {
            SoundManager.get().playClick();
            boolean muted = SoundManager.get().toggleMusicMute();
            muteMusicBtn.setText(muted ? "Unmute Music" : "Mute Music");
            muteMusicBtn.setStyle(buildMuteBtnStyle(muted ? "#882222" : "#443388"));
        });

        Button muteSfxBtn = buildMuteButton(
            SoundManager.get().isSfxMuted() ? "Unmute SFX" : "Mute SFX",
            SoundManager.get().isSfxMuted() ? "#882222" : "#443388");
        muteSfxBtn.setOnAction(e -> {
            SoundManager.get().playClick();
            boolean muted = SoundManager.get().toggleSfxMute();
            muteSfxBtn.setText(muted ? "Unmute SFX" : "Mute SFX");
            muteSfxBtn.setStyle(buildMuteBtnStyle(muted ? "#882222" : "#443388"));
        });

        bar.getChildren().addAll(title, spacer, currentPlayerLabel, turnLabel, diceImageView, diceLabel,
                                 muteMusicBtn, muteSfxBtn);
        return bar;
    }

    private Button buildMuteButton(String label, String bgColor) {
        Button btn = new Button(label);
        btn.setFont(Font.font("Georgia", FontWeight.BOLD, 11));
        btn.setPrefHeight(32);
        btn.setStyle(buildMuteBtnStyle(bgColor));
        return btn;
    }

    private String buildMuteBtnStyle(String bgColor) {
        return "-fx-background-color: " + bgColor + ";" +
               "-fx-text-fill: white;" +
               "-fx-background-radius: 8;" +
               "-fx-cursor: hand;" +
               "-fx-padding: 4 10 4 10;";
    }

    // =========================================================================
    //  ACTION PANELS
    // =========================================================================

    private VBox buildActionsPanel() {
        VBox panel = new VBox(8);
        panel.setPadding(new Insets(10));
        panel.setMaxWidth(Double.MAX_VALUE);
        panel.setStyle(
            "-fx-background-color: #111128;" +
            "-fx-border-color: #333366;" +
            "-fx-border-width: 2;" +
            "-fx-border-radius: 10;" +
            "-fx-background-radius: 10;");

        Label header = new Label("Actions");
        header.setFont(Font.font("Impact", 15));
        header.setTextFill(Color.web("#FFD700"));

        powerupBtn = new Button("Activate Powerup\n(costs 500 energy)");
        powerupBtn.setMaxWidth(Double.MAX_VALUE);
        powerupBtn.setPrefHeight(48);
        powerupBtn.setFont(Font.font("Georgia", FontWeight.BOLD, 11));
        powerupBtn.setStyle(
            "-fx-background-color: linear-gradient(to bottom, #6600AA, #440077);" +
            "-fx-text-fill: white; -fx-background-radius: 10; -fx-cursor: hand;");
        powerupBtn.setOnAction(e -> handlePowerup());

        rollBtn = new Button("Roll Dice");
        rollBtn.setMaxWidth(Double.MAX_VALUE);
        rollBtn.setPrefHeight(48);
        rollBtn.setFont(Font.font("Impact", 17));
        rollBtn.setStyle(
            "-fx-background-color: linear-gradient(to bottom, #00AA44, #006622);" +
            "-fx-text-fill: white; -fx-background-radius: 10; -fx-cursor: hand;");
        rollBtn.setOnAction(e -> { SoundManager.get().playClick(); handleRoll(); });

        Button menuBtn = new Button("MAIN MENU");
        menuBtn.setMaxWidth(Double.MAX_VALUE);
        menuBtn.setPrefHeight(38);
        menuBtn.setFont(Font.font("Impact", 14));
        menuBtn.setStyle(
            "-fx-background-color:rgba(40,70,150,0.95);" +
            "-fx-text-fill:white;-fx-background-radius:10;-fx-cursor:hand;");
        menuBtn.setOnAction(e -> {
            SoundManager.get().playClick();
            waitingForOverlay = false;
            afterOverlay = null;
            if (boardView != null) {
                boardView.clearHeldMonster();
                boardView.unfreezeCellVisualState();
            }
            game.gui.Main.mainScene.setOnKeyPressed(ev -> {
                if (ev.getCode() == KeyCode.F11)
                    mainStage.setFullScreen(!mainStage.isFullScreen());
            });
            new StartScreenView(mainStage).show();
        });

        Button exitBtn = new Button("EXIT GAME");
        exitBtn.setMaxWidth(Double.MAX_VALUE);
        exitBtn.setPrefHeight(38);
        exitBtn.setFont(Font.font("Impact", 14));
        exitBtn.setStyle(
            "-fx-background-color:rgba(160,20,20,0.9);" +
            "-fx-text-fill:white;-fx-background-radius:10;-fx-cursor:hand;");
        exitBtn.setOnAction(e -> {
            SoundManager.get().playClick();
            StackPane overlay = StartScreenView.buildExitOverlay(
                () -> { game.gui.Main.stopMusic(); mainStage.close(); },
                () -> rootStack.getChildren().remove(rootStack.getChildren().get(rootStack.getChildren().size() - 1))
            );
            rootStack.getChildren().add(overlay);
        });

        panel.getChildren().addAll(header, powerupBtn, rollBtn, menuBtn, exitBtn);
        return panel;
    }

    private VBox buildLegendPanel() {
        VBox panel = new VBox(3);
        panel.setPadding(new Insets(7));
        panel.setMaxWidth(Double.MAX_VALUE);
        panel.setStyle(
            "-fx-background-color: #111128; -fx-border-color: #333366;" +
            "-fx-border-width: 1; -fx-border-radius: 8; -fx-background-radius: 8;");

        Label title = new Label("Legend");
        title.setFont(Font.font("Impact", 12));
        title.setTextFill(Color.web("#FFD700"));
        panel.getChildren().add(title);

        panel.getChildren().addAll(
            legendRow("#4A1080", "Scarer Door"),
            legendRow("#0A5C28", "Laugher Door"),
            legendRow("#000000", "Used Door (Black)"),
            legendRow("#7A3B00", "Scarer Monster Cell"),
            legendRow("#004A5C", "Laugher Monster Cell"),
            legendRow("#7A0000", "Card Cell"),
            legendRow("#003A7A", "Conveyor Belt"),
            legendRow("#C04000", "Contamination Sock"),
            legendRow("#003830", "Start"),
            legendRow("#6B4A00", "Boo's Door (Win)"),
            legendRow("#1A1A30", "Normal Cell")
        );
        return panel;
    }

    private HBox legendRow(String color, String label) {
        HBox row = new HBox(5);
        row.setAlignment(Pos.CENTER_LEFT);
        Rectangle rect = new Rectangle(11, 11, Color.web(color));
        rect.setArcWidth(3); rect.setArcHeight(3);
        rect.setStroke(Color.web("#666688")); rect.setStrokeWidth(0.5);
        Label lbl = new Label(label);
        lbl.setFont(Font.font("Georgia", 9));
        lbl.setTextFill(Color.web("#CCCCCC"));
        row.getChildren().addAll(rect, lbl);
        return row;
    }

    private VBox buildLogPanel() {
        VBox panel = new VBox(4);
        panel.setPadding(new Insets(7));
        VBox.setVgrow(panel, Priority.ALWAYS);

        Label title = new Label("Event Log");
        title.setFont(Font.font("Impact", 12));
        title.setTextFill(Color.web("#FFD700"));

        eventLog = new TextArea();
        eventLog.setEditable(false);
        eventLog.setWrapText(true);
        eventLog.setStyle(
            "-fx-control-inner-background: #080818;" +
            "-fx-text-fill: #AADDFF;" +
            "-fx-font-family: Consolas;" +
            "-fx-font-size: 10;" +
            "-fx-border-color: #333366;");
        VBox.setVgrow(eventLog, Priority.ALWAYS);

        panel.getChildren().addAll(title, eventLog);
        return panel;
    }

    // =========================================================================
    //  CARD OVERLAY  (in-scene, animated, with card image)
    // =========================================================================

    private StackPane buildCardOverlay() {
        StackPane overlay = new StackPane();
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.88);");
        overlay.setPickOnBounds(true);

        // Full-screen layout
        VBox card = new VBox(24);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(40));
        card.setMaxWidth(Double.MAX_VALUE);
        card.setMaxHeight(Double.MAX_VALUE);
        card.setPrefWidth(Double.MAX_VALUE);
        card.setPrefHeight(Double.MAX_VALUE);
        card.setStyle("-fx-background-color: transparent;");

        Label header = new Label("CARD DRAWN!");
        header.setFont(Font.font("Impact", 28));
        header.setTextFill(Color.web("#FF8844"));
        header.setEffect(new Glow(0.5));

        // Large card image
        ImageView cardImg = new ImageView();
        cardImg.setId("ov-img");
        cardImg.setFitWidth(620);
        cardImg.setFitHeight(520);
        cardImg.setPreserveRatio(true);
        cardImg.setSmooth(true);
        cardImg.setEffect(new DropShadow(60, Color.web("#FFD700", 0.8)));

        Label cardName = new Label();
        cardName.setId("ov-name");
        cardName.setFont(Font.font("Impact", 36));
        cardName.setTextFill(Color.web("#FFD700"));
        cardName.setEffect(new Glow(0.6));
        cardName.setWrapText(true);
        cardName.setAlignment(Pos.CENTER);
        cardName.setTextAlignment(TextAlignment.CENTER);

        Label effectLbl = new Label();
        effectLbl.setId("ov-effect");
        effectLbl.setFont(Font.font("Georgia", FontWeight.BOLD, 18));
        effectLbl.setTextFill(Color.web("#CCDDFF"));
        effectLbl.setWrapText(true);
        effectLbl.setAlignment(Pos.CENTER);
        effectLbl.setTextAlignment(TextAlignment.CENTER);
        effectLbl.setMaxWidth(900);

        Button dismissBtn = new Button("OK - Got it!");
        dismissBtn.setFont(Font.font("Impact", 22));
        dismissBtn.setPrefSize(220, 56);
        dismissBtn.setDefaultButton(true);
        dismissBtn.setStyle(
            "-fx-background-color: linear-gradient(to right, #FFD700, #FF8C00);" +
            "-fx-text-fill: #1a1a1a;" +
            "-fx-background-radius: 28;" +
            "-fx-cursor: hand;");
        dismissBtn.setOnAction(e -> { SoundManager.get().playClick(); dismissOverlay(cardOverlay); });

        card.getChildren().addAll(header, cardImg, cardName, effectLbl, dismissBtn);
        overlay.getChildren().add(card);
        overlay.setUserData(card);
        return overlay;
    }

    // =========================================================================
    //  SHIELD BREAK OVERLAY
    // =========================================================================

    private StackPane buildShieldOverlay() {
        StackPane overlay = new StackPane();
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.88);");
        overlay.setPickOnBounds(true);

        // Full-screen layout matching card overlay
        VBox card = new VBox(24);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(40));
        card.setMaxWidth(Double.MAX_VALUE);
        card.setMaxHeight(Double.MAX_VALUE);
        card.setPrefWidth(Double.MAX_VALUE);
        card.setPrefHeight(Double.MAX_VALUE);
        card.setStyle("-fx-background-color: transparent;");

        Label header = new Label("SHIELD SHATTERED!");
        header.setFont(Font.font("Impact", 28));
        header.setTextFill(Color.web("#FFFFFF"));
        header.setEffect(new Glow(0.5));

        // Large broken shield image
        ImageView shieldImg = new ImageView();
        shieldImg.setFitWidth(800);
        shieldImg.setFitHeight(800);
        shieldImg.setPreserveRatio(true);
        shieldImg.setSmooth(true);
        shieldImg.setEffect(new DropShadow(60, Color.web("#FFFFFF", 0.8)));
        // Load broken_shield image
        try {
            InputStream is = getClass().getResourceAsStream("/game/gui/resources/images/broken_shield.jpg");
            if (is != null) {
                Image img = new Image(is);
                if (!img.isError()) shieldImg.setImage(img);
            }
        } catch (Exception ignored) {}

        Label sub = new Label();
        sub.setId("sh-msg");
        sub.setFont(Font.font("Georgia", FontWeight.BOLD, 18));
        sub.setTextFill(Color.web("#CCDDFF"));
        sub.setWrapText(true);
        sub.setAlignment(Pos.CENTER);
        sub.setTextAlignment(TextAlignment.CENTER);

        Button ok = new Button("OK - Got it!");
        ok.setFont(Font.font("Impact", 22));
        ok.setPrefSize(220, 56);
        ok.setDefaultButton(true);
        ok.setStyle(
            "-fx-background-color: linear-gradient(to right, #FFFFFF, #CCCCCC);" +
            "-fx-text-fill: #1a1a1a;" +
            "-fx-background-radius: 28;" +
            "-fx-cursor: hand;");
        ok.setOnAction(e -> { SoundManager.get().playClick(); dismissOverlay(shieldOverlay); });

        card.getChildren().addAll(header, shieldImg, sub, ok);
        overlay.getChildren().add(card);
        overlay.setUserData(card);
        return overlay;
    }

    // ---- Show / dismiss overlays ----

    private void showCardOverlay(String name, String type, String effect, String imgKey, Runnable onDismiss) {
        Label nameL   = (Label) cardOverlay.lookup("#ov-name");
        Label effectL = (Label) cardOverlay.lookup("#ov-effect");
        ImageView imgV = (ImageView) cardOverlay.lookup("#ov-img");

        Image cardBack = loadImageResource("/game/gui/resources/images/card_deck.jpg");
        Image cardFace = boardView.getCardImage(name);

        if (nameL   != null) { nameL.setText(""); nameL.setOpacity(0); }
        if (effectL != null) { effectL.setText(""); effectL.setOpacity(0); }
        if (imgV    != null) {
            imgV.setImage(cardBack != null ? cardBack : cardFace);
            imgV.setVisible(true);
            imgV.setScaleX(1.0);
            imgV.setScaleY(1.0);
            imgV.setRotate(-6);
        }

        animateCardReveal(name, effect, cardFace, onDismiss);
    }

    private void animateCardReveal(String name, String effect, Image cardFace, Runnable onDismiss) {
        afterOverlay      = onDismiss;
        waitingForOverlay = true;
        cardOverlay.setVisible(true);
        cardOverlay.setOpacity(1.0);

        // Card-flip sound
        SoundManager.get().playCardFlip();

        VBox card = (VBox) cardOverlay.getUserData();
        Label nameL   = (Label) cardOverlay.lookup("#ov-name");
        Label effectL = (Label) cardOverlay.lookup("#ov-effect");
        ImageView imgV = (ImageView) cardOverlay.lookup("#ov-img");

        card.setScaleX(0.75);
        card.setScaleY(0.75);
        card.setOpacity(0.0);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(650), card);
        fadeIn.setToValue(1.0);
        ScaleTransition zoomIn = new ScaleTransition(Duration.millis(650), card);
        zoomIn.setToX(1.0);
        zoomIn.setToY(1.0);

        RotateTransition tilt = new RotateTransition(Duration.millis(650), imgV);
        tilt.setFromAngle(-6);
        tilt.setToAngle(0);

        ScaleTransition closeFlip = new ScaleTransition(Duration.millis(850), imgV);
        closeFlip.setFromX(1.0);
        closeFlip.setToX(0.0);

        closeFlip.setOnFinished(e -> {
            if (imgV != null && cardFace != null) imgV.setImage(cardFace);
            if (nameL != null) {
                nameL.setText(name);
                nameL.setOpacity(1.0);
            }
            if (effectL != null) {
                effectL.setText(effect);
                effectL.setOpacity(1.0);
            }
        });

        ScaleTransition openFlip = new ScaleTransition(Duration.millis(950), imgV);
        openFlip.setFromX(0.0);
        openFlip.setToX(1.0);

        ScaleTransition pop = new ScaleTransition(Duration.millis(380), imgV);
        pop.setFromX(1.0); pop.setToX(1.08);
        pop.setFromY(1.0); pop.setToY(1.08);
        pop.setAutoReverse(true);
        pop.setCycleCount(2);

        SequentialTransition sequence = new SequentialTransition(
            new ParallelTransition(fadeIn, zoomIn, tilt),
            closeFlip,
            openFlip,
            pop
        );
        sequence.play();
    }

    public void showShieldBreak(String monsterName, Runnable onDismiss) {
        Label sub = (Label) shieldOverlay.lookup("#sh-msg");
        if (sub != null)
            sub.setText(monsterName + "'s shield absorbed the hit and shattered into pieces!");
        SoundManager.get().playShieldBreak();
        animateOverlayIn(shieldOverlay, onDismiss);
    }

    private void animateOverlayIn(StackPane overlay, Runnable onDismiss) {
        afterOverlay      = onDismiss;
        waitingForOverlay = true;
        overlay.setVisible(true);

        VBox card = (VBox) overlay.getUserData();
        card.setScaleX(0.3); card.setScaleY(0.3); card.setOpacity(0.0);
        ScaleTransition st = new ScaleTransition(Duration.millis(250), card);
        st.setToX(1.0); st.setToY(1.0);
        FadeTransition ft = new FadeTransition(Duration.millis(250), card);
        ft.setToValue(1.0);
        new ParallelTransition(st, ft).play();
    }

    private void dismissOverlay(StackPane overlay) {
        if (!waitingForOverlay) return;
        waitingForOverlay = false;
        VBox card = (VBox) overlay.getUserData();
        ScaleTransition st = new ScaleTransition(Duration.millis(180), card);
        st.setToX(0.3); st.setToY(0.3);
        FadeTransition ft = new FadeTransition(Duration.millis(180), card);
        ft.setToValue(0.0);
        ParallelTransition pt = new ParallelTransition(st, ft);
        pt.setOnFinished(e -> {
            overlay.setVisible(false);
            Runnable cb = afterOverlay;
            afterOverlay = null;
            if (cb != null) cb.run();
        });
        pt.play();
    }

    // =========================================================================
    //  ACTION HANDLERS
    // =========================================================================

    private void handlePowerup() {
        if (waitingForOverlay) return;
        String err = model.usePowerup();
        if (err != null) {
            SoundManager.get().playError();
            safeMessageStage("Powerup Failed", err, "#3a1a0a", "#FFB300");
        } else {
            SoundManager.get().playBoing();
            Monster current = model.getCurrent();
            String powerupName = getPowerupName(current);
            String powerupDesc = getPowerupDesc(current);
            showPowerupPopup(current.getName(), powerupName, powerupDesc);
            addLog("POWERUP: " + current.getName() + " activated their powerup!");
            refreshAll();
        }
    }

    private String getPowerupName(Monster m) {
        if (m instanceof game.engine.monsters.Dasher)      return "Momentum Rush";
        if (m instanceof game.engine.monsters.Dynamo)      return "Energy Freeze";
        if (m instanceof game.engine.monsters.MultiTasker) return "Focus Mode";
        if (m instanceof game.engine.monsters.Schemer)     return "Chain Attack";
        return "Powerup";
    }

    private String getPowerupDesc(Monster m) {
        if (m instanceof game.engine.monsters.Dasher)
            return "Moves at 3x speed for the next 3 turns!";
        if (m instanceof game.engine.monsters.Dynamo)
            return "Opponent is FROZEN and must skip their next turn!";
        if (m instanceof game.engine.monsters.MultiTasker)
            return "Moves at normal speed (not halved) for the next 2 turns!";
        if (m instanceof game.engine.monsters.Schemer)
            return "Steals 10 energy from every other monster on the board!";
        return "Powerup activated!";
    }

    private void showPowerupPopup(String monsterName, String powerupName, String desc) {
        StackPane overlay = new StackPane();
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.88);");
        overlay.setPickOnBounds(true);

        // Full-screen layout matching card overlay exactly
        VBox card = new VBox(24);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(40));
        card.setMaxWidth(Double.MAX_VALUE);
        card.setMaxHeight(Double.MAX_VALUE);
        card.setPrefWidth(Double.MAX_VALUE);
        card.setPrefHeight(Double.MAX_VALUE);
        card.setStyle("-fx-background-color: transparent;");

        Label header = new Label("POWERUP ACTIVATED!");
        header.setFont(Font.font("Impact", 28));
        header.setTextFill(Color.web("#FFD700"));
        header.setEffect(new Glow(0.5));

        // Powerup image
        ImageView powerImg = new ImageView();
        powerImg.setFitWidth(800);
        powerImg.setFitHeight(800);
        powerImg.setPreserveRatio(true);
        powerImg.setSmooth(true);
        powerImg.setEffect(new DropShadow(60, Color.web("#FFD700", 0.8)));
        try {
            InputStream is = getClass().getResourceAsStream(
                "/game/gui/resources/images/powerup.jpg");
            if (is != null) {
                Image img = new Image(is);
                if (!img.isError()) powerImg.setImage(img);
            }
        } catch (Exception ignored) {}

        // Powerup name
        Label nameLabel = new Label(monsterName + " - " + powerupName);
        nameLabel.setFont(Font.font("Impact", 36));
        nameLabel.setTextFill(Color.web("#FFD700"));
        nameLabel.setEffect(new Glow(0.6));
        nameLabel.setWrapText(true);
        nameLabel.setAlignment(Pos.CENTER);
        nameLabel.setTextAlignment(TextAlignment.CENTER);

        // Description
        Label descLabel = new Label(desc);
        descLabel.setFont(Font.font("Georgia", FontWeight.BOLD, 18));
        descLabel.setTextFill(Color.web("#CCDDFF"));
        descLabel.setWrapText(true);
        descLabel.setAlignment(Pos.CENTER);
        descLabel.setTextAlignment(TextAlignment.CENTER);

        Button ok = new Button("OK - Got it!");
        ok.setFont(Font.font("Impact", 22));
        ok.setPrefSize(220, 56);
        ok.setDefaultButton(true);
        ok.setStyle(
            "-fx-background-color: linear-gradient(to right, #FFD700, #FF8C00);" +
            "-fx-text-fill: #1a1a1a;" +
            "-fx-background-radius: 28;" +
            "-fx-cursor: hand;");
        ok.setOnAction(e -> { SoundManager.get().playClick(); rootStack.getChildren().remove(overlay); });

        card.getChildren().addAll(header, powerImg, nameLabel, descLabel, ok);
        overlay.getChildren().add(card);
        rootStack.getChildren().add(overlay);
    }

    private void handleRoll() {
        if (waitingForOverlay) return;
        boardView.clearHeldMonster();
        boardView.freezeCellVisualState();
        setButtonsEnabled(false);

        // Track shield status before turn
        boolean playerShieldBefore   = model.getPlayer().isShielded();
        boolean opponentShieldBefore = model.getOpponent().isShielded();
        Monster movingMonsterBeforeTurn = model.getCurrent();
        int movingMonsterStartPosition = movingMonsterBeforeTurn.getPosition();

        // Snapshot stationed monster energies BEFORE turn for change detection
        java.util.Map<String,Integer> stationedEnergyBefore = snapshotStationedEnergies();

        String result;
        try { result = model.playTurn(); }
        catch (Exception ex) {
            boardView.unfreezeCellVisualState();
            safeMessageStage("Engine Error", ex.getClass().getSimpleName() + ": " + ex.getMessage(), "#3a0a0a", "#FF4444");
            setButtonsEnabled(true);
            return;
        }

        if ("FROZEN".equals(result)) {
            boardView.unfreezeCellVisualState();
            String msg = model.getCurrent().getName() + " is FROZEN and skips their turn!";
            addLog("FROZEN: " + msg);
            diceLabel.setText("Dice: FROZEN");
            refreshAll();
            setButtonsEnabled(true);
            checkWin();
            return;
        }
        if ("INVALID".equals(result)) {
            boardView.unfreezeCellVisualState();
            SoundManager.get().playError();
            addLog("INVALID: Cell occupied - roll again.");
            safeMessageStage("Invalid Move", "That cell is occupied by the opponent!\nPlease roll again.", "#3a2a0a", "#FFB300");
            setButtonsEnabled(true);
            return;
        }

        int roll = model.getLastDiceRoll();
        boardView.holdMonsterAt(movingMonsterBeforeTurn, movingMonsterStartPosition);
        // Compute actual steps from log - read from player's position change
        Monster moved = model.getCurrent() == model.getPlayer() ? model.getOpponent() : model.getPlayer();
        // current has already switched after playTurn, so the monster that just moved is the opponent of current
        animateDice(roll, () -> {
            int toPos = movingMonsterBeforeTurn.getPosition();
            // Compute actual steps moved for the animation path builder.
            // This is the true distance so the token does not overshoot and backtrack
            // (important for MultiTaskers who move half the dice roll, and Dashers who move double).
            int actualStepsMoved;
            if (toPos >= movingMonsterStartPosition) {
                actualStepsMoved = toPos - movingMonsterStartPosition;
            } else if (movingMonsterStartPosition + roll >= 100) {
                // forward wrap
                actualStepsMoved = (100 - movingMonsterStartPosition) + toPos;
            } else {
                // backward movement (contamination sock / start-over card)
                actualStepsMoved = movingMonsterStartPosition - toPos;
            }
            boardView.animateMonsterMove(movingMonsterBeforeTurn, movingMonsterStartPosition,
                toPos, actualStepsMoved, () -> {

                // Landing sound + particles when monster reaches its cell
                SoundManager.get().playLanding();
                boardView.spawnParticles(movingMonsterBeforeTurn.getPosition(),
                    javafx.scene.paint.Color.web("#FFD700"), 10);

                // Screen shake on contamination sock or energy loss
                String logPreview = model.getLastLogEntry();
                if (logPreview != null && (logPreview.toLowerCase().contains("sock")
                        || logPreview.toLowerCase().contains("energy")
                        || logPreview.toLowerCase().contains("contaminat"))) {
                    boardView.screenShake();
                }

                boardView.unfreezeCellVisualState();
                refreshAll();
                String logEntry = model.getLastLogEntry();
                addLog(logEntry);

                // Log any stationed monster energy changes
                logStationedEnergyChanges(stationedEnergyBefore);

                // Check if shield was broken this turn
                boolean playerShieldBroken   = playerShieldBefore   && !model.getPlayer().isShielded();
                boolean opponentShieldBroken = opponentShieldBefore && !model.getOpponent().isShielded();
                String shieldBrokenName = playerShieldBroken
                    ? model.getPlayer().getName()
                    : (opponentShieldBroken ? model.getOpponent().getName() : null);

                boolean hasCard = !model.getLastCardDrawn().isEmpty();

                Runnable afterCard = () -> {
                    if (shieldBrokenName != null) {
                        addLog("SHIELD BREAK: " + shieldBrokenName + "'s shield was shattered!");
                        showShieldBreak(shieldBrokenName, this::finishTurn);
                    } else {
                        finishTurn();
                    }
                };

                if (hasCard) {
                    String cn = model.getLastCardDrawn();
                    String ct = model.getLastCardType();
                    String ce = model.getLastCardEffect();
                    addLog("CARD: " + cn + " (" + ct + "): " + ce);
                    showCardOverlay(cn, ct, ce, cn, afterCard);
                } else {
                    afterCard.run();
                }
            });
        });
    }

    private void finishTurn() {
        turnNumber++;
        turnLabel.setText("Turn " + turnNumber);
        refreshAll();
        setButtonsEnabled(true);
        checkWin();

        if (!multiplayer && model.getCurrent() == model.getOpponent()) {
            setButtonsEnabled(false);
            PauseTransition botDelay = new PauseTransition(Duration.seconds(1.0));
            botDelay.setOnFinished(ev -> playBotTurn());
            botDelay.play();
        }
    }

    private void playBotTurn() {
        boardView.clearHeldMonster();
        boardView.freezeCellVisualState();
        boolean playerShieldBefore   = model.getPlayer().isShielded();
        boolean opponentShieldBefore = model.getOpponent().isShielded();
        Monster movingMonsterBeforeTurn = model.getCurrent();
        int movingMonsterStartPosition = movingMonsterBeforeTurn.getPosition();

        java.util.Map<String,Integer> stationedBefore = snapshotStationedEnergies();
        String result;
        try { result = model.playTurn(); }
        catch (Exception ex) {
            boardView.unfreezeCellVisualState();
            safeMessageStage("Bot Error", ex.getMessage(), "#3a0a0a", "#FF4444");
            setButtonsEnabled(true);
            return;
        }

        if ("FROZEN".equals(result)) {
            boardView.unfreezeCellVisualState();
            addLog("FROZEN: Bot (" + model.getOpponent().getName() + ") skips turn.");
            diceLabel.setText("Dice: FROZEN");
            finishBotTurn(playerShieldBefore, opponentShieldBefore);
            return;
        }
        if ("INVALID".equals(result)) {
            boardView.unfreezeCellVisualState();
            addLog("BOT-RETRY: re-rolling...");
            PauseTransition retryDelay = new PauseTransition(Duration.millis(400));
            retryDelay.setOnFinished(e -> playBotTurn());
            retryDelay.play();
            return;
        }

        int botRoll = model.getLastDiceRoll();
        boardView.holdMonsterAt(movingMonsterBeforeTurn, movingMonsterStartPosition);
        animateDice(botRoll, () -> {
            boardView.animateMonsterMove(movingMonsterBeforeTurn, movingMonsterStartPosition,
                movingMonsterBeforeTurn.getPosition(), botRoll, () -> {

                // Landing sound + particles
                SoundManager.get().playLanding();
                boardView.spawnParticles(movingMonsterBeforeTurn.getPosition(),
                    javafx.scene.paint.Color.web("#FF6622"), 10);

                String botLog = model.getLastLogEntry();
                if (botLog != null && (botLog.toLowerCase().contains("sock")
                        || botLog.toLowerCase().contains("energy")
                        || botLog.toLowerCase().contains("contaminat"))) {
                    boardView.screenShake();
                }

                boardView.unfreezeCellVisualState();
                refreshAll();
                addLog("BOT: " + model.getLastLogEntry());
                logStationedEnergyChanges(stationedBefore);

                boolean hasCard = !model.getLastCardDrawn().isEmpty();
                boolean playerShieldBroken   = playerShieldBefore   && !model.getPlayer().isShielded();
                boolean opponentShieldBroken = opponentShieldBefore && !model.getOpponent().isShielded();
                String shieldBrokenName = playerShieldBroken
                    ? model.getPlayer().getName()
                    : (opponentShieldBroken ? model.getOpponent().getName() : null);

                Runnable afterCard = () -> {
                    if (shieldBrokenName != null) {
                        addLog("SHIELD BREAK: " + shieldBrokenName + "'s shield was shattered!");
                        showShieldBreak(shieldBrokenName, () -> finishBotTurn(false, false));
                    } else {
                        finishBotTurn(false, false);
                    }
                };

                if (hasCard) {
                    String cn = model.getLastCardDrawn();
                    String ct = model.getLastCardType();
                    String ce = model.getLastCardEffect();
                    addLog("BOT-CARD: " + cn + " (" + ct + "): " + ce);
                    showCardOverlay("[BOT] " + cn, ct, ce, cn, afterCard);
                } else {
                    afterCard.run();
                }
            });
        });
    }

    private void finishBotTurn(boolean ps, boolean os) {
        turnNumber++;
        turnLabel.setText("Turn " + turnNumber);
        refreshAll();
        setButtonsEnabled(true);
        checkWin();
    }

    // Guard: once the win screen is shown, never show it again for this game session.
    private boolean gameOver = false;

    private void checkWin() {
        if (gameOver) return;          // already handled — ignore duplicate calls
        Monster winner = model.getWinner();
        if (winner != null) {
            gameOver = true;           // set before anything async so re-entrant calls are blocked
            Monster loser     = (winner == model.getPlayer()) ? model.getOpponent() : model.getPlayer();
            boolean playerWon = (winner == model.getPlayer());
            addLog("WIN: " + winner.getName() + " wins with " + winner.getEnergy() + " energy!");
            SoundManager.get().pauseMusicForWinScreen();
            if (!multiplayer && !playerWon) {
                SoundManager.get().playLoseScreenEffect();
            } else {
                SoundManager.get().playWinScreenEffect();
            }
            new WinScreenView(mainStage, winner, loser, playerWon, multiplayer).show();
        }
    }

    // =========================================================================
    //  REFRESH
    // =========================================================================

    private void refreshAll() {
        Monster current  = model.getCurrent();
        Monster player   = model.getPlayer();
        Monster opponent = model.getOpponent();
        playerPanel.update(player,   current == player);
        opponentPanel.update(opponent, current == opponent);
        boardView.refreshFull();

        boolean playerTurn = (current == player);
        String who = multiplayer
            ? (playerTurn ? "P1 Turn: " + player.getName() : "P2 Turn: " + opponent.getName())
            : (playerTurn ? "Your Turn" : "Bot's Turn");
        currentPlayerLabel.setText(who);
        currentPlayerLabel.setTextFill(playerTurn ? Color.web("#00FF88") : Color.web("#FF6622"));
    }

    // =========================================================================
    //  HELPERS
    // =========================================================================

    private void addLog(String line) {
        logLines.addFirst("[T" + turnNumber + "] " + line);
        if (logLines.size() > 120) logLines.removeLast();
        StringBuilder sb = new StringBuilder();
        logLines.forEach(l -> sb.append(l).append("\n"));
        eventLog.setText(sb.toString());
        eventLog.positionCaret(0);
    }

    private void setButtonsEnabled(boolean enabled) {
        boolean ok = enabled && !waitingForOverlay;
        rollBtn.setDisable(!ok);
        powerupBtn.setDisable(!ok);
    }

    private void animateDice(int roll, Runnable onFinish) {
        Timeline tl = new Timeline();
        for (int i = 0; i < 12; i++) {
            final int face = diceRandom.nextInt(6) + 1;
            tl.getKeyFrames().add(new KeyFrame(Duration.millis(i * 65), e -> {
                diceLabel.setText("Dice: ?");
                setDiceFace(face);
                diceImageView.setRotate(diceImageView.getRotate() + 35);
            }));
        }
        tl.getKeyFrames().add(new KeyFrame(Duration.millis(820), e -> {
            diceLabel.setText("Dice: " + roll);
            setDiceFace(roll);
            diceImageView.setRotate(0);
        }));
        tl.setOnFinished(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(220), diceImageView);
            st.setFromX(1); st.setToX(1.35); st.setFromY(1); st.setToY(1.35);
            st.setAutoReverse(true); st.setCycleCount(2);
            st.setOnFinished(ev -> onFinish.run());
            st.play();
        });
        tl.play();
    }

    private void setDiceFace(int face) {
        if (diceImageView == null) return;
        int index = Math.max(1, Math.min(6, face)) - 1;
        if (diceImages[index] != null) diceImageView.setImage(diceImages[index]);
    }

    private void loadDiceImages() {
        for (int i = 1; i <= 6; i++) {
            diceImages[i - 1] = loadImageResource("/game/gui/resources/images/dice" + i + ".png");
        }
    }

    private Image loadImageResource(String path) {
        try {
            InputStream is = getClass().getResourceAsStream(path);
            if (is != null) {
                Image img = new Image(is);
                if (!img.isError()) return img;
            }
        } catch (Exception ignored) {}
        return null;
    }

    private ImageView loadIconImage(String key, double size) {
        String[] exts = {".png", ".jpg", ".jpeg"};
        for (String ext : exts) {
            try {
                InputStream is = getClass().getResourceAsStream(
                    "/game/gui/resources/images/" + key + ext);
                if (is != null) {
                    Image img = new Image(is);
                    if (!img.isError()) {
                        ImageView iv = new ImageView(img);
                        iv.setFitWidth(size); iv.setFitHeight(size);
                        iv.setPreserveRatio(true); iv.setSmooth(true);
                        return iv;
                    }
                }
            } catch (Exception ignored) {}
        }
        return null;
    }

    /** Snapshot energies of all stationed monsters for change detection. */
    private java.util.Map<String,Integer> snapshotStationedEnergies() {
        java.util.Map<String,Integer> snap = new java.util.HashMap<>();
        game.engine.cells.Cell[][] cells = model.getBoardCells();
        for (int r = 0; r < 10; r++) {
            for (int c = 0; c < 10; c++) {
                if (cells[r][c] instanceof game.engine.cells.MonsterCell) {
                    game.engine.cells.MonsterCell mc = (game.engine.cells.MonsterCell) cells[r][c];
                    if (mc.getCellMonster() != null)
                        snap.put(mc.getCellMonster().getName(), mc.getCellMonster().getEnergy());
                }
            }
        }
        return snap;
    }

    /** Compare current stationed energies against snapshot and log any changes. */
    private void logStationedEnergyChanges(java.util.Map<String,Integer> before) {
        game.engine.cells.Cell[][] cells = model.getBoardCells();
        for (int r = 0; r < 10; r++) {
            for (int c = 0; c < 10; c++) {
                if (cells[r][c] instanceof game.engine.cells.MonsterCell) {
                    game.engine.cells.MonsterCell mc = (game.engine.cells.MonsterCell) cells[r][c];
                    if (mc.getCellMonster() == null) continue;
                    String name = mc.getCellMonster().getName();
                    int now = mc.getCellMonster().getEnergy();
                    Integer was = before.get(name);
                    if (was != null && was != now) {
                        int diff = now - was;
                        addLog("STATIONED: " + name + (diff > 0 ? " gained +" : " lost ") + diff
                            + " energy (now " + now + ")");
                    }
                }
            }
        }
        boardView.refreshFull(); // refresh board so stationed energy labels update
    }

    private void safeMessageStage(String title, String message, String bgColor, String accent) {
        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.initOwner(mainStage);
        popup.setTitle(title);
        popup.setResizable(false);

        VBox content = new VBox(14);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(28, 36, 28, 36));
        content.setPrefWidth(420);
        content.setStyle(
            "-fx-background-color: " + bgColor + ";" +
            "-fx-border-color: " + accent + ";" +
            "-fx-border-width: 2;" +
            "-fx-border-radius: 12;" +
            "-fx-background-radius: 12;");

        Label titleLbl = new Label(title);
        titleLbl.setFont(Font.font("Impact", 20));
        titleLbl.setTextFill(Color.web(accent));

        Label msgLbl = new Label(message);
        msgLbl.setFont(Font.font("Georgia", 14));
        msgLbl.setTextFill(Color.WHITE);
        msgLbl.setWrapText(true); msgLbl.setMaxWidth(360);
        msgLbl.setAlignment(Pos.CENTER); msgLbl.setTextAlignment(TextAlignment.CENTER);

        Button okBtn = new Button("OK");
        okBtn.setFont(Font.font("Impact", 16)); okBtn.setPrefSize(110, 38);
        okBtn.setDefaultButton(true);
        okBtn.setStyle("-fx-background-color:" + accent + ";-fx-text-fill:#1a1a1a;" +
                       "-fx-background-radius:20;-fx-cursor:hand;");
        okBtn.setOnAction(e -> { SoundManager.get().playClick(); popup.close(); });

        content.getChildren().addAll(titleLbl, msgLbl, okBtn);
        popup.setScene(new Scene(content));
        popup.showAndWait();
    }
}
