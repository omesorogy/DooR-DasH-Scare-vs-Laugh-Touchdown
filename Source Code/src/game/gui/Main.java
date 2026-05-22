package game.gui;

import game.gui.view.StartScreenView;
import javafx.animation.*;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.image.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.InputStream;

public class Main extends Application {

    /**
     * The ONE permanent scene that lives for the entire app lifetime.
     * Every view swap replaces rootWrapper's single child — setScene is
     * never called again, so the OS never exits/re-enters fullscreen.
     */
    public static StackPane rootWrapper;
    public static Scene     mainScene;
    public static Stage     mainStage;

    @Override
    public void start(Stage primaryStage) {
        mainStage = primaryStage;

        startMusic();
        SoundManager.get().preloadSfx();

        // Create the one-and-only scene
        rootWrapper = new StackPane();
        rootWrapper.setStyle("-fx-background-color: #08080f;");
        mainScene = new Scene(rootWrapper);

        primaryStage.setTitle("DooR DasH: Scare vs Laugh Touchdown");
        primaryStage.setScene(mainScene);
        primaryStage.setMaximized(true);
        primaryStage.setFullScreen(true);
        primaryStage.setFullScreenExitHint("Press F11 to toggle fullscreen.");
        primaryStage.show();

        // Global F11 handler on the permanent scene
        mainScene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.F11)
                primaryStage.setFullScreen(!primaryStage.isFullScreen());
        });

        primaryStage.setOnCloseRequest(e -> stopMusic());

        showSplash();
    }

    /** Replace the current view with a new root node — no scene swap, no flicker. */
    public static void setRoot(javafx.scene.Parent newRoot) {
        rootWrapper.getChildren().setAll(newRoot);
    }

    /**
     * Replace the current view with a short fade/slide transition.
     * The same permanent Scene is kept, so fullscreen remains stable.
     */
    public static void setRootAnimated(javafx.scene.Parent newRoot) {
        if (rootWrapper == null || rootWrapper.getChildren().isEmpty()) {
            setRoot(newRoot);
            return;
        }

        javafx.scene.Node oldRoot = rootWrapper.getChildren().get(0);

        double offset = rootWrapper.getWidth() > 0
            ? Math.max(80, rootWrapper.getWidth() * 0.06)
            : 120;

        newRoot.setOpacity(0);
        newRoot.setTranslateX(offset);
        rootWrapper.getChildren().add(newRoot);

        FadeTransition oldFade = new FadeTransition(Duration.millis(260), oldRoot);
        oldFade.setFromValue(1);
        oldFade.setToValue(0);

        TranslateTransition oldSlide = new TranslateTransition(Duration.millis(260), oldRoot);
        oldSlide.setFromX(0);
        oldSlide.setToX(-offset);

        FadeTransition newFade = new FadeTransition(Duration.millis(360), newRoot);
        newFade.setFromValue(0);
        newFade.setToValue(1);

        TranslateTransition newSlide = new TranslateTransition(Duration.millis(360), newRoot);
        newSlide.setFromX(offset);
        newSlide.setToX(0);

        ParallelTransition transition = new ParallelTransition(oldFade, oldSlide, newFade, newSlide);
        transition.setOnFinished(e -> {
            rootWrapper.getChildren().setAll(newRoot);
            oldRoot.setOpacity(1);
            oldRoot.setTranslateX(0);
            newRoot.setOpacity(1);
            newRoot.setTranslateX(0);
        });
        transition.play();
    }

    // -- Music -----------------------------------------------------------------

    public static void startMusic() { SoundManager.get().startMusic(); }
    public static void stopMusic()  { SoundManager.get().stopMusic(); }

    // -- Splash ---------------------------------------------------------------

    private void showSplash() {
        StackPane splash = new StackPane();
        splash.setStyle("-fx-background-color: #08080f;");

        ImageView logoView = loadLogo();
        if (logoView != null) {
            logoView.setPreserveRatio(false);
            logoView.setSmooth(true);
            logoView.setOpacity(0);
            logoView.fitWidthProperty().bind(splash.widthProperty());
            logoView.fitHeightProperty().bind(splash.heightProperty());
            StackPane.setAlignment(logoView, Pos.CENTER);
            splash.getChildren().add(logoView);

            FadeTransition fadeIn  = new FadeTransition(Duration.millis(600), logoView);
            fadeIn.setFromValue(0); fadeIn.setToValue(1);

            PauseTransition hold = new PauseTransition(Duration.millis(1400));

            FadeTransition fadeOut = new FadeTransition(Duration.millis(500), logoView);
            fadeOut.setFromValue(1); fadeOut.setToValue(0);
            fadeOut.setOnFinished(e -> new StartScreenView(mainStage).show());

            new SequentialTransition(fadeIn, hold, fadeOut).play();
        } else {
            new StartScreenView(mainStage).show();
            return;
        }

        setRoot(splash);
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

    public static void main(String[] args) { launch(args); }
}
