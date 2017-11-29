#include <stdio.h>

#include <stdlib.h>
#include <unistd.h>
#include <stdio.h>
#include <string.h>
#include <glib.h>
#include <gio/gio.h>

#include <sys/inotify.h>
#include <signal.h>
#include <sys/poll.h>
#include <errno.h>
#include <limits.h>

#include "lab3-jni.h"

#define ASCII_NEW_LINE  10
#define ASCII_ZERO      48

#define EVENTS_COUNT_BUFFER 5
#define EVENTS_BYTE_BUFFER sizeof(struct inotify_event)*EVENTS_COUNT_BUFFER

#define TIMEOUT_ERROR   -1
#define COMMON_ERROR    -2

static int waitForAccess(const char* path, int timeout) {
    int inotifyFd = inotify_init();
    if (inotifyFd == -1) {
        return COMMON_ERROR;
    }

    int watchFd = inotify_add_watch(inotifyFd, "/sys/class/power_supply/BAT1/capacity", IN_ACCESS);
    if (watchFd == -1) {
        return COMMON_ERROR;
    }

    struct pollfd inotify_poll_fd;
    inotify_poll_fd.fd = inotifyFd;
    inotify_poll_fd.events = POLLIN;
    inotify_poll_fd.revents = 0;
    if (poll(&inotify_poll_fd, 1, timeout * 1000) == 0) {
        close(inotifyFd);
        return TIMEOUT_ERROR;
    }
    close(inotifyFd);
    return 0;
}

static int getCapacity() {
    FILE* capacityFile = fopen("/sys/class/power_supply/BAT1/capacity", "r");
    if (capacityFile == NULL) {
        return COMMON_ERROR;
    } 
    char currentCapacityString[3] = {ASCII_NEW_LINE,};
    fread(&currentCapacityString, sizeof(char), 3, capacityFile);
    fclose(capacityFile);
    int currentCapacity = 0;
    for (int i = 0; i < 3; i++) {
        if (currentCapacityString[i] != ASCII_NEW_LINE) {
            currentCapacity *= 10;
            currentCapacity += currentCapacityString[i] - ASCII_ZERO;             
        }
    }
    return currentCapacity; 
}

static int getSupplier() {
    FILE* supplierFile = fopen("/sys/class/power_supply/ADP1/online", "r");
    if (supplierFile == NULL) {
        return COMMON_ERROR;
    } else {
        char currentSupplierString;
        fread(&currentSupplierString, sizeof(char), 1, supplierFile);
        fclose(supplierFile);
        return currentSupplierString - ASCII_ZERO; 
    }
}

JNIEXPORT jint JNICALL Java_com_zoxal_labs_iapd_power_NativeFacade_getSleepTimeout (JNIEnv* env, jclass class) {
    GSettings* settings = g_settings_new("org.gnome.desktop.session");
    GVariant* key_value_variant = g_settings_get_value(settings, "idle-delay");
    guint32 key_value;
    g_variant_get(key_value_variant, "u", &key_value);
    return (int)key_value;
}

JNIEXPORT void JNICALL Java_com_zoxal_labs_iapd_power_NativeFacade_setSleepTimeout (JNIEnv* env, jclass class, jint value) {
    GSettings* settings = g_settings_new("org.gnome.desktop.session");
    guint32 new_value = value;
    GVariant* new_value_variant = g_variant_new("u", new_value);
    g_settings_set_value(settings, "idle-delay", new_value_variant);
    g_settings_apply(settings);
    g_settings_sync();
}

JNIEXPORT jint JNICALL Java_com_zoxal_labs_iapd_power_NativeFacade_getCurrentCapacity (JNIEnv* env, jclass class) {
    return getCapacity();
}

JNIEXPORT jint JNICALL Java_com_zoxal_labs_iapd_power_NativeFacade_getCurrentSupplier (JNIEnv* env, jclass class) {
    return getSupplier();
}

// returns -2 on error, -1 if there was no changes during timeout
// 0 on ADP unplug, 1 on ADP plug
JNIEXPORT jint JNICALL Java_com_zoxal_labs_iapd_power_NativeFacade_waitPowerCapacityChange (JNIEnv* env, jclass class, jint timeout) {
    int waitResult = waitForAccess("/sys/class/power_supply/BAT1/capacity", timeout);
    return (waitResult == 0) ? getCapacity() : waitResult;
}

JNIEXPORT jint JNICALL Java_com_zoxal_labs_iapd_power_NativeFacade_waitPowerSupplyChange (JNIEnv* env, jclass class, jint timeout) {
    int waitResult = waitForAccess("/sys/class/power_supply/BAT1/capacity", timeout);
    return (waitResult == 0) ? getSupplier() : waitResult;
}

void test() {
    printf("Test ok");
}

int main() {
    test();
	return 0;
}