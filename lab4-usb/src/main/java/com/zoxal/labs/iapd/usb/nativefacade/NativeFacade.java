package com.zoxal.labs.iapd.usb.nativefacade;

import java.io.IOException;

public class NativeFacade {
    static {
        try {
            NativeUtils.loadLibraryFromJar("/liblab4-c.so");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static native String getDevPath(String sysPath);
    public static native String getLabel(String sysPath);
    public static native long getFreeSpace(String devPath);
    public static native long getFreeSpaceMounted(String devPath);
    public static native long getTotalSpace(String devPath);
    public static native long getTotalSpaceMounted(String devPath);
    public static native String getMountPath(String devPath);
}
