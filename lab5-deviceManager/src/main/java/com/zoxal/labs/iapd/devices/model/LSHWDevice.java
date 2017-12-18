package com.zoxal.labs.iapd.devices.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LSHWDevice {
    private String businfo;
    private String serial;
    private String product;
    private String vendor;
    private String description;
    private LSHWConfiguration configuration;
    private List<LSHWDevice> children;
    private String GUID;

    public String getBusinfo() {
        return businfo;
    }

    public void setBusinfo(String businfo) {
        this.businfo = businfo;
    }

    public String getSerial() {
        return serial;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public String getVendor() {
        return vendor;
    }

    public String getGUID() {
        return GUID;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public List<LSHWDevice> getChildren() {
        return children;
    }

    public void setChildren(List<LSHWDevice> children) {
        this.children = children;
    }

    public String getDriverName() {
        return (configuration != null) ? configuration.getDriver() : null;
    }

    public LSHWConfiguration getConfiguration() {
        return configuration;
    }

    public LSHWDevice() {
        GUID = UUID.randomUUID().toString();
    }

    public void setConfiguration(LSHWConfiguration configuration) {
        this.configuration = configuration;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "LSHWDevice{" +
                "businfo='" + businfo + '\'' +
                ", serial='" + serial + '\'' +
                ", product='" + product + '\'' +
                ", vendor='" + vendor + '\'' +
                ", configuration=" + configuration +
                ", children=" + children +
                '}';
    }
}
