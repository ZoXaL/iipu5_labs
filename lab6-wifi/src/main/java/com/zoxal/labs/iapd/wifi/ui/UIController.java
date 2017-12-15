package com.zoxal.labs.iapd.wifi.ui;

import com.zoxal.labs.iapd.wifi.manager.ConnectionMonitoringTask;
import com.zoxal.labs.iapd.wifi.model.Connection;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.*;

public class UIController implements Initializable {
    public static final Logger log = LoggerFactory.getLogger(UIController.class);
    public static final long MONITOR_START_DELAY = 1000L;
    public static final long MONITOR_PERIOD = 3000L;

    private ObservableList<Connection> connectionsList = FXCollections.observableList(new ArrayList<>());
    @FXML
    private Label MAC;
    @FXML
    private Label connectionLevel;
    @FXML
    private Label authType;
    @FXML
    private TableView<Connection> connectionsTable;
    @FXML
    private TableColumn<Connection, String> connectionColumn;
    @FXML
    private TextArea passwordInput;
    @FXML
    private Button connectBtn;
    private Connection selectedConnection = null;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        connectionColumn.setCellValueFactory(
            cellData -> cellData.getValue().getName());

        connectionsTable.setItems(connectionsList);
        connectionsTable.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> showConnectionInfo(newValue));

        Timer timer = new Timer(true);
        TimerTask task = new ConnectionMonitoringTask((
            (List<Connection> list) ->
                Platform.runLater(() -> {
                    connectionsTable.setItems(FXCollections.observableList(list));
                    for (Connection connection : list) {
                        if (selectedConnection != null && connection.getAddress().equals(selectedConnection.getAddress())) {
                            connectionsTable.getSelectionModel().select(connection);
//                            selectedConnection = connection;
                        }
                    }
                    connectionsTable.refresh();
                })));
        timer.schedule(task, MONITOR_START_DELAY, MONITOR_PERIOD);
    }

    @FXML
    public void connect(ActionEvent e) {
        connectBtn.setDisable(true);
        new Thread(() -> {
            try {
                Process unmountProcess = Runtime.getRuntime().exec(
                    new String[] {
                        "nmcli",
                        "dev",
                        "wifi",
                        "connect",
                        selectedConnection.getName().toString(),
                        passwordInput.getText()
                    }
                );
                int unmountResult = unmountProcess.waitFor();
                if (unmountResult != 0) {
                    Platform.runLater(() -> {
                        Alert connectingAlert = new Alert(Alert.AlertType.NONE);
                        connectingAlert.setTitle("Connect error");
                        connectingAlert.setContentText("Can not connect to network.");
                        connectingAlert.getButtonTypes().setAll(new ButtonType("Ok", ButtonBar.ButtonData.BACK_PREVIOUS));
                        connectingAlert.showAndWait();
                        connectBtn.setDisable(false);
                    });
                } else {
                    selectedConnection.setConnected(true);
                }
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }).start();
    }

    private void showConnectionInfo(Connection connection) {
        if (connection != null) {
            selectedConnection = connection;
            System.out.println("Connection updated");
            connectionLevel.setText(String.valueOf(connection.getConnectionLevel()));
            MAC.setText(connection.getAddress());
            authType.setText(connection.getAuthType());
            System.out.println(connectBtn == null);
            connectBtn.setDisable(selectedConnection.getConnected());
        }
    }
}
