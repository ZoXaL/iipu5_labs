package com.zoxal.labs.iapd.power.ui;

import com.zoxal.labs.iapd.power.NativeFacade;
import com.zoxal.labs.iapd.power.math.DischargeApproximator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import static java.time.temporal.ChronoField.INSTANT_SECONDS;

public class BatteryTimeEstimator implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(BatteryTimeEstimator.class);
    private static final int WAIT_TIMEOUT = 3;
    private static final int APPROX_VALUES_COUNT = 5;

    private Consumer<String> estimateConsumer;
    private Consumer<String> capacityConsumer;
    AtomicBoolean running = new AtomicBoolean(true);
    AtomicBoolean reset = new AtomicBoolean(false);
    private ArrayDeque<DischargeApproximator.Pair> approxValues = new ArrayDeque<>();

    public BatteryTimeEstimator(Consumer<String> estimateConsumer,
                                Consumer<String> capacityConsumer) {
        this.estimateConsumer = estimateConsumer;
        this.capacityConsumer = capacityConsumer;
    }

    @Override
    public void run() {
        approxValues.add(DischargeApproximator.Pair.ofCapacity(NativeFacade.getCurrentCapacity()));
        while(running.get()) {
            if (reset.get()) {
                approxValues.clear();
                approxValues.add(DischargeApproximator.Pair.ofCapacity(NativeFacade.getCurrentCapacity()));
            }
//            log.debug("capacity tick");
            int newCapacityLevel = NativeFacade.waitPowerCapacityChange(WAIT_TIMEOUT);
//            log.debug("got new capacity level: {}", newCapacityLevel);
            if (newCapacityLevel > 0) {
                log.debug("last value: {}, new value: {}", approxValues.peekLast().capacity, newCapacityLevel);
                if (newCapacityLevel != approxValues.peekLast().capacity) {

                    log.debug("Updating data: {}", DischargeApproximator.Pair.ofCapacity(newCapacityLevel));
                    approxValues.addLast(DischargeApproximator.Pair.ofCapacity(newCapacityLevel));
                    if (approxValues.size() > APPROX_VALUES_COUNT) approxValues.pollFirst();
                }

                capacityConsumer.accept(String.valueOf(newCapacityLevel));
                if (approxValues.size() > 2) {
                    Duration dischargeTime = Duration.between(
                            Instant.ofEpochSecond(DischargeApproximator.approximate(approxValues)),
                            Instant.now()
                    );
                    estimateConsumer.accept(
                        String.format("%dD; %02d:%02d",
                                -(dischargeTime.toDays()),
                                -(dischargeTime.toHours()%24),
                                -(dischargeTime.toMinutes()%60))
                    );
                }
            } else if (newCapacityLevel != -1) {
                log.warn("Unexpected new capacity level: {}", newCapacityLevel);
            }
        }
    }
}
