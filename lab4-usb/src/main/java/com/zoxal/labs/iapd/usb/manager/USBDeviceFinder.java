package com.zoxal.labs.iapd.usb.manager;

import java.util.HashMap;
import java.util.regex.Pattern;

/**
 * Matched path example: /sys/devices/pci0000:00/0000:00:12.2/usb1/1-2
 *
 */
public class USBDeviceFinder extends DirectoryFinder {
    private static int PCI_DEVICE_ELEMENT_POS = 3;
    private static int BUS_NUMBER_ELEMENT_POS = 4;
    private static int DEVICE_NUMBER_ELEMENT_POS = 5;

    public USBDeviceFinder() {
        pathElementMatcherMap = new HashMap<>();
        pathElementMatcherMap.put(PCI_DEVICE_ELEMENT_POS, Pattern.compile("0000:00:12\\.\\d+"));
        pathElementMatcherMap.put(BUS_NUMBER_ELEMENT_POS, Pattern.compile("usb?"));
        pathElementMatcherMap.put(DEVICE_NUMBER_ELEMENT_POS, Pattern.compile("^\\d+-\\d+$"));
    }

    @Override
    protected int getMaxPathLength() {
        return DEVICE_NUMBER_ELEMENT_POS + 1;
    }

    @Override
    protected int getMinPathLength() {
        return PCI_DEVICE_ELEMENT_POS + 1;
    }
}