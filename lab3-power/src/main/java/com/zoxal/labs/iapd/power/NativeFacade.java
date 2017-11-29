package com.zoxal.labs.iapd.power;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class NativeFacade {
    public static final Logger log = LoggerFactory.getLogger(NativeFacade.class);

    static {
        try {
            NativeUtils.loadLibraryFromJar("/liblab3-c.so");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private NativeFacade(){}

    public native static int getSleepTimeout();

    public native static void setSleepTimeout(int seconds);

    public native static int getCurrentCapacity();

    public native static int getCurrentSupplier();

    public native static int waitPowerCapacityChange(int secondsTimeout);

    public native static int waitPowerSupplyChange(int secondsTimeout);
}
