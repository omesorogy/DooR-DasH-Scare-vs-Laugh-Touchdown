package game.gui;

import game.gui.view.StartScreenView;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        StartScreenView startScreen = new StartScreenView(primaryStage);
        startScreen.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
