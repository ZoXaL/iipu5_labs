package com.zoxal.labs.iapd.pci.wmi;

import com.zoxal.labs.iapd.pci.PCIDataExtractor;
import com.zoxal.labs.iapd.pci.dao.PCIDevice;
import com.zoxal.labs.iapd.pci.dao.PCIDeviceDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WMIPCIDataExtractor implements PCIDataExtractor {
    private static final String TMP_FILE = "jwmi-tmp.vbs";
    private static final Logger log = LoggerFactory.getLogger(WMIPCIDataExtractor.class);
    private PCIDeviceDAO pciDeviceDAO;

//    public static void main(String[] args) throws Exception{
//        new WMIPCIDataExtractor().getPCIDevices();
//    }

    public WMIPCIDataExtractor() throws Exception {
        pciDeviceDAO = new PCIDeviceDAO();
    }

    @Override
    public List<PCIDevice> getPCIDevices() throws Exception {
        Process VBScript;
        try {
            FileWriter fw = new FileWriter(TMP_FILE);
            fw.write(getVBSScript());
            fw.close();
            VBScript = Runtime.getRuntime().exec(
                    new String[] {
                            "cmd.exe", "/C", "cscript.exe", "//Nologo", TMP_FILE
                    }
            );
        } catch (IOException e) {
            log.error("Can not run vbscript: ", e);
            return Collections.emptyList();
        }
        Scanner scanner = new Scanner(VBScript.getInputStream(), StandardCharsets.UTF_8.name());
        Map<String, String> devices = new HashMap<>();
        Pattern deviceIdVendorIdPatter =
                Pattern.compile("[^&]*VEN_(?<vendorId>[^&]*)&DEV_(?<deviceId>[^&]*)");
        try {
            while (scanner.hasNextLine()) {
                Matcher matcher = deviceIdVendorIdPatter.matcher(scanner.nextLine());
                if (matcher.find()) {
                    log.trace("{} - {}", matcher.group("deviceId"), matcher.group("vendorId"));
                    devices.put(matcher.group("deviceId"), matcher.group("vendorId"));
                }
            }
        } finally {
            new File(TMP_FILE).delete();
        }
        return pciDeviceDAO.getPCIDevices(devices);
    }

    private String getVBSScript() {
        return "Dim oWMI : Set oWMI = GetObject(\"winmgmts:\")\n" +
                "Dim classComponent : Set classComponent = " +
                "oWMI.ExecQuery(\"SELECT DeviceID FROM Win32_PnPEntity WHERE DeviceID LIKE 'PCI%'\")\n" +
                "Dim obj\n" +
                "For Each obj in classComponent\n" +
                "  wscript.echo obj.DeviceID\n" +
                "Next\n";
    }
}
