package game.gui.view;

import game.engine.Role;
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

        // Logo image (optional)
        ImageView logoView = tryLoadImage("logo", 220, 100);

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
            Role role = scarerBtn.isSelected() ? Role.SCARER : Role.LAUGHER;
            boolean mp = multiBtn.isSelected();
            GameController ctrl = new GameController(stage, role, mp);
            ctrl.startGame();
        });

        if (logoView != null) content.getChildren().add(logoView);
        content.getChildren().addAll(titleTop, titleSub, divider);
        content.getChildren().addAll(chooseLabel, roleBox, modeLabel, modeBox, rulesPane, startBtn);

        root.getChildren().addAll(bg, content);

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setResizable(true);
        stage.setMaximized(true);
        stage.setFullScreen(true);
        stage.setFullScreenExitHint("Press F11 or Escape to exit fullscreen.");
        stage.show();

        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.F11)
                stage.setFullScreen(!stage.isFullScreen());
        });

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
            "GOAL: Reach cell 100 (Boo's Door) with 1000+ energy to win!\n\n" +
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
            "WIN: Stand on cell 100 with >= 1000 energy!\n" +
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
}
