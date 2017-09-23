package com.zoxal.labs.iapd.pci.dao;


import java.util.Formatter;
import java.util.HashMap;
import java.util.Map;

public class PCIDevice {
    private String deviceId;
    private String deviceShortName;
    private String deviceFullName;
    private String vendorId;
    private String vendorShortName;
    private String vendorFullName;

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceShortName() {
        return deviceShortName;
    }

    public void setDeviceShortName(String deviceShortName) {
        this.deviceShortName = deviceShortName;
    }

    public String getDeviceFullName() {
        return deviceFullName;
    }

    public void setDeviceFullName(String deviceFullName) {
        this.deviceFullName = deviceFullName;
    }

    public String getVendorId() {
        return vendorId;
    }

    public void setVendorId(String vendorId) {
        this.vendorId = vendorId;
    }

    public String getVendorShortName() {
        return vendorShortName;
    }

    public void setVendorShortName(String vendorShortName) {
        this.vendorShortName = vendorShortName;
    }

    public String getVendorFullName() {
        return vendorFullName;
    }

    public void setVendorFullName(String vendorFullName) {
        this.vendorFullName = vendorFullName;
    }

    public Map<String, String> asMap() {
        Map<String, String> dataMap = new HashMap<>();
        dataMap.put("Device Id", deviceId);
        dataMap.put("Vendor Id", vendorId);
        dataMap.put("Device name", deviceShortName);
        dataMap.put("Device description", deviceFullName);
        dataMap.put("Vendor name", vendorShortName);
        dataMap.put("Vendor description", vendorFullName);
        return dataMap;
    }

    @Override
    public String toString() {
        Formatter sf = new Formatter();
        sf.format("|%4s|%4s|%50s|%62s|%10s|%24s|",
                deviceId, vendorId,
                deviceShortName, deviceFullName,
                vendorShortName, vendorFullName
        );
        return sf.toString();
    }
}
