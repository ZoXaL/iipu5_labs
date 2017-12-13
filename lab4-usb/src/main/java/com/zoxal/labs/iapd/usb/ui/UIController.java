package com.zoxal.labs.iapd.usb.ui;

import com.zoxal.labs.iapd.usb.manager.DeviceMonitoringTask;
import com.zoxal.labs.iapd.usb.model.USBDevice;
import com.zoxal.labs.iapd.usb.nativefacade.NativeFacade;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.*;

public class UIController implements Initializable {
    public static final Logger log = LoggerFactory.getLogger(UIController.class);
    public static final long MONITOR_START_DELAY = 1000L;
    public static final long MONITOR_PERIOD = 3000L;

    private ObservableList<USBDevice> devicesList = FXCollections.observableList(new ArrayList<>());
    @FXML
    private Label deviceLabelValue;
    @FXML
    private Label deviceNameValue;
    @FXML
    private Label totalMemoryValue;
    @FXML
    private Label availableMemoryValue;
    @FXML
    private Label usedMemoryValue;
    @FXML
    private TableView<USBDevice> devicesTable;
    @FXML
    private TableColumn<USBDevice, String> deviceProductName;
    @FXML
    private Button safeEjectBtn;
    private USBDevice selectedDevice = null;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        deviceProductName.setCellValueFactory(
            cellData -> cellData.getValue().getProductNameProperty());

        devicesTable.setItems(devicesList);
        devicesTable.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> showDeviceInfo(newValue));

        Timer timer = new Timer(true);
        timer.schedule(new DeviceMonitoringTask(
            (List<USBDevice> list) ->
                    Platform.runLater(() -> {
                        devicesTable.setItems(FXCollections.observableList(list));
                        for (USBDevice device : list) {
                            if (selectedDevice != null && device.getProductName().equals(selectedDevice.getProductName())) {
                                devicesTable.getSelectionModel().select(device);
                            }
                        }
                        devicesTable.refresh();
                    })),
            MONITOR_START_DELAY,
            MONITOR_PERIOD
        );
    }

    @FXML
    public void safeEject(ActionEvent e) {
        safeEjectBtn.setDisable(true);
        try {
            String deviceMountPath = selectedDevice.getDevPath();
            if (selectedDevice.getType() == USBDevice.DeviceType.STORAGE) {
                deviceMountPath = NativeFacade.getMountPath(selectedDevice.getDevPath());
            }
            if (deviceMountPath != null) {
                log.debug("Ejecting device {}", deviceMountPath);
                Process unmountProcess = Runtime.getRuntime().exec(
                    new String[] {
                            "gio", "mount", "-u", deviceMountPath
                    }
                );
                new Thread(() -> {
                    try {
                        int unmountResult = unmountProcess.waitFor();
                        log.debug("Unmounted: " + unmountResult);
                        if (unmountResult != 0) {
                            Platform.runLater(() -> {
                                Alert connectingAlert = new Alert(Alert.AlertType.NONE);
                                connectingAlert.setTitle("Eject error");
                                connectingAlert.setContentText("Can not safe eject: device in use.");
                                connectingAlert.getButtonTypes().setAll(new ButtonType("Ok", ButtonBar.ButtonData.BACK_PREVIOUS));
                                connectingAlert.showAndWait();
                                safeEjectBtn.setDisable(false);
                            });
                        }
                    } catch (InterruptedException e1) {
                        safeEjectBtn.setDisable(true);
                    }
                }).start();
            } else {
                System.out.println("Mount path is null");
                safeEjectBtn.setDisable(true);
            }
        } catch (IOException ex) {
            log.error("Can not run eject script", e);
            safeEjectBtn.setDisable(true);
        }
    }

    private void showDeviceInfo(USBDevice device) {
        if (device != null) {
            deviceLabelValue.setText(device.getLabel());
            deviceNameValue.setText(device.getName());
            totalMemoryValue.setText(String.valueOf(device.getTotalSpace()));
            availableMemoryValue.setText(String.valueOf(device.getAvailableSpace()));
            usedMemoryValue.setText(String.valueOf(device.getUsedSpace()));
            selectedDevice = device;
            if (device.getType() == USBDevice.DeviceType.DEVICE) {
                safeEjectBtn.setDisable(true);
            } else {
                String deviceMountPath = selectedDevice.getDevPath();
                if (selectedDevice.getType() == USBDevice.DeviceType.STORAGE) {
                    deviceMountPath = NativeFacade.getMountPath(selectedDevice.getDevPath());
                }
                if (deviceMountPath == null) {
                    safeEjectBtn.setDisable(true);
                } else {
                    safeEjectBtn.setDisable(false);
                }
            }
        }
    }
}
