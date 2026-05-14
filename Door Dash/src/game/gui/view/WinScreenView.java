package game.gui.view;

import game.engine.monsters.Monster;
import javafx.animation.*;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
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

public class WinScreenView {

    private final Stage   stage;
    private final Monster winner;
    private final Monster loser;
    private final boolean playerWon;

    public WinScreenView(Stage stage, Monster winner, Monster loser, boolean playerWon) {
        this.stage     = stage;
        this.winner    = winner;
        this.loser     = loser;
        this.playerWon = playerWon;
    }

    public void show() {
        StackPane root = new StackPane();

        // Background
        Rectangle bg = new Rectangle();
        bg.widthProperty().bind(root.widthProperty());
        bg.heightProperty().bind(root.heightProperty());
        bg.setFill(new RadialGradient(0,0,0.5,0.5,0.8,true,CycleMethod.NO_CYCLE,
            new Stop(0, Color.web(playerWon ? "#0a2a0a" : "#2a0a0a")),
            new Stop(1, Color.web("#000000"))));

        // Particles
        for (int i = 0; i < 30; i++) {
            Circle p = new Circle(3+Math.random()*6,
                Color.web(playerWon ? "#00FF88":"#FF6622", 0.5+Math.random()*0.5));
            p.setTranslateX(-450+Math.random()*900);
            p.setTranslateY(-350+Math.random()*700);
            root.getChildren().add(p);
            double dur = 2+Math.random()*3;
            TranslateTransition tt = new TranslateTransition(Duration.seconds(dur), p);
            tt.setByY(-60-Math.random()*80); tt.setAutoReverse(true);
            tt.setCycleCount(Animation.INDEFINITE); tt.play();
            FadeTransition ft = new FadeTransition(Duration.seconds(dur*0.8), p);
            ft.setFromValue(1); ft.setToValue(0.2); ft.setAutoReverse(true);
            ft.setCycleCount(Animation.INDEFINITE); ft.play();
        }

        VBox content = new VBox(20);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(40));
        content.setMaxWidth(700);

        // Banner
        Text banner = new Text(playerWon ? "*** VICTORY! ***" : "*** DEFEATED! ***");
        banner.setFont(Font.font("Impact", FontWeight.BOLD, 72));
        banner.setFill(Color.web(playerWon ? "#FFD700" : "#FF4444"));
        DropShadow glow = new DropShadow(40, playerWon ? Color.GOLD : Color.RED);
        glow.setSpread(0.3);
        banner.setEffect(glow);

        Text winnerText = new Text(winner.getName() + " WINS!");
        winnerText.setFont(Font.font("Georgia", FontWeight.BOLD, 36));
        winnerText.setFill(Color.WHITE);

        Label roleLabel = new Label(
            "Role: " + winner.getRole().toString()
            + "  |  Type: " + winner.getClass().getSimpleName());
        roleLabel.setFont(Font.font("Georgia", 18));
        roleLabel.setTextFill(Color.web("#AAAAFF"));

        // Monster portrait for winner
        ImageView winnerPortrait = tryLoadImage(monsterKey(winner), 100, 100);

        // Energy cards
        HBox energyBox = new HBox(40);
        energyBox.setAlignment(Pos.CENTER);
        energyBox.getChildren().addAll(
            buildEnergyCard(winner, true),
            buildEnergyCard(loser, false));

        Button returnBtn = new Button("PLAY AGAIN");
        returnBtn.setFont(Font.font("Impact", 22));
        returnBtn.setPrefSize(260, 55);
        returnBtn.setStyle(
            "-fx-background-color:linear-gradient(to right,#FFD700,#FF8C00);" +
            "-fx-text-fill:#1a1a1a;-fx-background-radius:30;-fx-cursor:hand;");
        returnBtn.setOnAction(e -> { new StartScreenView(stage).show(); });

        if (winnerPortrait != null) content.getChildren().add(winnerPortrait);
        content.getChildren().addAll(banner, winnerText, roleLabel, energyBox, returnBtn);
        root.getChildren().addAll(bg, content);

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setFullScreen(true);

        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.F11)
                stage.setFullScreen(!stage.isFullScreen());
        });

        ScaleTransition pulse = new ScaleTransition(Duration.seconds(1), banner);
        pulse.setFromX(1); pulse.setToX(1.08);
        pulse.setFromY(1); pulse.setToY(1.08);
        pulse.setAutoReverse(true);
        pulse.setCycleCount(Animation.INDEFINITE);
        pulse.play();
    }

    private VBox buildEnergyCard(Monster m, boolean isWinner) {
        VBox box = new VBox(6);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(16));
        String color = isWinner ? "#00cc44" : "#cc3300";
        box.setStyle(
            "-fx-background-color:" + (isWinner ? "#0d2d0d" : "#2d0d0d") + ";" +
            "-fx-border-color:" + color + ";" +
            "-fx-border-width:2;-fx-border-radius:10;-fx-background-radius:10;");

        // Small portrait
        ImageView port = tryLoadImage(monsterKey(m), 60, 60);

        Label crown = new Label(isWinner ? "[WINNER]" : "[LOSER]");
        crown.setFont(Font.font("Impact", 16));
        crown.setTextFill(Color.web(color));

        Label name = new Label(m.getName());
        name.setFont(Font.font("Georgia", FontWeight.BOLD, 18));
        name.setTextFill(Color.WHITE);

        Label energy = new Label("Energy: " + m.getEnergy());
        energy.setFont(Font.font("Georgia", FontWeight.BOLD, 16));
        energy.setTextFill(Color.web("#FFD700"));

        Label pos = new Label("Cell " + (m.getPosition() + 1));
        pos.setFont(Font.font("Georgia", 13));
        pos.setTextFill(Color.web("#AAAAFF"));

        if (port != null) box.getChildren().add(port);
        box.getChildren().addAll(crown, name, energy, pos);
        return box;
    }

    private String monsterKey(Monster m) {
        if (m == null) return null;
        String n = m.getName().toLowerCase();
        if (n.contains("sullivan") || n.contains("sulley")) return "sulley";
        if (n.contains("wazowski") || n.contains("mike"))   return "mike";
        if (n.contains("randall"))                          return "randall";
        if (n.contains("celia"))                            return "celia";
        if (n.contains("roz"))                              return "roz";
        if (n.contains("fungus"))                           return "fungus";
        if (n.contains("waternoose"))                       return "waternoose";
        if (n.contains("yeti"))                             return "yeti";
        return null;
    }

    private ImageView tryLoadImage(String key, double w, double h) {
        if (key == null) return null;
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
