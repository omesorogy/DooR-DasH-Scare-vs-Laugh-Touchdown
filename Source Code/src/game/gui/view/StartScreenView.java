package game.gui.view;

import game.engine.Role;
import game.gui.SoundManager;
import game.gui.controller.GameController;
import javafx.animation.*;
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
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.InputStream;

public class StartScreenView {

    private final Stage stage;

    public StartScreenView(Stage stage) {
        this.stage = stage;
        stage.setTitle("DooR DasH: Scare vs Laugh Touchdown");
    }

    public void show() {
        StackPane root = new StackPane();

        // Background
        Rectangle bg = new Rectangle();
        bg.widthProperty().bind(root.widthProperty());
        bg.heightProperty().bind(root.heightProperty());
        bg.setFill(new LinearGradient(0,0,1,1,true,CycleMethod.NO_CYCLE,
            new Stop(0, Color.web("#0a0a1a")),
            new Stop(0.4, Color.web("#1a0a2e")),
            new Stop(1, Color.web("#0d1f0d"))));

        // Floating orbs
        for (int i = 0; i < 6; i++) {
            Circle orb = new Circle(20+i*8,
                Color.web(i%2==0 ? "#ff6600" : "#00cc44", 0.08));
            orb.setTranslateX(-300+i*120);
            orb.setTranslateY(-200+(i%3)*150);
            orb.setEffect(new GaussianBlur(20));
            root.getChildren().add(orb);
            TranslateTransition tt = new TranslateTransition(Duration.seconds(3+i*0.5), orb);
            tt.setByY(-30); tt.setAutoReverse(true);
            tt.setCycleCount(Animation.INDEFINITE); tt.play();
        }

        // Main content
        VBox content = new VBox(20);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(40));
        content.setMaxWidth(720);

        // Logo image - large at top of menu
        ImageView logoView = tryLoadImage("logo", 320, 320);

        Text titleTop = new Text("DooR DasH");
        titleTop.setFont(Font.font("Impact", FontWeight.BOLD, 68));
        titleTop.setFill(Color.web("#FFD700"));
        titleTop.setEffect(new Glow(0.8));

        Text titleSub = new Text("Scare vs Laugh Touchdown");
        titleSub.setFont(Font.font("Georgia", FontWeight.BOLD, 28));
        titleSub.setFill(Color.web("#CCCCFF"));

        Line divider = new Line(0,0,440,0);
        divider.setStroke(Color.web("#FFD700", 0.5));
        divider.setStrokeWidth(2);

        // Monster previews (Sulley and Mike)
        HBox monsterRow = buildMonsterPreviewRow();

        Label chooseLabel = new Label("Choose Your Side");
        chooseLabel.setFont(Font.font("Georgia", FontWeight.BOLD, 20));
        chooseLabel.setTextFill(Color.WHITE);

        HBox roleBox = new HBox(20);
        roleBox.setAlignment(Pos.CENTER);
        ToggleGroup roleGroup = new ToggleGroup();
        ToggleButton scarerBtn  = buildRoleButton("SCARER",  "#cc3300", "#ff6633", roleGroup);
        ToggleButton laugherBtn = buildRoleButton("LAUGHER", "#006600", "#00cc44", roleGroup);
        scarerBtn.setSelected(true);
        roleBox.getChildren().addAll(scarerBtn, laugherBtn);

        Label modeLabel = new Label("Game Mode");
        modeLabel.setFont(Font.font("Georgia", FontWeight.BOLD, 18));
        modeLabel.setTextFill(Color.web("#CCCCFF"));

        HBox modeBox = new HBox(16);
        modeBox.setAlignment(Pos.CENTER);
        ToggleGroup modeGroup = new ToggleGroup();
        ToggleButton singleBtn = buildModeButton("vs BOT (Single Player)", modeGroup);
        ToggleButton multiBtn  = buildModeButton("2 Players (Local Co-op)", modeGroup);
        singleBtn.setSelected(true);
        modeBox.getChildren().addAll(singleBtn, multiBtn);

        TitledPane rulesPane = buildRulesPane();
        rulesPane.setPrefWidth(640);
        rulesPane.setStyle("-fx-base:#1a1a3a; -fx-text-fill:white;");

        Button startBtn = new Button("ENTER THE FLOOR");
        startBtn.setFont(Font.font("Impact", 24));
        startBtn.setPrefSize(340, 58);
        startBtn.setStyle(
            "-fx-background-color:linear-gradient(to right,#FFD700,#FF8C00);" +
            "-fx-text-fill:#1a1a1a;-fx-background-radius:30;-fx-cursor:hand;");
        startBtn.setEffect(new Glow(0.4));
        startBtn.setOnMouseEntered(e -> startBtn.setStyle(
            "-fx-background-color:linear-gradient(to right,#FFEC00,#FFA500);" +
            "-fx-text-fill:#000;-fx-background-radius:30;-fx-cursor:hand;"));
        startBtn.setOnMouseExited(e -> startBtn.setStyle(
            "-fx-background-color:linear-gradient(to right,#FFD700,#FF8C00);" +
            "-fx-text-fill:#1a1a1a;-fx-background-radius:30;-fx-cursor:hand;"));
        startBtn.setOnAction(e -> {
            SoundManager.get().playClick();
            Role role = scarerBtn.isSelected() ? Role.SCARER : Role.LAUGHER;
            boolean mp = multiBtn.isSelected();
            GameController ctrl = new GameController(stage, role, mp);
            ctrl.startGame();
        });

        if (logoView != null) content.getChildren().add(logoView);
        content.getChildren().addAll(titleTop, titleSub, divider);
        content.getChildren().addAll(chooseLabel, roleBox, modeLabel, modeBox, rulesPane, startBtn);

        // Exit button - bottom-left corner
        Button exitBtn = new Button("EXIT");
        exitBtn.setFont(Font.font("Impact", 16));
        exitBtn.setPrefSize(120, 38);
        exitBtn.setStyle(
            "-fx-background-color:rgba(180,30,30,0.85);" +
            "-fx-text-fill:white;-fx-background-radius:8;-fx-cursor:hand;");
        exitBtn.setOnMouseEntered(e2 -> exitBtn.setStyle(
            "-fx-background-color:rgba(220,50,50,1);" +
            "-fx-text-fill:white;-fx-background-radius:8;-fx-cursor:hand;"));
        exitBtn.setOnMouseExited(e2 -> exitBtn.setStyle(
            "-fx-background-color:rgba(180,30,30,0.85);" +
            "-fx-text-fill:white;-fx-background-radius:8;-fx-cursor:hand;"));
        exitBtn.setOnAction(e2 -> { SoundManager.get().playClick(); showExitConfirm(stage, root); });
        StackPane.setAlignment(exitBtn, Pos.BOTTOM_LEFT);
        StackPane.setMargin(exitBtn, new Insets(0, 0, 18, 18));

        root.getChildren().addAll(bg, content, exitBtn, buildStartScreenMuteButtons());

        // Swap root node inside the permanent scene with a soft transition.
        game.gui.Main.setRootAnimated(root);

        ScaleTransition pulse = new ScaleTransition(Duration.seconds(2), titleTop);
        pulse.setFromX(1.0); pulse.setToX(1.05);
        pulse.setFromY(1.0); pulse.setToY(1.05);
        pulse.setAutoReverse(true);
        pulse.setCycleCount(Animation.INDEFINITE);
        pulse.play();
    }

    // ---- Monster preview row (Sulley vs Mike) ----

    private HBox buildMonsterPreviewRow() {
        HBox row = new HBox(40);
        row.setAlignment(Pos.CENTER);

        ImageView sulley = tryLoadImage("sulley", 80, 80);
        ImageView mike   = tryLoadImage("mike",   80, 80);

        if (sulley != null) {
            VBox sv = new VBox(4, sulley, labelFor("Scare Team", "#CC88FF"));
            sv.setAlignment(Pos.CENTER);
            row.getChildren().add(sv);
        }

        if (mike != null) {
            VBox mv = new VBox(4, mike, labelFor("Laugh Team", "#88FFAA"));
            mv.setAlignment(Pos.CENTER);
            row.getChildren().add(mv);
        }

        return row;
    }

    private Label labelFor(String text, String color) {
        Label l = new Label(text);
        l.setFont(Font.font("Georgia", FontWeight.BOLD, 13));
        l.setTextFill(Color.web(color));
        return l;
    }

    // ---- Button builders ----

    private ToggleButton buildRoleButton(String text, String base,
                                          String hover, ToggleGroup group) {
        ToggleButton btn = new ToggleButton(text);
        btn.setToggleGroup(group);
        btn.setPrefSize(210, 58);
        btn.setFont(Font.font("Georgia", FontWeight.BOLD, 18));
        String styleBase = "-fx-background-color:linear-gradient(to bottom,"
            + base + "88," + base + ");"
            + "-fx-text-fill:white;-fx-background-radius:12;-fx-cursor:hand;";
        String styleSel  = "-fx-background-color:linear-gradient(to bottom,"
            + hover + "," + base + ");"
            + "-fx-text-fill:white;-fx-background-radius:12;-fx-cursor:hand;"
            + "-fx-border-color:white;-fx-border-width:2;-fx-border-radius:12;";
        btn.setStyle(styleBase);
        btn.selectedProperty().addListener((obs,ov,nv) ->
            btn.setStyle(nv ? styleSel : styleBase));
        return btn;
    }

    private ToggleButton buildModeButton(String text, ToggleGroup group) {
        ToggleButton btn = new ToggleButton(text);
        btn.setToggleGroup(group);
        btn.setPrefSize(240, 48);
        btn.setFont(Font.font("Georgia", FontWeight.BOLD, 14));
        String base = "-fx-background-color:#2a2a4a;-fx-text-fill:#AAAAFF;"
            + "-fx-background-radius:10;-fx-cursor:hand;";
        String sel  = "-fx-background-color:#5555AA;-fx-text-fill:white;"
            + "-fx-background-radius:10;-fx-cursor:hand;"
            + "-fx-border-color:#AAAAFF;-fx-border-width:2;-fx-border-radius:10;";
        btn.setStyle(base);
        btn.selectedProperty().addListener((obs,ov,nv) -> btn.setStyle(nv ? sel : base));
        return btn;
    }

    // ---- Rules pane ----

    private TitledPane buildRulesPane() {
        TextArea rules = new TextArea(
            "GOAL: Reach cell 99 (Boo's Door) with 1000+ energy to win!\n\n" +
            "TURN: Optionally activate powerup (costs 500 energy), then roll dice.\n\n" +
            "DOORS: Land on matching role -> gain energy.  Wrong role -> lose energy.\n" +
            "       Exhausted doors (USED) are already activated and have no further effect.\n" +
            "CARD CELLS: Draw a mystery card with powerful effects.\n" +
            "BELT CELLS: Conveyor belts move you forward.\n" +
            "SOCK CELLS: Contamination socks move you back and drain 100 energy!\n" +
            "MONSTER CELLS: Same role -> free powerup.  Different role -> energy swap.\n\n" +
            "TYPES:\n" +
            "  Dasher      - 2x movement speed (Momentum Rush: 3x for 3 turns)\n" +
            "  Dynamo      - 2x energy gains AND losses (Freeze: opponent skips a turn)\n" +
            "  MultiTasker - Half speed, +200 to all energy changes (Focus Mode: normal speed 2t)\n" +
            "  Schemer     - +10 bonus to all energy changes (Chain Attack: steal from everyone)\n\n" +
            "WIN: Stand on cell 99 with >= 1000 energy!\n" +
            "TIP: Press F11 to toggle fullscreen at any time."
        );
        rules.setEditable(false);
        rules.setWrapText(true);
        rules.setPrefHeight(155);
        rules.setStyle(
            "-fx-control-inner-background:#0a0a1f;" +
            "-fx-text-fill:#CCDDFF;" +
            "-fx-font-family:Georgia;-fx-font-size:13;");
        TitledPane pane = new TitledPane("How to Play", rules);
        pane.setExpanded(false);
        pane.setStyle("-fx-text-fill:#FFD700;-fx-font-family:Georgia;-fx-font-size:14;");
        return pane;
    }

    // ---- Image helper ----

    private ImageView tryLoadImage(String key, double w, double h) {
        String[] exts = {".png", ".jpg", ".jpeg"};
        for (String ext : exts) {
            try {
                InputStream is = getClass().getResourceAsStream(
                    "/game/gui/resources/images/" + key + ext);
                if (is != null) {
                    Image img = new Image(is);
                    if (!img.isError()) {
                        ImageView iv = new ImageView(img);
                        iv.setFitWidth(w); iv.setFitHeight(h);
                        iv.setPreserveRatio(true); iv.setSmooth(true);
                        return iv;
                    }
                }
            } catch (Exception ignored) {}
        }
        return null;
    }

    private HBox buildStartScreenMuteButtons() {
        Button muteMusicBtn = buildStartMuteBtn(
            SoundManager.get().isMusicMuted() ? "Unmute Music" : "Mute Music",
            SoundManager.get().isMusicMuted() ? "#882222" : "#443388");
        muteMusicBtn.setOnAction(e -> {
            SoundManager.get().playClick();
            boolean muted = SoundManager.get().toggleMusicMute();
            muteMusicBtn.setText(muted ? "Unmute Music" : "Mute Music");
            muteMusicBtn.setStyle(startMuteBtnStyle(muted ? "#882222" : "#443388"));
        });

        Button muteSfxBtn = buildStartMuteBtn(
            SoundManager.get().isSfxMuted() ? "Unmute SFX" : "Mute SFX",
            SoundManager.get().isSfxMuted() ? "#882222" : "#443388");
        muteSfxBtn.setOnAction(e -> {
            SoundManager.get().playClick();
            boolean muted = SoundManager.get().toggleSfxMute();
            muteSfxBtn.setText(muted ? "Unmute SFX" : "Mute SFX");
            muteSfxBtn.setStyle(startMuteBtnStyle(muted ? "#882222" : "#443388"));
        });

        HBox box = new HBox(10, muteMusicBtn, muteSfxBtn);
        box.setAlignment(Pos.TOP_RIGHT);
        box.setPadding(new Insets(14, 18, 0, 0));
        box.setPickOnBounds(false);   // don't block clicks on nodes behind the empty HBox area
        StackPane.setAlignment(box, Pos.TOP_RIGHT);
        return box;
    }

    private Button buildStartMuteBtn(String label, String bgColor) {
        Button btn = new Button(label);
        btn.setFont(Font.font("Georgia", FontWeight.BOLD, 11));
        btn.setPrefHeight(30);
        btn.setStyle(startMuteBtnStyle(bgColor));
        return btn;
    }

    private String startMuteBtnStyle(String bgColor) {
        return "-fx-background-color: " + bgColor + ";" +
               "-fx-text-fill: white;" +
               "-fx-background-radius: 8;" +
               "-fx-cursor: hand;" +
               "-fx-padding: 4 10 4 10;";
    }

    private void showExitConfirm(Stage stage, StackPane root) {
        StackPane overlay = buildExitOverlay(
            () -> { game.gui.Main.stopMusic(); stage.close(); },
            () -> root.getChildren().remove(root.getChildren().get(root.getChildren().size() - 1))
        );
        root.getChildren().add(overlay);
    }

    public static StackPane buildExitOverlay(Runnable onYes, Runnable onNo) {

        StackPane overlay = new StackPane();
        overlay.setStyle("-fx-background-color:rgba(0,0,0,0.88);");

        javafx.scene.layout.VBox card = new javafx.scene.layout.VBox(20);
        card.setAlignment(javafx.geometry.Pos.CENTER);
        card.setPadding(new javafx.geometry.Insets(40));
        card.setMaxWidth(500);
        card.setMaxHeight(500);
        card.setStyle(
            "-fx-background-color:linear-gradient(to bottom,#1a0a00,#2a1500);" +
            "-fx-border-color:#FFD700;-fx-border-width:3;" +
            "-fx-border-radius:18;-fx-background-radius:18;");

        // "Are you sure" image
        javafx.scene.image.ImageView img = null;
        try {
            java.io.InputStream is = StartScreenView.class
                .getResourceAsStream("/game/gui/resources/images/are_you_sure.png");
            if (is != null) {
                javafx.scene.image.Image image = new javafx.scene.image.Image(is);
                if (!image.isError()) {
                    img = new javafx.scene.image.ImageView(image);
                    img.setFitWidth(360);
                    img.setFitHeight(260);
                    img.setPreserveRatio(true);
                    img.setSmooth(true);
                }
            }
        } catch (Exception ignored) {}

        // Buttons
        javafx.scene.control.Button yesBtn = new javafx.scene.control.Button("Pretty sure");
        yesBtn.setFont(javafx.scene.text.Font.font("Impact", 18));
        yesBtn.setPrefSize(160, 46);
        yesBtn.setStyle(
            "-fx-background-color:linear-gradient(to right,#cc2200,#ff4400);" +
            "-fx-text-fill:white;-fx-background-radius:23;-fx-cursor:hand;");
        yesBtn.setOnAction(e -> { SoundManager.get().playClick(); onYes.run(); });

        javafx.scene.control.Button noBtn = new javafx.scene.control.Button("No");
        noBtn.setFont(javafx.scene.text.Font.font("Impact", 18));
        noBtn.setPrefSize(160, 46);
        noBtn.setStyle(
            "-fx-background-color:linear-gradient(to right,#1a6b20,#2aaa30);" +
            "-fx-text-fill:white;-fx-background-radius:23;-fx-cursor:hand;");
        noBtn.setOnAction(e -> { SoundManager.get().playClick(); onNo.run(); });

        javafx.scene.layout.HBox btnRow = new javafx.scene.layout.HBox(24, yesBtn, noBtn);
        btnRow.setAlignment(javafx.geometry.Pos.CENTER);

        if (img != null) card.getChildren().add(img);
        card.getChildren().add(btnRow);
        overlay.getChildren().add(card);
        return overlay;
    }
}
