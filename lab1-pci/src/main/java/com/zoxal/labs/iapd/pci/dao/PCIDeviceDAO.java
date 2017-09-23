package com.zoxal.labs.iapd.pci.dao;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

public class PCIDeviceDAO {
    private static final Logger log = LoggerFactory.getLogger(PCIDeviceDAO.class);
    private static final String JDBC_DRIVER_CLASS = "org.apache.derby.jdbc.EmbeddedDriver";
    private static final String CONNECTION_URL = "jdbc:derby:classpath:devices;";

    protected Connection connection;

    public PCIDeviceDAO() throws ClassNotFoundException, SQLException {
        try {
            Class.forName(JDBC_DRIVER_CLASS);
            connection = DriverManager.getConnection(CONNECTION_URL);
        } catch (ClassNotFoundException | SQLException e) {
            log.error("Can not instantiate PCIDevice DAO: ", e);
            throw e;
        }
    }

    public PCIDevice getPCIDevice(String deviceId) throws SQLException {
        String query = "select "
                + "devices.device_id, devices.device_name, devices.device_description, "
                + "vendors.vendor_id, vendors.vendor_name_short, vendors.vendor_name_full "
                + "from DEVICES LEFT OUTER JOIN VENDORS on (DEVICES.DEVICE_ID = VENDORS.VENDOR_ID) "
                + "where DEVICE_ID = \'" + deviceId + "\'";
        return getPCIDevicesByQuery(query).get(0);
    }
    public List<PCIDevice> getPCIDevices(List<String> devicesId) throws SQLException {
        StringBuilder query = new StringBuilder();
        query.append("select ");
        query.append("devices.device_id, devices.device_name, devices.device_description, ");
        query.append("vendors.vendor_id, vendors.vendor_name_short, vendors.vendor_name_full ");
        query.append("from DEVICES LEFT OUTER JOIN VENDORS on (DEVICES.DEVICE_ID = VENDORS.VENDOR_ID) ");
        query.append("where DEVICE_ID in (");
        StringJoiner stringJoiner = new StringJoiner(",");
        for (String deviceId : devicesId) {
            stringJoiner.add(deviceId);
        }
        query.append(stringJoiner.toString());
        query.append(")");

        return getPCIDevicesByQuery(query.toString());
    }

    public List<PCIDevice> getPCIDevices(Map<String, String> devicesMap) throws SQLException {
        return getPCIDevicesByQuery(getQuery(devicesMap));
    }

    private List<PCIDevice> getPCIDevicesByQuery(String query) throws SQLException {
        log.trace("Running sql query:\n {}", query);
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {
            List<PCIDevice> devices = new ArrayList<>();
            while (resultSet.next()) {
                devices.add(fillDevice(resultSet));
            }
            return devices;
        }
    }

    private PCIDevice fillDevice(ResultSet resultSet) throws SQLException {
        PCIDevice device = new PCIDevice();
        device.setDeviceId(resultSet.getString(1));
        device.setDeviceShortName(resultSet.getString(2));
        device.setDeviceFullName(resultSet.getString(3));
        device.setVendorId(resultSet.getString(4));
        device.setVendorShortName(resultSet.getString(5));
        device.setVendorFullName(resultSet.getString(6));
        return device;
    }

    private String getQuery(Map<String, String> deviceVendorMap) {
        StringJoiner deviceVendorTuple = new StringJoiner(", ");
        for (Map.Entry<String, String> entry : deviceVendorMap.entrySet()) {
            deviceVendorTuple.add(
                    new StringJoiner(", ", "(", ")")
                            .add("\'" + entry.getKey() + "\'")
                            .add("\'" + entry.getValue() + "\'")
                            .toString()
            );
        }
//        return new StringBuilder()
//                .append("select\n")
//                .append("--  both device_id and vendor_id are present\n")
//                .append("    devices.device_id as DID, devices.device_name as DN, devices.device_description as DD,\n")
//                .append("    vendors.vendor_id as VID, vendors.vendor_name_short as VNS, vendors.vendor_name_full as VN\n")
//                .append("    from\n")
//                .append("        devices inner join\n")
//                .append("        (values ")
//                .append(deviceVendorTuple)
//                .append(") as V(device_id, vendor_id)\n")
//                .append("        on (devices.device_id = V.device_id and devices.vendor_id = V.vendor_id)\n")
//                .append("        inner join vendors on (devices.vendor_id = vendors.vendor_id)\n")
//                .append("union\n")
//                .append("select\n")
//                .append("--  only device_id or vendor_id are present\n")
//                .append("    orphan_devices.device_id as DID, orphan_devices.device_name as DN, orphan_devices.device_description as DD,\n")
//                .append("    vendors.vendor_id as VID, vendors.vendor_name_short as VNS, vendors.vendor_name_full as VN\n")
//                .append("    from\n")
//                .append("        vendors right outer join (\n")
//                .append("        select\n")
//                .append("            V.device_id as device_id,\n")
//                .append("            devices.device_name as device_name,\n")
//                .append("            devices.device_description as device_description,\n")
//                .append("            V.vendor_id\n")
//                .append("            from\n")
//                .append("                DEVICES right outer join\n")
//                .append("        (values ")
//                .append(deviceVendorTuple)
//                .append(") as V(device_id, vendor_id)\n")
//                .append("                on (devices.device_id = V.device_id and devices.vendor_id = V.vendor_id)\n")
//                .append("        ) as orphan_devices\n")
//                .append("        on (vendors.vendor_id = orphan_devices.vendor_id)")
//                .toString();

        return new StringBuilder(500)
                .append("select\n")
                .append("--  only device_id or vendor_id are present\n")
                .append("    orphan_devices.device_id as DID, orphan_devices.device_name as DN, orphan_devices.device_description as DD,\n")
                .append("    orphan_devices.vendor_id as VID, vendors.vendor_name_short as VNS, vendors.vendor_name_full as VN\n")
                .append("    from\n")
                .append("        vendors right outer join (\n")
                .append("--      fill info by deviceId\n")
                .append("        select\n")
                .append("            V.device_id as device_id,\n")
                .append("            V.vendor_id as vendor_id,\n")
                .append("            devices.device_name as device_name,\n")
                .append("            devices.device_description as device_description\n")
                .append("            from\n")
                .append("                DEVICES right outer join\n")
                .append("                (values ")
                .append(deviceVendorTuple)
                .append(") as V(device_id, vendor_id)\n")
                .append("                on (devices.device_id = V.device_id and devices.vendor_id = V.vendor_id)\n")
                .append("        ) as orphan_devices\n")
                .append("        on (vendors.vendor_id = orphan_devices.vendor_id)")
                .toString();
    }


}
