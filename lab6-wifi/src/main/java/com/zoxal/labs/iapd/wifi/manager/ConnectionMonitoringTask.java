package com.zoxal.labs.iapd.wifi.manager;

import com.zoxal.labs.iapd.wifi.model.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConnectionMonitoringTask extends TimerTask {
    private static final Logger log = LoggerFactory.getLogger(ConnectionMonitoringTask.class);
    private Consumer<List<Connection>> deviceConsumer;

    public ConnectionMonitoringTask(Consumer<List<Connection>> deviceConsumer) {
        this.deviceConsumer = deviceConsumer;
    }

    @Override
    public void run() {
        try {
            List<Connection> connections = new ArrayList<>();
            Process refreshProcess = Runtime.getRuntime().exec(
                new String[] {
                    "nmcli", "dev", "wifi", "rescan"
                }
            );
            refreshProcess.waitFor();

            Process infoGatheringProcess = Runtime.getRuntime().exec(
                new String[] {
                    "nmcli", "-f", "SSID,SIGNAL,SECURITY,BSSID", "dev", "wifi", "list"
                }
            );
            BufferedReader inputStream = new BufferedReader(new InputStreamReader(infoGatheringProcess.getInputStream()));
            inputStream.readLine(); // first raw
            String connectionLine = inputStream.readLine();
            while(connectionLine != null) {
                Connection connection = new Connection();
                Pattern pattern = Pattern.compile("(\\w+)\\s+(\\d+)\\s+([^\\s]+)\\s+([^\\s]+)");
                Matcher m = pattern.matcher(connectionLine);
                m.find();
                connection.setName(m.group(1));
                connection.setLevel(m.group(2));
                connection.setAuthType(m.group(3));
                connection.setAddress(m.group(4));
                connections.add(connection);
                connectionLine = inputStream.readLine();
            }
            deviceConsumer.accept(connections);
        } catch (Exception e) {
            log.error("Unexpected exception at monitor", e);
        }
    }
}
