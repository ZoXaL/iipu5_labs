package com.zoxal.labs.iapd.power.ui;

import com.zoxal.labs.iapd.power.NativeFacade;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.util.StringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Native;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;


public class UIController implements Initializable {
    private static final Logger log = LoggerFactory.getLogger(UIController.class);
    public static final String BATTERY_SUPPLIER = "Battery";
    public static final String POWER_SUPPLIER = "Power supplier";

    @FXML
    private Label supplierLabel;
    @FXML
    private Label currentCapacityLabel;
    @FXML
    private Label estimatedDischargeTimeLabel;
    @FXML
    private Label dimTimeoutLabel;
    @FXML
    private Slider dimTimeoutSlider;

    private int initialDimTimeout;
    private BatteryTimeEstimator estimator;
    private PowerSupplyPlugListener powerSupplyPlugListener;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        double dimTimeout = NativeFacade.getSleepTimeout();
        initialDimTimeout = (int)dimTimeout;

        if (dimTimeout > dimTimeoutSlider.getMax()) {
            dimTimeout = dimTimeoutSlider.getMax();
        }
        if (dimTimeout < dimTimeoutSlider.getMin()) {
            dimTimeout = dimTimeoutSlider.getMin();
        }
        dimTimeoutLabel.setText(String.format("%.0f", dimTimeout));
        dimTimeoutSlider.setValue(dimTimeout);
        dimTimeoutSlider.valueProperty().addListener((value, oldValue, newValue)-> {
            dimTimeoutLabel.setText(String.format("%.0f", newValue));
        });

        int currentSupplier = NativeFacade.getCurrentSupplier();
        if (currentSupplier == 0) {
            supplierLabel.setText(BATTERY_SUPPLIER);
        } else if (currentSupplier == 1) {
            supplierLabel.setText(POWER_SUPPLIER);
        } else {
            log.warn("Unexpected supplier value: {}", currentSupplier);
        }

        int capacityLevel = NativeFacade.getCurrentCapacity();
        if (capacityLevel < 0) {
            log.warn("Unexpected capacity level value: {}", currentSupplier);
        } else {
            currentCapacityLabel.setText(String.valueOf(capacityLevel) + "%");
        }

        estimatedDischargeTimeLabel.setText("Calculating...");
        estimator = new BatteryTimeEstimator(
            (estimate) -> Platform.runLater(() -> estimatedDischargeTimeLabel.setText(estimate)),
            (capacity) -> Platform.runLater(() -> currentCapacityLabel.setText(capacity))
        );
        try {
            log.debug("Running estimator");
            new Thread(estimator).start();

            log.debug("Running plug listener");
            powerSupplyPlugListener = new PowerSupplyPlugListener(
                    (newSupplier) -> Platform.runLater(
                            () -> supplierLabel.setText(newSupplier)
                    ),
                    estimator
            );
            new Thread(powerSupplyPlugListener).start();
        } catch (Exception e) {
            log.warn("e", e);
        }
    }

    @FXML
    protected void updateDimTimeout(MouseEvent e) {
        log.debug("Updating dim timeout to {}", (int)dimTimeoutSlider.getValue());
        NativeFacade.setSleepTimeout((int)dimTimeoutSlider.getValue());
    }

    public void destroy() {
        estimator.running.set(false);
        powerSupplyPlugListener.running.set(false);
        NativeFacade.setSleepTimeout(initialDimTimeout);
    }
}