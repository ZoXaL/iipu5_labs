package com.zoxal.labs.iapd.usb.manager;

import com.zoxal.labs.iapd.usb.nativefacade.NativeFacade;

import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.net.URLEncoder;
import java.nio.file.*;
import java.util.Scanner;
import java.util.stream.Collectors;

/**
 * read bus num and device num
 * read uid
 * find dir
 */
public class GVFSUSBFinder {
    public static void main(String[] args)  throws  Exception{
//        System.out.println(new File("/dev/sdb1").getUsableSpace());
        System.out.println(NativeFacade.getMountPath("/dev/sdb1"));
        System.out.println(NativeFacade.getMountPath("/run/user/1000/gvfs/mtp:host=%5Busb%3A001%2C024%5D"));
//        System.out.println(NativeFacade.getFreeSpace("/dev/sdb1"));
//        System.out.println(NativeFacade.getTotalSpace("/dev/sdb1"));
//
//        System.out.println(NativeFacade.getTotalSpaceMounted("/run/user/1000/gvfs/mtp:host=%5Busb%3A001%2C003%5D"));
//        System.out.println(NativeFacade.getFreeSpaceMounted("/run/user/1000/gvfs/mtp:host=%5Busb%3A001%2C003%5D"));
//        System.out.println(new File("/dev/sdb1").getTotalSpace());
//        FileSystemView fsv = FileSystemView.getFileSystemView();
//        File[] files = File.listRoots();
//
////        File roots = new File("/run/user/1000/gvfs/mtp:host=%5Busb%3A001%2C126%5D");
//        File roots = new File("/dev/sdb1");
//        if (roots.exists()) {
//            System.out.println("ok");
//            System.out.println(fsv.getSystemDisplayName(roots));
//        }

//
//        Path usbDevicePath = FileSystems.getDefault().getPath("/sys/devices/pci0000:00/0000:00:12.2/usb1/1-5");
//        Scanner busnumScanner = new Scanner(usbDevicePath.resolve("busnum"));
//        int busnum = busnumScanner.nextInt();
//        String busnumString = String.format("%03d", busnum);
//        Scanner devnumScanner = new Scanner(usbDevicePath.resolve("devnum"));
//        int devnum = devnumScanner.nextInt();
//        String devnumString = String.format("%03d", devnum);
//            System.out.println(devnumString);
//            System.out.println(busnumString);
//        String mountPathString = URLEncoder.encode("[usb:"
//                        + busnumString
//                        + ","
//                        + devnumString
//                        + "]",
//                "UTF-8"
//        );
//        PathMatcher matcher = FileSystems.getDefault().getPathMatcher("regex:.+%5Busb%3A001%2C126%5D");
//        Files.walk(FileSystems.getDefault().getPath("/run/user/1000/gvfs/"), 1)
//                .filter((p) -> {
//                    System.out.println(p);
//                    System.out.println("----");
//            return matcher.matches(p);
//                })
//                .collect(Collectors.toList()).forEach(System.out::println);


//        System.out.println(new GVFSUSBFinder().findGVFS(null));
//        File f = new GVFSUSBFinder().findGVFS(null).toFile();
//        if (f.exists()) {
//            System.out.println(f.getFreeSpace());
//        }
//        System.out.println(new GVFSUSBFinder().findGVFS(null).toFile().exists());
    }
    public Path findGVFS(Path usbDevicePath) throws Exception {
        // 1 get busnum
        // 2 get devnum
        // 3 get uid
        // find it
        usbDevicePath = FileSystems.getDefault().getPath("/sys/devices/pci0000:00/0000:00:12.2/usb1/1-2");
        Scanner busnumScanner = new Scanner(usbDevicePath.resolve("busnum"));
        int busnum = busnumScanner.nextInt();
        Scanner devnumScanner = new Scanner(usbDevicePath.resolve("devnum"));
        int devnum = devnumScanner.nextInt();
        String devnumString = String.format("%03d", devnum);
        String busnumString = String.format("%03d", busnum);
        System.out.println(devnumString);
        System.out.println(busnumString);
        return FileSystems.getDefault().getPath("/run/user/1000/gvfs/mtp:host=" + URLEncoder.encode("[usb:" + busnumString +"," + devnumString +"]", "UTF-8"));
    }
}
