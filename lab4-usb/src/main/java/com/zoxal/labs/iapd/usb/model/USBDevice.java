package com.zoxal.labs.iapd.usb.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableStringValue;

public class USBDevice {
    private String name = "";
    private String label = "";
    private String productName = "'";
    private long totalSpace;
    private long availableSpace;
    private String devPath;
    private DeviceType type;   // 1 -- simple, 2 -- storage, 3 -- mtp


    public USBDevice() {

    }

    public USBDevice(String label, String name, String productName, long totalSpace, long availableSpace) {
        this.label = label;
        this.name = name;
        this.productName = productName;
        this.totalSpace = totalSpace;
        this.availableSpace = availableSpace;
    }

    public enum DeviceType {
        DEVICE, STORAGE, MTP
    }

    public DeviceType getType() {
        return type;
    }

    public void setType(DeviceType type) {
        this.type = type;
    }

    public String getDevPath() {
        return devPath;
    }

    public void setDevPath(String devPath) {
        this.devPath = devPath;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public StringProperty getProductNameProperty() {
        return new SimpleStringProperty(productName);
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public long getTotalSpace() {
        return totalSpace;
    }

    public void setTotalSpace(long totalSpace) {
        this.totalSpace = totalSpace;
    }

    public long getAvailableSpace() {
        return availableSpace;
    }

    public void setAvailableSpace(long availableSpace) {
        this.availableSpace = availableSpace;
    }

    public long getUsedSpace() {
        return totalSpace - availableSpace;
    }
}
