package com.zoxal.labs.iapd.devices.ui;

import com.zoxal.labs.iapd.devices.manager.DeviceManager;
import com.zoxal.labs.iapd.devices.model.Device;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.controlsfx.control.ToggleSwitch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.util.ResourceBundle;

public class UIController implements Initializable {
    public static final Logger log = LoggerFactory.getLogger(UIController.class);
    @FXML
    private Label deviceHardwareId;
    @FXML
    private Label deviceManufacturer;
    @FXML
    private Label deviceDriverInfo;
    @FXML
    private Label deviceDriverPath;
    @FXML
    private Label devicePath;
    @FXML
    private Label deviceDriverAuthor;
    @FXML
    private TableView<Device> devicesTable;
    @FXML
    private Label deviceDescription;
    @FXML
    private Label deviceGUID;
    @FXML
    private TableColumn<Device, String> deviceProductName;
    @FXML
    private ToggleSwitch disableDeviceSwitch;
    private Device selectedDevice = null;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        deviceProductName.setCellValueFactory(
            cellData -> cellData.getValue().getDeviceName());

        DeviceManager manager = new DeviceManager();
        ObservableList<Device> devicesList = FXCollections.observableList(manager.findDevices());

        devicesTable.setItems(devicesList);
        devicesTable.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> showDeviceInfo(newValue));

        disableDeviceSwitch.selectedProperty().addListener((ov, t, t1) -> {
            System.out.println("value: " + ov);
            System.out.println(selectedDevice.getDriverFolder());
            System.out.println(selectedDevice.getDeviceDriverName());
            String fileToWrite = ov.getValue() ?
                    selectedDevice.getDriverFolder() + "/unbind" : selectedDevice.getDriverFolder() + "/bind";
            try (BufferedWriter output = new BufferedWriter(
                    new FileWriter(fileToWrite))
            ) {
                System.out.println("Writing " + selectedDevice.getDeviceDriverName() + "to" + fileToWrite);
                output.write(selectedDevice.getDeviceDriverName());
                output.close();
            } catch (IOException e) {
                e.printStackTrace();
                log.error("Exception during deactivating device", e);
            }
        });
    }



    private void showDeviceInfo(Device device) {
        if (device != null) {
            deviceHardwareId.setText(device.getHardwareId());
            deviceManufacturer.setText(device.getManufacturer());
            deviceDriverInfo.setText(device.getDriverInfo());
            deviceDriverPath.setText(device.getDriverPath());
            deviceDriverAuthor.setText(device.getDriverAuthor());
            devicePath.setText(device.getDevicePath());
            deviceDescription.setText(device.getDescription());
            deviceGUID.setText(device.getGUID());
            selectedDevice = device;
        }
    }
}
