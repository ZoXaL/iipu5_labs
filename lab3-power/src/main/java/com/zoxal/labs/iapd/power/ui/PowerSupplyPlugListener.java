package com.zoxal.labs.iapd.power.ui;

import com.zoxal.labs.iapd.power.NativeFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class PowerSupplyPlugListener implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(PowerSupplyPlugListener.class);
    private static final int WAIT_TIMEOUT = 3;
    private BatteryTimeEstimator estimator;
    private int lastSupplier;

    private Consumer<String> plugConsumer;
    AtomicBoolean running = new AtomicBoolean(true);

    public PowerSupplyPlugListener(Consumer<String> plugConsumer,
                                   BatteryTimeEstimator estimator) {
        this.plugConsumer = plugConsumer;
        this.estimator = estimator;
    }

    @Override
    public void run() {
        lastSupplier = NativeFacade.getCurrentSupplier();
        while(running.get()) {
            int supplier = NativeFacade.waitPowerSupplyChange(WAIT_TIMEOUT);
            if (supplier == 1) {
                if (supplier != lastSupplier) {
                    lastSupplier = supplier;
                    log.debug("changed");
                    estimator.reset.set(true);
                }
                plugConsumer.accept(UIController.POWER_SUPPLIER);
            } else if (supplier == 0) {
                if (supplier != lastSupplier) {
                    lastSupplier = supplier;
                    log.debug("changed");
                    estimator.reset.set(true);
                }
                plugConsumer.accept(UIController.BATTERY_SUPPLIER);
            } else if (supplier == -2) {
                log.warn("Unexpected new supplier value: {}", supplier);
            }
        }
    }
}
