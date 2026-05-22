package game.gui.view;

import game.engine.monsters.Monster;
import game.gui.SoundManager;
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


public class WinScreenView {

    private final Stage   stage;
    private final Monster winner;
    private final Monster loser;
    private final boolean playerWon;
    private final boolean multiplayer;

    public WinScreenView(Stage stage, Monster winner, Monster loser, boolean playerWon) {
        this(stage, winner, loser, playerWon, false);
    }

    public WinScreenView(Stage stage, Monster winner, Monster loser, boolean playerWon, boolean multiplayer) {
        this.stage       = stage;
        this.winner      = winner;
        this.loser       = loser;
        this.playerWon   = playerWon;
        this.multiplayer = multiplayer;
    }

    public void show() {
        StackPane root = new StackPane();

        // Background
        Rectangle bg = new Rectangle();
        bg.widthProperty().bind(root.widthProperty());
        bg.heightProperty().bind(root.heightProperty());
        boolean victoryStyle = multiplayer || playerWon;

        bg.setFill(new RadialGradient(0,0,0.5,0.5,0.8,true,CycleMethod.NO_CYCLE,
            new Stop(0, Color.web(victoryStyle ? "#0a2a0a" : "#2a0a0a")),
            new Stop(1, Color.web("#000000"))));

        // Particles - fewer, with finite cycle count so they stop consuming CPU
        for (int i = 0; i < 10; i++) {
            Circle p = new Circle(3+Math.random()*5,
                Color.web(victoryStyle ? "#00FF88":"#FF6622", 0.5+Math.random()*0.4));
            p.setTranslateX(-450+Math.random()*900);
            p.setTranslateY(-350+Math.random()*700);
            root.getChildren().add(p);
            double dur = 2.5+Math.random()*2;
            TranslateTransition tt = new TranslateTransition(Duration.seconds(dur), p);
            tt.setByY(-50-Math.random()*60); tt.setAutoReverse(true);
            tt.setCycleCount(6); // finite - stops after ~15s
            tt.play();
            FadeTransition ft = new FadeTransition(Duration.seconds(dur*0.8), p);
            ft.setFromValue(0.9); ft.setToValue(0.2); ft.setAutoReverse(true);
            ft.setCycleCount(6);
            ft.play();
        }

        VBox content = new VBox(20);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(40));
        content.setMaxWidth(700);

        // Banner
        String resultTitle;
        String resultSubtitle;
        if (multiplayer) {
            resultTitle = playerWon ? "*** PLAYER 1 WINS! ***" : "*** PLAYER 2 WINS! ***";
            resultSubtitle = winner.getName() + " claims the Floor!";
        } else if (playerWon) {
            resultTitle = "*** YOU WIN! ***";
            resultSubtitle = winner.getName() + " powered Monstropolis!";
        } else {
            resultTitle = "*** YOU LOST! ***";
            resultSubtitle = "Better luck on the scare floor!";
        }

        Text banner = new Text(resultTitle);
        banner.setFont(Font.font("Impact", FontWeight.BOLD, 72));
        banner.setFill(Color.web(victoryStyle ? "#FFD700" : "#FF4444"));
        DropShadow glow = new DropShadow(40, victoryStyle ? Color.GOLD : Color.RED);
        glow.setSpread(0.3);
        banner.setEffect(glow);

        Text winnerText = new Text(resultSubtitle);
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
        returnBtn.setOnAction(e -> {
            SoundManager.get().leaveWinScreenAndResumeTheme();
            new StartScreenView(stage).show();
        });

        if (winnerPortrait != null) content.getChildren().add(winnerPortrait);
        content.getChildren().addAll(banner, winnerText, roleLabel, energyBox, returnBtn);
        root.getChildren().addAll(bg, content);

        // Swap root inside permanent scene with a soft transition.
        content.setOpacity(0);
        content.setScaleX(0.92);
        content.setScaleY(0.92);
        game.gui.Main.setRootAnimated(root);

        FadeTransition contentFade = new FadeTransition(Duration.millis(700), content);
        contentFade.setFromValue(0);
        contentFade.setToValue(1);

        ScaleTransition contentPop = new ScaleTransition(Duration.millis(700), content);
        contentPop.setFromX(0.75);
        contentPop.setFromY(0.75);
        contentPop.setToX(1);
        contentPop.setToY(1);

        TranslateTransition contentDrop = new TranslateTransition(Duration.millis(700), content);
        contentDrop.setFromY(-45);
        contentDrop.setToY(0);

        new ParallelTransition(contentFade, contentPop, contentDrop).play();

        playCelebrationAnimation(root, content, banner, winnerPortrait);

        ScaleTransition pulse = new ScaleTransition(Duration.seconds(1), banner);
        pulse.setFromX(1); pulse.setToX(1.08);
        pulse.setFromY(1); pulse.setToY(1.08);
        pulse.setAutoReverse(true);
        pulse.setCycleCount(Animation.INDEFINITE);
        pulse.play();
    }

    private void playCelebrationAnimation(StackPane root, VBox content, Text banner, ImageView winnerPortrait) {
        // A quick winner entrance burst. It uses only JavaFX 8-compatible transitions.
        boolean victoryStyle = multiplayer || playerWon;
        Color mainColor = victoryStyle ? Color.GOLD : Color.web("#FF5533");
        Color secondColor = victoryStyle ? Color.web("#00FFAA") : Color.web("#AA66FF");

        Circle ring1 = new Circle(60);
        ring1.setFill(Color.TRANSPARENT);
        ring1.setStroke(mainColor);
        ring1.setStrokeWidth(4);
        ring1.setOpacity(0.9);

        Circle ring2 = new Circle(90);
        ring2.setFill(Color.TRANSPARENT);
        ring2.setStroke(secondColor);
        ring2.setStrokeWidth(3);
        ring2.setOpacity(0.8);

        root.getChildren().addAll(ring1, ring2);

        ScaleTransition ring1Scale = new ScaleTransition(Duration.millis(900), ring1);
        ring1Scale.setFromX(0.2);
        ring1Scale.setFromY(0.2);
        ring1Scale.setToX(3.5);
        ring1Scale.setToY(3.5);
        FadeTransition ring1Fade = new FadeTransition(Duration.millis(900), ring1);
        ring1Fade.setFromValue(0.9);
        ring1Fade.setToValue(0);

        ScaleTransition ring2Scale = new ScaleTransition(Duration.millis(1100), ring2);
        ring2Scale.setFromX(0.1);
        ring2Scale.setFromY(0.1);
        ring2Scale.setToX(4.2);
        ring2Scale.setToY(4.2);
        FadeTransition ring2Fade = new FadeTransition(Duration.millis(1100), ring2);
        ring2Fade.setFromValue(0.8);
        ring2Fade.setToValue(0);

        ParallelTransition rings = new ParallelTransition(ring1Scale, ring1Fade, ring2Scale, ring2Fade);
        rings.setOnFinished(e -> root.getChildren().removeAll(ring1, ring2));
        rings.play();

        // Confetti rain only belongs on victory screens. Do not show it when the human loses to the bot.
        if (victoryStyle) {
            for (int i = 0; i < 22; i++) {
                Rectangle confetti = new Rectangle(6 + Math.random() * 8, 10 + Math.random() * 12);
                confetti.setArcWidth(3);
                confetti.setArcHeight(3);
                confetti.setFill(i % 3 == 0 ? mainColor : (i % 3 == 1 ? secondColor : Color.WHITE));
                confetti.setOpacity(0.9);
                confetti.setTranslateX(-520 + Math.random() * 1040);
                confetti.setTranslateY(-430 - Math.random() * 170);
                confetti.setRotate(Math.random() * 360);
                root.getChildren().add(confetti);

                TranslateTransition fall = new TranslateTransition(Duration.millis(1100 + Math.random() * 500), confetti);
                fall.setByY(650 + Math.random() * 180);
                fall.setByX(-60 + Math.random() * 120);

                RotateTransition spin = new RotateTransition(Duration.millis(700 + Math.random() * 500), confetti);
                spin.setByAngle(360 + Math.random() * 360);
                spin.setCycleCount(3);

                FadeTransition fade = new FadeTransition(Duration.millis(700), confetti);
                fade.setDelay(Duration.millis(1200));
                fade.setFromValue(0.9);
                fade.setToValue(0);

                PauseTransition delay = new PauseTransition(Duration.millis(Math.random() * 200));
                ParallelTransition motion = new ParallelTransition(fall, spin, fade);
                SequentialTransition seq = new SequentialTransition(delay, motion);
                seq.setOnFinished(e -> root.getChildren().remove(confetti));
                seq.play();
            }
        }

        // Make the winner portrait feel like it lands on the screen.
        if (winnerPortrait != null) {
            winnerPortrait.setScaleX(0.65);
            winnerPortrait.setScaleY(0.65);
            winnerPortrait.setOpacity(0);
            ScaleTransition portraitPop = new ScaleTransition(Duration.millis(650), winnerPortrait);
            portraitPop.setFromX(0.65);
            portraitPop.setFromY(0.65);
            portraitPop.setToX(1.08);
            portraitPop.setToY(1.08);
            portraitPop.setAutoReverse(true);
            portraitPop.setCycleCount(2);
            FadeTransition portraitFade = new FadeTransition(Duration.millis(300), winnerPortrait);
            portraitFade.setFromValue(0);
            portraitFade.setToValue(1);
            new ParallelTransition(portraitPop, portraitFade).play();
        }

        // A tiny shake/pop for the main victory text.
        TranslateTransition bannerShake = new TranslateTransition(Duration.millis(70), banner);
        bannerShake.setFromX(-8);
        bannerShake.setToX(8);
        bannerShake.setAutoReverse(true);
        bannerShake.setCycleCount(8);
        bannerShake.play();
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

        Label crown = new Label(isWinner ? "[WINNER]" : (multiplayer ? "[RUNNER-UP]" : "[DEFEATED]"));
        crown.setFont(Font.font("Impact", 16));
        crown.setTextFill(Color.web(color));

        Label name = new Label(m.getName());
        name.setFont(Font.font("Georgia", FontWeight.BOLD, 18));
        name.setTextFill(Color.WHITE);

        Label energy = new Label("Energy: " + m.getEnergy());
        energy.setFont(Font.font("Georgia", FontWeight.BOLD, 16));
        energy.setTextFill(Color.web("#FFD700"));

        Label pos = new Label("Cell " + m.getPosition());
        pos.setFont(Font.font("Georgia", 13));
        pos.setTextFill(Color.web("#AAAAFF"));

        if (port != null) box.getChildren().add(port);
        box.getChildren().addAll(crown, name, energy, pos);
        return box;
    }

    private String monsterKey(Monster m) {
        if (m == null) return null;
        String n = m.getName().toLowerCase();
        if (n.contains("sullivan") || n.contains("sulley")) return "sullivan";
        if (n.contains("wazowski") || n.contains("mike"))   return "mike";
        if (n.contains("randall"))                          return "randall";
        if (n.contains("celia"))                            return "celia";
        if (n.contains("roz"))                              return "roz";
        if (n.contains("fungus"))                           return "fungus";
        if (n.contains("waternoose"))                       return "spider";
        if (n.contains("yeti"))                             return "yeti";
        return null;
    }

    private ImageView tryLoadImage(String key, double w, double h) {
        if (key == null) return null;
        // Use the app-wide static image cache so no disk I/O happens here
        Image img = game.gui.view.GameBoardView.getCachedImage(key);
        if (img != null) {
            ImageView iv = new ImageView(img);
            iv.setFitWidth(w); iv.setFitHeight(h);
            iv.setPreserveRatio(true); iv.setSmooth(true);
            return iv;
        }
        return null;
    }
}
