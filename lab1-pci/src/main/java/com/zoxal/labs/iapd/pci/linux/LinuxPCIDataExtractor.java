package com.zoxal.labs.iapd.pci.linux;

import com.zoxal.labs.iapd.pci.PCIDataExtractor;
import com.zoxal.labs.iapd.pci.dao.PCIDevice;
import com.zoxal.labs.iapd.pci.dao.PCIDeviceDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.DatatypeConverter;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.*;

public class LinuxPCIDataExtractor implements PCIDataExtractor {
    private static final Logger log = LoggerFactory.getLogger(LinuxPCIDataExtractor.class);
    private static final String PCI_HOME = "/proc/bus/pci";
    private PCIDeviceDAO pciDeviceDAO;

    public LinuxPCIDataExtractor() throws Exception {
        pciDeviceDAO = new PCIDeviceDAO();
    }

    @Override
    public List<PCIDevice> getPCIDevices() throws Exception {
        List<File> pciDevices = new ArrayList<>();
        File[] directories = new File(PCI_HOME).listFiles(File::isDirectory);
        if (directories != null) {
            for (File dir : directories) {
                File[] devices = dir.listFiles();
                if (devices != null) {
                    pciDevices.addAll(Arrays.asList(devices));
                }
            }
        }
//        pciDevices.forEach(file -> System.out.println(file.toString()));
        Map<String, String> pciDevicesMap = new HashMap<>();
        for (File pciDeviceFile : pciDevices) {
            try (InputStream is = new FileInputStream(pciDeviceFile)) {
                byte[] buffer = new byte[2];
                byte[] buffer2 = new byte[2];

                is.read(buffer);
                buffer2[0] = buffer[1];
                buffer2[1] = buffer[0];

                String deviceId = DatatypeConverter.printHexBinary(buffer2);

                is.read(buffer);
                buffer2[0] = buffer[1];
                buffer2[1] = buffer[0];
                String vendorId = DatatypeConverter.printHexBinary(buffer2);
                log.debug("{} - {}", deviceId, vendorId);
                pciDevicesMap.put(deviceId, vendorId);
            }
        }
        return pciDeviceDAO.getPCIDevices(pciDevicesMap);
    }
}
