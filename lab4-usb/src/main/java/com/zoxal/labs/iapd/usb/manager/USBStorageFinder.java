package com.zoxal.labs.iapd.usb.manager;

import java.util.HashMap;
import java.util.regex.Pattern;

/**
 * Matched path example: /sys/devices/pci0000:00/0000:00:12.2/usb1/1-5/1-5:1.0/host6/target6:0:0/6:0:0:0/block/sdb/sdb1
 *
 */
public class USBStorageFinder extends DirectoryFinder {
    private static int INTERFACE_ELEMENT_POS = 6;
    private static int HOST_ELEMENT_POS = 7;
    private static int TARGET_ELEMENT_POS = 8;
    private static int TARGET_PATH_ELEMENT_POS = 9;
    private static int BLOCK_ELEMENT_POS = 10;
    private static int DISK_ELEMENT_POS = 11;
    private static int DRIVE_ELEMENT_POS = 12;

    public USBStorageFinder() {
        pathElementMatcherMap = new HashMap<>();
        pathElementMatcherMap.put(INTERFACE_ELEMENT_POS, Pattern.compile("\\d+-\\d+:\\d+\\.\\d+"));
        pathElementMatcherMap.put(HOST_ELEMENT_POS, Pattern.compile("host\\d*"));
        pathElementMatcherMap.put(TARGET_ELEMENT_POS, Pattern.compile("target\\.*"));
        pathElementMatcherMap.put(TARGET_PATH_ELEMENT_POS, Pattern.compile("\\d+:\\d+:\\d+:\\d+"));
        pathElementMatcherMap.put(BLOCK_ELEMENT_POS, Pattern.compile("block"));
        pathElementMatcherMap.put(DISK_ELEMENT_POS, Pattern.compile("sd\\w"));
        pathElementMatcherMap.put(DRIVE_ELEMENT_POS, Pattern.compile("^sd\\w\\d+$"));
    }

    @Override
    protected int getMaxPathLength() {
        return DRIVE_ELEMENT_POS + 1;
    }

    @Override
    protected int getMinPathLength() {
        return INTERFACE_ELEMENT_POS + 1;
    }
}