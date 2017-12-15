package com.zoxal.labs.iapd.wifi.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Connection {
    private String name;
    private String level;
    private String address;
    private String authType;
    private boolean connected = false;

    public StringProperty getName() {
        return new SimpleStringProperty(name);
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getConnectionLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAuthType() {
        return authType;
    }

    public void setAuthType(String authType) {
        this.authType = authType;
    }

    public boolean getConnected() {
        return connected;
    }

    public void setConnected(Boolean connected) {
        this.connected = connected;
    }

    @Override
    public String toString() {
        return "Connection{" +
                "name='" + name + '\'' +
                ", level='" + level + '\'' +
                ", address='" + address + '\'' +
                ", authType='" + authType + '\'' +
                '}';
    }
}
