package com.zoxal.labs.iapd.devices.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Device {
    private String GUID;
    private String hardwareId = "";
    private String deviceName = "";
    private String manufacturer = "'";
    private String driverInfo;
    private String driverAuthor;
    private String driverPath;
    private String devicePath;
    private String driverFolder;
    private String deviceDriverName;
    private String description;

    public Device() {

    }

    public String getHardwareId() {
        return hardwareId;
    }

    public void setHardwareId(String hardwareId) {
        this.hardwareId = hardwareId;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getDriverInfo() {
        return driverInfo;
    }

    public void setDriverInfo(String driverInfo) {
        this.driverInfo = driverInfo;
    }

    public String getDriverPath() {
        return driverPath;
    }

    public void setDriverPath(String driverPath) {
        this.driverPath = driverPath;
    }

    public String getDevicePath() {
        return devicePath;
    }

    public void setDevicePath(String devicePath) {
        this.devicePath = devicePath;
    }

    public StringProperty getDeviceName() {
        return new SimpleStringProperty(deviceName);
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDriverFolder() {
        return driverFolder;
    }

    public void setDriverFolder(String driverFolder) {
        this.driverFolder = driverFolder;
    }

    public String getDriverAuthor() {
        return driverAuthor;
    }

    public void setDriverAuthor(String driverAuthor) {
        this.driverAuthor = driverAuthor;
    }

    public String getDeviceDriverName() {
        return deviceDriverName;
    }

    public void setDeviceDriverName(String deviceDriverName) {
        this.deviceDriverName = deviceDriverName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getGUID() {
        return GUID;
    }

    public void setGUID(String GUID) {
        this.GUID = GUID;
    }

    @Override
    public String toString() {
        return "Device{" +
                "hardwareId='" + hardwareId + '\'' +
                ", deviceName='" + deviceName + '\'' +
                ", manufacturer='" + manufacturer + '\'' +
                ", driverInfo='" + driverInfo + '\'' +
                ", driverAuthor='" + driverAuthor + '\'' +
                ", driverPath='" + driverPath + '\'' +
                ", devicePath='" + devicePath + '\'' +
                ", driverFolder='" + driverFolder + '\'' +
                ", deviceDriverName='" + deviceDriverName + '\'' +
                '}';
    }
}
