package com.zoxal.labs.iapd.pci.dao;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class DBBuilder implements AutoCloseable {
    private static final Logger log = LoggerFactory.getLogger(DBBuilder.class);
    private static final String DB_PATH = "/src/main/resources/devices";
    private static final String JDBC_DRIVER_CLASS = "org.apache.derby.jdbc.EmbeddedDriver";
    private static final String PCI_DATABASE_URL = "http://pcidatabase.com/pci_c_header.php";
    private static final int BATCH_INSERT_SIZE = 500;

    protected Connection connection;
    protected final String DB_ABSOLUTE_PATH;

    public static void main(String[] args) {
        if (args.length <= 0) {
            log.error("Specify DB path in the first argument");
            return;
        }

        try (DBBuilder builder = new DBBuilder(args[0] + DB_PATH)) {
            builder.buildDatabase();
        } catch (ClassNotFoundException classNotFoundException) {
            log.error("Could not find jdbc driver", classNotFoundException);
        } catch (SQLException | IOException sqlException) {
            log.error("Exception during building db: ", sqlException);
        }
    }

    public DBBuilder(String jdbcURL) throws SQLException, ClassNotFoundException {
        Class.forName(JDBC_DRIVER_CLASS);
        DB_ABSOLUTE_PATH = jdbcURL;
        connection = DriverManager.getConnection("jdbc:derby:" + DB_ABSOLUTE_PATH + ";create=true");
    }

    public void buildDatabase() throws SQLException, IOException {
        createDatabase();
        fillDatabase();
        prepareForEmbedded();
    }

    protected void createDatabase() throws SQLException {
        String createVendorsTableStatementQuery =
                "create table VENDORS (" +
                        "VENDOR_ID VARCHAR(6), " +
                        "VENDOR_NAME_SHORT VARCHAR(256), " +
                        "VENDOR_NAME_FULL VARCHAR(256), " +
                        "PRIMARY KEY (VENDOR_ID)" +
                ")";
        // ! No foreign key constrain because of dirty data (devices with no vendors)
        String createDeviceTableStatementQuery =
                "create table DEVICES (" +
                        "VENDOR_ID VARCHAR(6), " +
                        "DEVICE_ID VARCHAR(6), " +
                        "DEVICE_NAME VARCHAR(256), " +
                        "DEVICE_DESCRIPTION VARCHAR(256), " +
                        "PRIMARY KEY (VENDOR_ID, DEVICE_ID)" +
                        ")";

        try (Statement createVendorsTableStatement = connection.createStatement();
             Statement createDeviceTableStatement = connection.createStatement();
        ) {
            createVendorsTableStatement.execute(createVendorsTableStatementQuery);
            createDeviceTableStatement.execute(createDeviceTableStatementQuery);
        }
    }

    protected void fillDatabase() throws SQLException {
        HttpClient client = new HttpClient();
        GetMethod getMethod = new GetMethod(PCI_DATABASE_URL);
        InputStream rawData;
        try {
            client.executeMethod(getMethod);
            rawData = getMethod.getResponseBodyAsStream();
        } catch (IOException e) {
            log.error("Can not read data from {}", PCI_DATABASE_URL, e);
            return;
        }
        log.debug("Data was successfully fetched from {}", PCI_DATABASE_URL);
        Scanner scanner = new Scanner(rawData);

        Pattern vendorsDataPattern = Pattern.compile("PciVenTable\\s*\\[\\]\\s*=[\\n\\s]*\\{.*?\\}[\\n\\s]*;",
                Pattern.MULTILINE | Pattern.DOTALL);
        Pattern devicesDataPattern = Pattern.compile("PciDevTable\\s*\\[\\]\\s*=[\\n\\s]*\\{.*?\\}[\\n\\s]*;",
                Pattern.MULTILINE | Pattern.DOTALL);

        String vendorsData = scanner.findWithinHorizon(vendorsDataPattern, 0);
        String devicesData = scanner.findWithinHorizon(devicesDataPattern, 0);

        scanner.close();

        fillVendorsData(vendorsData);
        fillDevicesData(devicesData);
    }

    protected void fillVendorsData(String vendorsData) throws SQLException {
        Pattern vendorDataPattern = Pattern.compile(
                "\\{" +
                        "\\s*0x(?<deviceId>[^,{]+)\\s*," +
                        "\\s*\"(?<shortName>[^\"]*)\"\\s*," +
                        "\\s*\"(?<fullName>[^}]*)\"\\s*" +
                "\\}");

        Matcher vendorDataMatcher = vendorDataPattern.matcher(vendorsData);
        PreparedStatement updateVendorsStatement = connection.prepareStatement(
                "insert into VENDORS values (?, ?, ?)"
        );
        int i = 0;
        while(vendorDataMatcher.find()) {
            log.trace("deviceId: {}, shortName: {}, fullName: {}",
                    vendorDataMatcher.group("deviceId"),
                    vendorDataMatcher.group("shortName"),
                    vendorDataMatcher.group("fullName")
            );
            updateVendorsStatement.setString(1, vendorDataMatcher.group("deviceId"));
            updateVendorsStatement.setString(2, vendorDataMatcher.group("shortName"));
            updateVendorsStatement.setString(3, vendorDataMatcher.group("fullName"));
            updateVendorsStatement.addBatch();
            if (i % BATCH_INSERT_SIZE == 0) {
                updateVendorsStatement.executeBatch();
                log.debug("Inserting " + BATCH_INSERT_SIZE + " rows in VENDORS table...");
            }
            i++;
        }
        if (i % BATCH_INSERT_SIZE > 0) {
            updateVendorsStatement.executeBatch();
            log.trace("Inserting {} rows in VENDORS table...", i % BATCH_INSERT_SIZE);
        }
        updateVendorsStatement.close();
        log.debug("Inserted {} rows in VENDORS table", i);
    }

    protected void fillDevicesData(String devicesData) throws SQLException {
        Pattern deviceDataPattern = Pattern.compile(
                "\\{" +
                        "\\s*0x(?<vendorId>[^,{]+)\\s*," +
                        "\\s*0x(?<deviceId>[^,]+)\\s*," +
                        "\\s*\"(?<shortName>[^\"]*)\"\\s*," +
                        "\\s*\"(?<fullName>[^}]*)\"\\s*" +
                        "\\}"
        );

        Matcher deviceDataMatcher = deviceDataPattern.matcher(devicesData);
        PreparedStatement updateDevicesStatement = connection.prepareStatement(
                "insert into DEVICES values (?, ?, ?, ?)"
        );
        int i = 0;
        while(deviceDataMatcher.find()) {
            log.trace("vendorId: {}, deviceId: {}, shortName: {}, fullName: {}",
                    deviceDataMatcher.group("vendorId"),
                    deviceDataMatcher.group("deviceId"),
                    deviceDataMatcher.group("shortName"),
                    deviceDataMatcher.group("fullName")
            );
            updateDevicesStatement.setString(1, deviceDataMatcher.group("vendorId"));
            updateDevicesStatement.setString(2, deviceDataMatcher.group("deviceId"));
            updateDevicesStatement.setString(3, deviceDataMatcher.group("shortName"));
            updateDevicesStatement.setString(4, deviceDataMatcher.group("fullName"));
            updateDevicesStatement.addBatch();
            if (i % BATCH_INSERT_SIZE == 0) {
                updateDevicesStatement.executeBatch();
                log.debug("Inserting " + BATCH_INSERT_SIZE + " rows in DEVICES table...");
            }
            i++;
        }
        if (i % BATCH_INSERT_SIZE > 0) {
            updateDevicesStatement.executeBatch();
            log.trace("Inserting {} rows in DEVICES table...", i % BATCH_INSERT_SIZE);
        }
        updateDevicesStatement.close();
        log.debug("Inserted {} rows in DEVICES table", i);
    }

    protected void prepareForEmbedded() throws SQLException {
        String setTmpDirectoryPropertyQuery = "call syscs_util.syscs_set_database_property("
                + "'derby.storage.tempDirectory', "
                + "'tmp'"
                + ")";
        String setErrorFilePropertyQuery = "call syscs_util.syscs_set_database_property("
                + "'derby.stream.error.file', "
                + "'derby_error.log'"
                + ")";
        try (CallableStatement setTmpDirectoryProperty =
                     connection.prepareCall(setTmpDirectoryPropertyQuery);
             CallableStatement setErrorFileProperty =
                     connection.prepareCall(setErrorFilePropertyQuery);) {
             setTmpDirectoryProperty.execute();
             setErrorFileProperty.execute();
        }
    }

    public void close() {
        try {
            connection.close();
            try {
                DriverManager.getConnection("jdbc:derby:" + DB_ABSOLUTE_PATH + ";shutdown=true");
            } catch (SQLException e) {
                if (e.getErrorCode() != 45000) {
                    throw e;
                }
            }
            log.info("Database was successfully closed");
        } catch (SQLException e) {
            log.warn("Exception during closing database: ", e);
        }
    }
}
