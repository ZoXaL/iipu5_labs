package com.zoxal.labs.iapd.devices.manager;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zoxal.labs.iapd.devices.model.Device;
import com.zoxal.labs.iapd.devices.model.LSHWDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DeviceManager {
    public static final Logger log = LoggerFactory.getLogger(DeviceManager.class);

    public static void main(String[] args) throws Exception {
        List<Device> devices = new DeviceManager().findDevices();
        devices.forEach((d) -> {
            System.out.println("----");
            System.out.println(d);
        });
    }

    public List<Device> findDevices() {
        List<Device> devices = new ArrayList<>();
        try {
            Process lshwProcess = Runtime.getRuntime().exec(new String[] {"lshw", "-json"});
            try {
                lshwProcess.waitFor();
                ObjectMapper mapper = new ObjectMapper();
                mapper.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);
                BufferedReader br = new BufferedReader(new InputStreamReader(lshwProcess.getInputStream()));
                String line = br.readLine();
                StringBuilder builder = new StringBuilder(line);
                while (line != null) {
                    line = br.readLine();
                    builder.append(line);
                }
//                    System.out.println(builder.toString());
                LSHWDevice lshwDevice = mapper.readValue(builder.toString(), LSHWDevice.class);
                lineraizeDevice(lshwDevice, devices);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return devices;
    }

    private List<Device> lineraizeDevice(LSHWDevice lshwDevice, List<Device> devices) {
        Device convertedDevice = convertDevice(lshwDevice);
        if (convertedDevice != null) {
            devices.add(convertedDevice);
        }
        if (lshwDevice.getChildren() != null) {
            for (LSHWDevice device : lshwDevice.getChildren()) {
                devices = lineraizeDevice(device, devices);
            }
        }
        return devices;
    }

    private Device convertDevice(LSHWDevice lshwDevice) {
        String busInfo = lshwDevice.getBusinfo();
        if (busInfo == null) return null;
        Device device = new Device();
        Pattern pattern = Pattern.compile("([^@]+)@([^@,]+)");
        Matcher matcher = pattern.matcher(busInfo);
        matcher.find();
        String busName = matcher.group(1);
        String busPath = matcher.group(2);
        String deviceFolder = "/sys/bus";
        String deviceName = null;
        if ("pci".equals(busName)) {
            deviceFolder += "/pci/devices/" + busPath;
            device.setDeviceDriverName(busPath);
        } else if ("usb".equals(busName)) {
            deviceFolder += "/usb/devices/";
            Pattern usbPathPattern = Pattern.compile("(\\d+)(:(\\d+))?");
            Matcher usbPathMatcher = usbPathPattern.matcher(busPath);
            usbPathMatcher.find();
            if (usbPathMatcher.group(3) != null) {
                deviceName = usbPathMatcher.group(1) + "-" + usbPathMatcher.group(3);
                device.setDeviceDriverName(deviceName);
                deviceFolder += deviceName;
            } else {
                deviceName = "usb" + usbPathMatcher.group(1);
                device.setDeviceDriverName(deviceName);
                deviceFolder += deviceName;
            }
        } else if ("scsi".equals(busName)) {
            if (busInfo.contains(",")) {
                return null;
            }
            device.setDeviceDriverName(busPath.replace('.', ':'));
            deviceFolder += "/scsi/devices/" + busPath.replace('.', ':');
        } else {
            if ("cpu".equals(busName)) {
                deviceFolder += "cpu/devices/" + busPath;
            }
        }
        device.setDevicePath(deviceFolder);

        try {
            device.setDriverFolder(FileSystems
                    .getDefault().getPath(deviceFolder + "/driver").toRealPath().toString()
            );
        } catch (IOException e) {
            log.error("Can not resolve driver folder symlink", e);
            device.setDriverFolder("");
        }
        if (lshwDevice.getConfiguration() != null) {
            String modalias = lshwDevice.getConfiguration().getModalias();
            if ((modalias == null || modalias.isEmpty()) && "usb".equals(busName)) {
                modalias = getUSBModalias(deviceFolder, deviceName);
            }
            if (modalias != null) {
                device.setDriverPath(getDriverLocation(modalias));
                device.setDriverAuthor(getDriverAuthor(modalias));
                device.setDriverInfo(getDriverInfo(modalias));
                device.setHardwareId(modalias);
            }
        }
        device.setDeviceName(lshwDevice.getProduct());
        device.setDescription(lshwDevice.getDescription());
        device.setManufacturer(lshwDevice.getVendor());
        device.setGUID(lshwDevice.getGUID());
        return device;
    }

    private String getUSBModalias(String devicePath, String deviceNumber) {
        String modaliasFile = devicePath + "/" + deviceNumber + ":1.0/modalias";
        if (deviceNumber.contains("usb")) {
            modaliasFile = devicePath + "/" + deviceNumber.replace("usb", "") + "-0:1.0/modalias";
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(modaliasFile))) {
            return reader.readLine();
        } catch (IOException e) {
            log.debug("Exception: ", e);
            return null;
        }
    }

    private String getDriverLocation(String modalias) {
        return getOutputLine("/sbin/modinfo", "-n", modalias);
    }

    private String getDriverInfo(String modalias) {
        return getOutputLine("/sbin/modinfo", "-d", modalias);
    }

    private String getDriverAuthor(String modalias) {
        return getOutputLine("/sbin/modinfo", "-a", modalias);
    }

    private String getOutputLine(String ... params) {
        try {
            Process findProcess = Runtime.getRuntime().exec(params);
            BufferedReader br = new BufferedReader(new InputStreamReader(findProcess.getInputStream()));
            return br.readLine();
        } catch (Exception e1) {
            e1.printStackTrace();
            return null;
        }
    }

}
