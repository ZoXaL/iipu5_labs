package com.zoxal.labs.iapd.power;

import com.zoxal.labs.iapd.power.ui.UIController;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class MainView extends Application{
    private static final String FXML_FILE = "MainPane.fxml";
    private static final Logger log = LoggerFactory.getLogger(MainView.class);
    private UIController controller;

    public static void main(String[] args) throws Exception {
        launch(args);
//        System.out.println("sleep timeout: " + NativeFacade.getSleepTimeout());
//        NativeFacade.setSleepTimeout(300);
//        System.out.println("sleep timeout: " + NativeFacade.getSleepTimeout());
//
//        System.out.println("current capacity: " + NativeFacade.getCurrentCapacity());
//
//        System.out.println("current supplier: " + NativeFacade.getCurrentSupplier());
//
////        System.out.println("supplier changed: " + NativeFacade.waitPowerSupplyChange(10));
//        System.out.println("capacity changed: " + NativeFacade.waitPowerCapacityChange(100));
    }
    @Override
    public void init() throws Exception {
        super.init();
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        if (controller != null) {
            controller.destroy();
        }
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Power manager");
        primaryStage.setScene(new Scene(createFXMLView()));
//        primaryStage.setWidth(700);
//        primaryStage.setMaxWidth(900);
//        primaryStage.setMinWidth(600);
//
//        primaryStage.setHeight(400);
//        primaryStage.setMaxHeight(600);
//        primaryStage.setMinHeight(400);
        primaryStage.show();
    }

    private Pane createFXMLView() {
        try {
            FXMLLoader loader = new FXMLLoader(getFXMLView());
            Pane mainPane = loader.load();
            controller = loader.getController();
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
