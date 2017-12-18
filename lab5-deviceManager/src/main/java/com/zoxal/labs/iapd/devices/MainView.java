package com.zoxal.labs.iapd.devices;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;

public class MainView extends Application{
    private static final String FXML_FILE = "MainPane.fxml";
    private static final Logger log = LoggerFactory.getLogger(MainView.class);

    public static void main(String[] args) throws Exception {
        launch(args);
    }

    @Override
    public void init() throws Exception {
        super.init();
    }

    @Override
    public void stop() throws Exception {
        super.stop();
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Device manager");
        primaryStage.setScene(new Scene(createFXMLView()));
        primaryStage.show();
    }

    private Pane createFXMLView() {
        try {
            FXMLLoader loader = new FXMLLoader(getFXMLView());
            Pane mainPane = loader.load();
            return mainPane;
        } catch (IOException e) {
            log.error("Unexpected exception during opening fxml", e);
            System.exit(1);
            return null;
        }
    }

    private URL getFXMLView() {
        return this.getClass().getClassLoader().getResource(FXML_FILE);
    }
}
