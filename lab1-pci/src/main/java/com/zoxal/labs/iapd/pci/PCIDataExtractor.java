package com.zoxal.labs.iapd.pci;

import com.zoxal.labs.iapd.pci.dao.PCIDevice;
import com.zoxal.labs.iapd.pci.linux.LinuxPCIDataExtractor;
import com.zoxal.labs.iapd.pci.wmi.WMIPCIDataExtractor;

import org.apache.commons.lang.SystemUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public interface PCIDataExtractor {
    Logger log = LoggerFactory.getLogger(PCIDataExtractor.class);

    List<PCIDevice> getPCIDevices() throws Exception;

    class Factory {
        public static PCIDataExtractor getPCIDataExtractor() {
            try {
                if (SystemUtils.IS_OS_LINUX) {
                    return new LinuxPCIDataExtractor();
                } else if (SystemUtils.IS_OS_WINDOWS) {
                    return new WMIPCIDataExtractor();
                } else {
                    log.warn("No available pci data extractor");
                    return null;
                }
            } catch (Exception e) {
                log.error("Error during loading WMIPCIDataExtracotor: {}", e);
                return null;
            }
        }
    }
}
