#include <fcntl.h>
#include <unistd.h>
#include <stdio.h>
#include <stdlib.h>
#include <errno.h>
#include <string.h>
#include <sys/ioctl.h>
#include <linux/hdreg.h>
#include <sys/statvfs.h>
#include <stdio.h>
#include <stdlib.h>
#include <mntent.h>

int main(void) {
  struct mntent* ent;
  FILE* aFile;
  long double total_mem = 0;
  long double free_mem = 0;

  aFile = setmntent("/proc/mounts", "r");
  // aFile = setmntent("/proc/self/mountinfo", "r");
  if (aFile == NULL) {
    perror("setmntent");
    exit(1);
  }
  while (NULL != (ent = getmntent(aFile))) {
    printf("%s %s\n", ent->mnt_fsname, ent->mnt_dir);
    
    struct statvfs hdMem;
    if((statvfs(ent->mnt_dir, &hdMem)) < 0 ) {
      perror("Getting free space: ");
    } else {
      printf("Disk: %s\n", ent->mnt_dir);
      printf("Block size: %lu\n", hdMem.f_bsize);
      printf("Total blocks: %lu\n", hdMem.f_blocks);
      printf("Free blocks (avail): %lu\n", hdMem.f_bavail);
      printf("Free blocks: %lu\n", hdMem.f_bfree);
    }
    total_mem += hdMem.f_bsize*hdMem.f_blocks;
    free_mem += hdMem.f_bsize*hdMem.f_bavail;
    printf("------\n");
  }
  printf("Total mem: %.2llfG, used mem: %.2llfG", total_mem/(1024*1024*1024), free_mem/(1024*1024*1024));
  endmntent(aFile);
}