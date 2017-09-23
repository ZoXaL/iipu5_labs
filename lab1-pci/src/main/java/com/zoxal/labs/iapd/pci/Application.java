package com.zoxal.labs.iapd.pci;

import com.zoxal.labs.iapd.pci.dao.PCIDevice;
import dnl.utils.text.table.MapBasedTableModel;
import dnl.utils.text.table.TextTable;
import dnl.utils.text.table.TextTableModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Application {
    private static final Logger log = LoggerFactory.getLogger(Application.class);
    private PCIDataExtractor pciDataExtractor;
    public static void main(String[] args) {
        Application app = new Application(PCIDataExtractor.Factory.getPCIDataExtractor());
        app.run();
    }

    public Application(PCIDataExtractor pciDataExtractor) {
        this.pciDataExtractor = pciDataExtractor;
    }

    public void run() {
        try {
            List<PCIDevice> pciDevices = pciDataExtractor.getPCIDevices();
            List<Map> tableModelData = new ArrayList<>();
            for (PCIDevice device : pciDevices) {
                tableModelData.add(device.asMap());
            }
            TextTableModel ttm = new MapBasedTableModel(tableModelData);
            TextTable tt = new TextTable(ttm);
            tt.printTable();
        } catch (Exception e) {
            log.error("Unexpected exception: ", e);
        }
    }


//    public void run() {
//        if (pciDataExtractor == null) {
//            System.out.println("Internal application error.");
//        }
//        try {
////            String pciDevices = "1022";
//            Map<String, String> m = new HashMap<>();
////            m.put("1022", "1705");
//            m.put("1002", "9648");
//            List<PCIDevice> dev = new PCIDeviceDAO().getPCIDevices(m);
//            for(PCIDevice pciDevice : dev) {
//                System.out.println(pciDevice);
//            }
////            System.out.println(new PCIDeviceDAO().getPCIDevices(m));
////            List<Map> tableModelData = new ArrayList<>();
////            for (PCIDevice device : pciDevices) {
//////                System.out.println(device);
////                tableModelData.add(device.asMap());
////            }
////            TextTableModel ttm = new MapBasedTableModel(tableModelData);
////            TextTable tt = new TextTable(ttm);
////            tt.printTable();
//        } catch (Exception e) {
//            System.out.println("Internal application error");
//            e.printStackTrace();
//        }
//    }

}
