package com.zoxal.labs.iapd.usb;

import com.zoxal.labs.iapd.usb.nativefacade.NativeFacade;
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
//    private UIController controller;

    public static void main(String[] args) throws Exception {
//        String devPath = NativeFacade.getDevPath("/sys/devices/pci0000:00/0000:00:12.2/usb1/1-5/1-5:1.0/host7/target7:0:0/7:0:0:0/block/sdb/sdb1");
//        System.out.println(devPath);
//        System.out.println(NativeFacade.getLabel("/sys/devices/pci0000:00/0000:00:12.2/usb1/1-5/1-5:1.0/host7/target7:0:0/7:0:0:0/block/sdb/sdb1"));
        launch(args);
    }
    @Override
    public void init() throws Exception {
        super.init();
    }

    @Override
    public void stop() throws Exception {
        super.stop();
//        if (controller != null) {
//            controller.destroy();
//        }
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("USB device manager");
        primaryStage.setScene(new Scene(createFXMLView()));
        primaryStage.show();
    }

    private Pane createFXMLView() {
        try {
            FXMLLoader loader = new FXMLLoader(getFXMLView());
            Pane mainPane = loader.load();
//            controller = loader.getController();
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
