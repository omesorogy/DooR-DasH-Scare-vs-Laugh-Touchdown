package game.gui;

import game.gui.view.StartScreenView;
import javafx.animation.*;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.scene.media.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.InputStream;
import java.net.URL;

public class Main extends Application {

    private static MediaPlayer themePlayer;

    @Override
    public void start(Stage primaryStage) {
        // 1. Start looping music immediately
        startMusic();

        // 2. Show splash screen, then hand off to start screen
        showSplash(primaryStage);

        // 3. Stop music when window closes
        primaryStage.setOnCloseRequest(e -> stopMusic());
    }

    // -- Music -----------------------------------------------------------------

    public static void startMusic() {
        try {
            URL url = Main.class.getResource("/game/gui/resources/audio/Theme.mp3");
            if (url == null) return;
            Media media = new Media(url.toExternalForm());
            themePlayer = new MediaPlayer(media);
            themePlayer.setCycleCount(MediaPlayer.INDEFINITE);   // loop forever
            themePlayer.setVolume(0.6);
            themePlayer.play();
        } catch (Exception ignored) {}
    }

    public static void stopMusic() {
        if (themePlayer != null) {
            themePlayer.stop();
            themePlayer.dispose();
        }
    }

    // -- Splash screen ---------------------------------------------------------

    private void showSplash(Stage stage) {
        StackPane splash = new StackPane();
        Rectangle bg = new Rectangle();
        bg.widthProperty().bind(splash.widthProperty());
        bg.heightProperty().bind(splash.heightProperty());
        bg.setFill(Color.web("#08080f"));
        splash.getChildren().add(bg);

        // Logo image
        ImageView logoView = loadLogo();
        if (logoView != null) {
            logoView.setPreserveRatio(false);
            logoView.setSmooth(true);
            logoView.setOpacity(0);
            // Bind to fill the entire splash pane
            logoView.fitWidthProperty().bind(splash.widthProperty());
            logoView.fitHeightProperty().bind(splash.heightProperty());
            StackPane.setAlignment(logoView, Pos.CENTER);
            splash.getChildren().add(logoView);

            // Fade in -> hold - fade out - show main menu
            FadeTransition fadeIn = new FadeTransition(Duration.millis(600), logoView);
            fadeIn.setFromValue(0); fadeIn.setToValue(1);

            PauseTransition hold = new PauseTransition(Duration.millis(1400));

            FadeTransition fadeOut = new FadeTransition(Duration.millis(500), logoView);
            fadeOut.setFromValue(1); fadeOut.setToValue(0);
            fadeOut.setOnFinished(e -> javafx.application.Platform.runLater(() -> {
                StartScreenView startScreen = new StartScreenView(stage);
                startScreen.show();
            }));

            new SequentialTransition(fadeIn, hold, fadeOut).play();
        } else {
            // No logo found - go straight to menu
            new StartScreenView(stage).show();
            return;
        }

        Scene splashScene = new Scene(splash, 800, 600);
        stage.setScene(splashScene);
        stage.setTitle("DooR DasH: Scare vs Laugh Touchdown");
        stage.setMaximized(true);
        stage.show();
    }

    private ImageView loadLogo() {
        String[] paths = {
            "/game/gui/resources/images/loading.png",
            "/game/gui/resources/images/loading.jpg",
            "/game/gui/resources/images/loading.jpeg"
        };
        for (String path : paths) {
            try {
                InputStream is = getClass().getResourceAsStream(path);
                if (is != null) {
                    Image img = new Image(is);
                    if (!img.isError()) return new ImageView(img);
                }
            } catch (Exception ignored) {}
        }
        return null;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
