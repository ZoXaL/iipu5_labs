// void printVariant();

#include <stdio.h>
#include <sys/inotify.h>
#include <sys/poll.h>
#include <unistd.h>

static int getCapacity() {
    FILE* capacityFile = fopen("/sys/class/power_supply/BAT1/capacity", "r");
    if (capacityFile == NULL) {
        return -1;
    } else {
        char currentCapacityString[3] = {10,};
        fread(&currentCapacityString, sizeof(char), 3, capacityFile);
        fclose(capacityFile);
        int currentCapacity = 0;
        for (int i = 0; i < 3; i++) {
            if (currentCapacityString[i] != 10) {
                currentCapacity *= 10;
                currentCapacity += currentCapacityString[i] - 48;             
            }
        }
        return currentCapacity; 
    }
}

static int waitForAccess(const char* path, int timeout) {
    int inotifyFd = inotify_init();
    if (inotifyFd == -1) {
        return -2;
    }

    int watchFd = inotify_add_watch(inotifyFd, "/sys/class/power_supply/BAT1/capacity", IN_ACCESS);
    if (watchFd == -1) {
        return -2;
    }

    struct pollfd inotify_poll_fd;
    inotify_poll_fd.fd = inotifyFd;
    inotify_poll_fd.events = POLLIN;
    inotify_poll_fd.revents = 0;
    if (poll(&inotify_poll_fd, 1, timeout * 1000) == 0) {
        close(inotifyFd);
        return -1;
    }
    close(inotifyFd);
    return 0;
}

int main() {
	int waitResult = waitForAccess("/sys/class/power_supply/BAT1/capacity", 15);
    printf("%d\n", (waitResult == 0) ? getCapacity() : waitResult);
    return 0;
}