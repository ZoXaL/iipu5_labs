package com.zoxal.labs.iapd.usb.manager;

import com.zoxal.labs.iapd.usb.model.USBDevice;
import com.zoxal.labs.iapd.usb.nativefacade.NativeFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.file.*;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class DeviceMonitoringTask extends TimerTask {
    private static final Logger log = LoggerFactory.getLogger(DeviceMonitoringTask.class);
    private Consumer<List<USBDevice>> deviceConsumer;
    public static void main(String[] args) throws Exception {
        String url = "http://example.com/query?q=" + URLEncoder.encode("[usb1-5]", "UTF-8");

//        new DeviceMonitoringThread().run();
        USBDeviceFinder finder = new USBDeviceFinder();

        // pass the initial directory and the finder to the file tree walker
        Files.walkFileTree(Paths.get("/sys/devices/pci0000:00"), finder);

        // get the matched paths
        Collection<Path> matchedFiles = finder.getMatchedPaths();

        System.out.println("--------------");

        // print the matched paths
        for (Path path : matchedFiles) {
            System.out.println(path.toFile().getAbsolutePath());

            USBStorageFinder finder2 = new USBStorageFinder();

            // pass the initial directory and the finder2 to the file tree walker
            Files.walkFileTree(path, finder2);

            // get the matched paths
            Collection<Path> matchedFiles2 = finder2.getMatchedPaths();

            System.out.println("=========================");
            // print the matched paths
            for (Path path2 : matchedFiles2) {
                System.out.println(path2.toFile().getAbsolutePath());
            }
            System.out.println("=========================");
        }



    }

    public DeviceMonitoringTask(Consumer<List<USBDevice>> deviceConsumer) {
        this.deviceConsumer = deviceConsumer;
    }

    @Override
    public void run() {
        try {
            // 1 get device usb path        +
            // 1.1 fill device product      +
            // 2 try to get sys block path  +
            // if no, try to get mtp block path +
            // if no, just add device       +
            Collection<Path> devicePaths = getInjectedUSBDevices();
            List<USBDevice> devices = new ArrayList<>();
            devicePaths.forEach((devicePath) -> {
                USBDevice usbDevice = new USBDevice();
                usbDevice.setProductName(getDeviceProductName(devicePath));
                usbDevice.setType(USBDevice.DeviceType.DEVICE);

                Collection<Path> storageDevices = getStorageDevices(devicePath);
                Collection<Path> mtpDevices = getMTPLikeDevices(devicePath);
                storageDevices.forEach((std) -> log.debug("std: {}", std));
                mtpDevices.forEach((mtpd) -> log.debug("mtpd: {}", mtpd));

                if (storageDevices.isEmpty() && mtpDevices.isEmpty()) {
                    devices.add(usbDevice);
                    return;
                }
                if (!storageDevices.isEmpty()) {
                    storageDevices.forEach((storageDevicePath) -> {
                        USBDevice storageUSB = new USBDevice();
                        storageUSB.setProductName(usbDevice.getProductName());
                        storageUSB.setLabel(NativeFacade.getLabel(storageDevicePath.toAbsolutePath().toString()));
                        storageUSB.setName(NativeFacade.getDevPath(storageDevicePath.toAbsolutePath().toString()));
                        storageUSB.setDevPath(NativeFacade.getDevPath(storageDevicePath.toAbsolutePath().toString()));
                        if (NativeFacade.getMountPath(storageUSB.getDevPath()) != null) {
                            storageUSB.setAvailableSpace(NativeFacade.getFreeSpace(storageUSB.getDevPath()));
                            storageUSB.setTotalSpace(NativeFacade.getTotalSpace(storageUSB.getDevPath()));
                        } else {
                            storageUSB.setAvailableSpace(0);
                            storageUSB.setTotalSpace(0);
                        }
                        storageUSB.setType(USBDevice.DeviceType.STORAGE);
                        devices.add(storageUSB);
                    });
                }
                if (!mtpDevices.isEmpty()) {
                    mtpDevices.forEach((mtpDevicePath) -> {
                        USBDevice mtpDevice = new USBDevice();
                        mtpDevice.setProductName(usbDevice.getProductName());
                        mtpDevice.setName(usbDevice.getProductName());
                        mtpDevice.setDevPath(mtpDevicePath.toAbsolutePath().toString());
                        mtpDevice.setLabel(mtpDevicePath.toString());
                        mtpDevice.setAvailableSpace(NativeFacade.getFreeSpaceMounted(mtpDevice.getDevPath()));
                        mtpDevice.setTotalSpace(NativeFacade.getTotalSpaceMounted(mtpDevice.getDevPath()));
                        mtpDevice.setType(USBDevice.DeviceType.MTP);
                        devices.add(mtpDevice);
                    });
                }
            });
            deviceConsumer.accept(devices);
        } catch (Exception e) {
            log.error("Unexpected exception at monitor", e);
        }
    }

    private Collection<Path> getInjectedUSBDevices() {
        try {
            USBDeviceFinder finder = new USBDeviceFinder();
            Files.walkFileTree(Paths.get("/sys/devices/pci0000:00"), finder);
            return finder.getMatchedPaths();
        } catch (IOException e) {
            String exceptionMessage = "IOException during finding ejected usb devices";
            log.error(exceptionMessage, e);
            throw new RuntimeException(exceptionMessage, e);
        }
    }

    private Collection<Path> getStorageDevices(Path usbDevicePath) {
        try {
            USBStorageFinder finder = new USBStorageFinder();
            Files.walkFileTree(usbDevicePath, finder);
            return finder.getMatchedPaths();
        } catch (IOException e) {
            String exceptionMessage = "IOException during finding storage usb devices";
            log.error(exceptionMessage, e);
            throw new RuntimeException(exceptionMessage, e);
        }
    }

    private Collection<Path> getMTPLikeDevices(Path usbDevicePath) {
        try {
            Scanner busnumScanner = new Scanner(usbDevicePath.resolve("busnum"));
            int busnum = busnumScanner.nextInt();
            String busnumString = String.format("%03d", busnum);
            Scanner devnumScanner = new Scanner(usbDevicePath.resolve("devnum"));
            int devnum = devnumScanner.nextInt();
            String devnumString = String.format("%03d", devnum);
            String mountPathString = URLEncoder.encode("[usb:"
                    + busnumString
                    + ","
                    + devnumString
                    + "]",
                    "UTF-8"
            );
            PathMatcher matcher = FileSystems.getDefault().getPathMatcher("regex:.+" + mountPathString);
            return Files.walk(FileSystems.getDefault().getPath("/run/user/1000/gvfs/"), 1)
                    .filter(matcher::matches)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            String exceptionMessage = "IOException during finding storage usb devices";
            log.error(exceptionMessage, e);
            throw new RuntimeException(exceptionMessage, e);
        }
    }

    private String getDeviceProductName(Path devicePath) {
        try (Scanner productNameScanner = new Scanner(devicePath.resolve("product"))) {
            return productNameScanner.nextLine();
        } catch (IOException e) {
            String exceptionMessage = "IOException during fetching device product names";
            log.error(exceptionMessage, e);
            throw new RuntimeException(exceptionMessage, e);
        }
    }
}
