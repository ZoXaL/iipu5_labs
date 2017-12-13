#include <libudev.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/statvfs.h>
#include <mntent.h>
#include "lab4-jni.h"

/*
 * Class:     com_zoxal_labs_iapd_usb_nativefacade_NativeFacade
 * Method:    getDevPath
 * Signature: (Ljava/lang/String;)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_com_zoxal_labs_iapd_usb_nativefacade_NativeFacade_getDevPath (JNIEnv* env, jclass class, jstring jsyspath) {    
    struct udev* udev = udev_new();
    if (!udev) {
        return NULL;
    }
    const char* syspath = (*env)->GetStringUTFChars(env, jsyspath, NULL);
    struct udev_device* dev = udev_device_new_from_syspath(udev, syspath);
    if (!dev) {
        return NULL;
    }
    char devpath[120];
    strncpy(devpath, udev_device_get_devnode(dev), 120);
    jstring jdevpath = (*env)->NewStringUTF(env, devpath);
    udev_device_unref(dev);
    udev_unref(udev);
    return jdevpath;
}

/*
 * Class:     com_zoxal_labs_iapd_usb_nativefacade_NativeFacade
 * Method:    getDevPath
 * Signature: (Ljava/lang/String;)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_com_zoxal_labs_iapd_usb_nativefacade_NativeFacade_getMountPath(JNIEnv* env, jclass class, jstring jdevpath) {    
    const char* devpath = (*env)->GetStringUTFChars(env, jdevpath, NULL);

    FILE* mounting_file = setmntent("/proc/mounts", "r");
    if (mounting_file == NULL) {
        return NULL;
    }
    struct mntent* mount_entity;
    jstring mountPath;
    while (NULL != (mount_entity = getmntent(mounting_file))) {
        if (strstr(mount_entity->mnt_fsname, devpath)) {
            char devpathtmp[120];
            strncpy(devpathtmp, mount_entity->mnt_dir, 120);
            mountPath = (*env)->NewStringUTF(env, devpathtmp);
            endmntent(mounting_file);
            return mountPath;
        }
    }
    return NULL;
}


/*
 * Class:     com_zoxal_labs_iapd_usb_nativefacade_NativeFacade
 * Method:    getLabel
 * Signature: (Ljava/lang/String;)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_com_zoxal_labs_iapd_usb_nativefacade_NativeFacade_getLabel(JNIEnv* env, jclass class, jstring jsyspath) {
    struct udev* udev = udev_new();
    if (!udev) {
        return NULL;
    }
    const char* syspath = (*env)->GetStringUTFChars(env, jsyspath, NULL);
    struct udev_device* dev = udev_device_new_from_syspath(udev, syspath);
    if (!dev) {
        return NULL;
    }
    char label[120];
    strncpy(label, udev_device_get_property_value(dev, "ID_FS_LABEL"), 120);
    jstring jlabel = (*env)->NewStringUTF(env, label);
    udev_device_unref(dev);
    udev_unref(udev);
    return jlabel;
} 

/*
 * Class:     com_zoxal_labs_iapd_usb_nativefacade_NativeFacade
 * Method:    getLabel
 * Signature: (Ljava/lang/String;)Ljava/lang/String;
 */
JNIEXPORT jlong JNICALL Java_com_zoxal_labs_iapd_usb_nativefacade_NativeFacade_getFreeSpace(JNIEnv* env, jclass class, jstring jdevpath) {
    const char* syspath = (*env)->GetStringUTFChars(env, jdevpath, NULL);
    FILE* mounting_file = setmntent("/proc/mounts", "r");
    if (mounting_file == NULL) {
        return -1;
    }
    struct mntent* mount_entity;
    long long freeMemory = -2;
    while (NULL != (mount_entity = getmntent(mounting_file))) {
        struct statvfs fsMem;
        if((statvfs(mount_entity->mnt_dir, &fsMem)) < 0 ) {
            return -1;
        } else {
            if (strstr(mount_entity->mnt_fsname, syspath)) {
                freeMemory = fsMem.f_bsize * fsMem.f_bavail;
            }
        }
    }
    endmntent(mounting_file);
    return freeMemory;
}

/*
 * Class:     com_zoxal_labs_iapd_usb_nativefacade_NativeFacade
 * Method:    getLabel
 * Signature: (Ljava/lang/String;)Ljava/lang/String;
 */
JNIEXPORT jlong JNICALL Java_com_zoxal_labs_iapd_usb_nativefacade_NativeFacade_getTotalSpace(JNIEnv* env, jclass class, jstring jdevpath) {
    const char* syspath = (*env)->GetStringUTFChars(env, jdevpath, NULL);
    FILE* mounting_file = setmntent("/proc/mounts", "r");
    if (mounting_file == NULL) {
        return -1;
    }
    struct mntent* mount_entity;
    long long totalMemory = -2;
    while (NULL != (mount_entity = getmntent(mounting_file))) {
        struct statvfs fsMem;
        if((statvfs(mount_entity->mnt_dir, &fsMem)) < 0 ) {
            return -1;
        } else {
            if (strstr(mount_entity->mnt_fsname, syspath)) {
                totalMemory = fsMem.f_bsize * fsMem.f_blocks;
            }
        }
    }
    endmntent(mounting_file);
    return totalMemory;
}

/*
 * Class:     com_zoxal_labs_iapd_usb_nativefacade_NativeFacade
 * Method:    getLabel
 * Signature: (Ljava/lang/String;)Ljava/lang/String;
 */
JNIEXPORT jlong JNICALL Java_com_zoxal_labs_iapd_usb_nativefacade_NativeFacade_getFreeSpaceMounted(JNIEnv* env, jclass class, jstring jdevpath) {
    const char* syspath = (*env)->GetStringUTFChars(env, jdevpath, NULL);
    FILE* mounting_file = setmntent("/proc/mounts", "r");
    if (mounting_file == NULL) {
        return -1;
    }
    struct statvfs fsMem;
    if((statvfs(syspath, &fsMem)) < 0 ) {
        return -1;
    } 
    return fsMem.f_bsize * fsMem.f_bavail;
}

/*
 * Class:     com_zoxal_labs_iapd_usb_nativefacade_NativeFacade
 * Method:    getLabel
 * Signature: (Ljava/lang/String;)Ljava/lang/String;
 */
JNIEXPORT jlong JNICALL Java_com_zoxal_labs_iapd_usb_nativefacade_NativeFacade_getTotalSpaceMounted(JNIEnv* env, jclass class, jstring jdevpath) {
    const char* syspath = (*env)->GetStringUTFChars(env, jdevpath, NULL);
    FILE* mounting_file = setmntent("/proc/mounts", "r");
    if (mounting_file == NULL) {
        return -1;
    }
    struct statvfs fsMem;
    if((statvfs(syspath, &fsMem)) < 0 ) {
        return -1;
    } 
    return fsMem.f_bsize * fsMem.f_blocks;
}

// int main (int argc, char* argv[]) {
//     // struct statvfs fsMem;
//     // if((statvfs(argv[1], &fsMem)) < 0 ) {
//     //     return -1;
//     // }
//     // printf("%lu", fsMem.f_bsize * fsMem.f_bavail);

//     // getting free space
//     long long total_mem = 0;
//     long long free_mem = 0;

//     FILE* mounting_file = setmntent("/proc/mounts", "r");
//     if (mounting_file == NULL) {
//         perror("setmntent");
//         return 1;
//     }
//     struct mntent* mount_entity;
//     while (NULL != (mount_entity = getmntent(mounting_file))) {
//         struct statvfs hdMem;
//         if((statvfs(mount_entity->mnt_dir, &hdMem)) < 0 ) {
//             perror("Getting free space: ");
//         } else {
//             // printf("Disk: %s\n", mount_entity->mnt_dir);
            
//             if (strstr(mount_entity->mnt_fsname, argv[1])) {
//                 printf("Disk: %s\n", mount_entity->mnt_fsname);
//                 printf("Block size: %lu\n", hdMem.f_bsize);
//                 printf("Total blocks: %lu\n", hdMem.f_blocks);
//                 printf("Free blocks (avail): %lu\n", hdMem.f_bavail);
//                 printf("Free blocks: %lu\n", hdMem.f_bfree);

//             total_mem += hdMem.f_bsize * hdMem.f_blocks;
//             free_mem += hdMem.f_bsize * hdMem.f_bavail;
//                 printf("------\n");
//             }
            

//         }
//     }
//     printf("Total mem: %Lu, free mem: %Lu\n", 
//         total_mem, free_mem);
//     endmntent(mounting_file);

//     // struct udev* udev = udev_new();
//     // if (!udev) {
//     //     printf("Can't create udev\n");
//     //     exit(1);
//     // }
//     // struct udev_device* dev = udev_device_new_from_syspath(udev, argv[1]);
//     // printf("Device Node Path: %s\n", udev_device_get_devnode(dev));
//     // printf("Sys Node Path: %s\n", udev_device_get_syspath(dev));
//     // printf("Label: %s\n", udev_device_get_property_value(dev, "ID_FS_LABEL"));

//     // char devpath[120];
//     // strncpy(devpath, udev_device_get_devnode(dev), 120);
//     // printf("t: %s\n", devpath);

//     // udev_device_unref(dev);
//     // udev_unref(udev);
//     return 0;       
// }
