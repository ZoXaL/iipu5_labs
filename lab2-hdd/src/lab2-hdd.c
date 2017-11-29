#include <fcntl.h>
#include <unistd.h>
#include <stdio.h>
#include <stdlib.h>
#include <errno.h>
#include <string.h>
#include <sys/ioctl.h>
#include <linux/hdreg.h>
#include <sys/statvfs.h>
#include <mntent.h>


static char* strip (char* s) {
	char *e;
	while (*s == ' ') ++s;
	if (*s)
		for (e = s + strlen(s); *--e == ' '; *e = '\0');
	return s;
}

int main(int argc, char* argv[]) {
	if (argc != 2) {
		printf("You must specify device name as argument.\n");
		exit(1);
	}
	int hdd_fd = open(argv[1], O_RDONLY);
	if (hdd_fd == -1) {
		perror("/dev/sda open");
		exit(0);
	}

	__u16 hd_identity[256];

	if (!ioctl(hdd_fd, HDIO_GET_IDENTITY, hd_identity)) {
		// Print model, fw and serial
		char* model = strip(strndup((char*)((__u16*)hd_identity + 27), 40));
		char* fwrev = strip(strndup((char*)((__u16*)hd_identity + 23), 8));
		char* serno = strip(strndup((char *)&hd_identity[10], 20));
		printf("model: %.40s\nfw_rev: %.8s\nserial: %.20s\n", model, fwrev, serno);

		// Print dma modes
		char dmodes[128]={0,};
		if (hd_identity[49] & 0x100) {
			if (hd_identity[62] | hd_identity[63]) {
				if (hd_identity[62] & 0x100)	strcat(dmodes,"*");
				if (hd_identity[62] & 1)	strcat(dmodes,"sdma0 ");
				if (hd_identity[62] & 0x200)	strcat(dmodes,"*");
				if (hd_identity[62] & 2)	strcat(dmodes,"sdma1 ");
				if (hd_identity[62] & 0x400)	strcat(dmodes,"*");
				if (hd_identity[62] & 4)	strcat(dmodes,"sdma2 ");
				if (hd_identity[62] & 0xf800)	strcat(dmodes,"*");
				if (hd_identity[62] & 0xf8)	strcat(dmodes,"sdma? ");
				if (hd_identity[63] & 0x100)	strcat(dmodes,"*");
				if (hd_identity[63] & 1)	strcat(dmodes,"mdma0 ");
				if (hd_identity[63] & 0x200)	strcat(dmodes,"*");
				if (hd_identity[63] & 2)	strcat(dmodes,"mdma1 ");
				if (hd_identity[63] & 0x400)	strcat(dmodes,"*");
				if (hd_identity[63] & 4)	strcat(dmodes,"mdma2 ");
				if (hd_identity[63] & 0xf800)	strcat(dmodes,"*");
				if (hd_identity[63] & 0xf8)	strcat(dmodes,"mdma? ");
			}
		}
		printf("DMA modes: %s\n", dmodes);

		// print pio modes
		char pmodes[64] = {0,};
		
		__u8 tPIO = hd_identity[51] >> 8;
		if (tPIO <= 5) {
			strcat(pmodes, "pio0 ");
			if (tPIO >= 1) strcat(pmodes, "pio1 ");
			if (tPIO >= 2) strcat(pmodes, "pio2 ");
		}
		if ((hd_identity[49] & 0x800) || (hd_identity[53] & 2)) {
			if ((hd_identity[53] & 2)) {
				if (hd_identity[64] & 1)	strcat(pmodes, "pio3 ");
				if (hd_identity[64] & 2)	strcat(pmodes, "pio4 ");
				if (hd_identity[64] &~3)	strcat(pmodes, "pio? ");
			}
		}
		printf("PIO modes: %s\n", pmodes);


		// ata/atapi standarts
		printf("ATA versions: ");
		for (int i=0, count = 0; i <= 7; i++) {
			if (hd_identity[80] & (1<<i)) {
				printf("%u ", i);
			}
		}
		printf("\n");
		 // struct statvfs vfsd;
	  //     /* f_frsize isn't guaranteed to be supported.  */
	  //     fsp->fsu_blocksize = (vfsd.f_frsize
	  //                           ? PROPAGATE_ALL_ONES (vfsd.f_frsize)
	  //                           : PROPAGATE_ALL_ONES (vfsd.f_bsize));

	  //     fsp->fsu_blocks = PROPAGATE_ALL_ONES (vfsd.f_blocks);
	  //     fsp->fsu_bfree = PROPAGATE_ALL_ONES (vfsd.f_bfree);
	  //     fsp->fsu_bavail = PROPAGATE_TOP_BIT (vfsd.f_bavail);
	  //     fsp->fsu_bavail_top_bit_set = EXTRACT_TOP_BIT (vfsd.f_bavail) != 0;
	  //     fsp->fsu_files = PROPAGATE_ALL_ONES (vfsd.f_files);
	  //     fsp->fsu_ffree = PROPAGATE_ALL_ONES (vfsd.f_ffree);        
		
		
	} else if (errno == -ENOMSG) {
		close(hdd_fd);
		printf(" no identification info available\n");
	} else {
		perror(" HDIO_GET_IDENTITY failed");
	}

	// getting free space
	long double total_mem = 0;
	long double free_mem = 0;

	FILE* mounting_file = setmntent("/proc/mounts", "r");
	if (mounting_file == NULL) {
		perror("setmntent");
		return 1;
	}
	struct mntent* mount_entity;
	while (NULL != (mount_entity = getmntent(mounting_file))) {
		struct statvfs hdMem;
		if((statvfs(mount_entity->mnt_dir, &hdMem)) < 0 ) {
			perror("Getting free space: ");
		} else {
			// printf("Disk: %s\n", mount_entity->mnt_dir);
			// printf("Block size: %lu\n", hdMem.f_bsize);
			// printf("Total blocks: %lu\n", hdMem.f_blocks);
			// printf("Free blocks (avail): %lu\n", hdMem.f_bavail);
			// printf("Free blocks: %lu\n", hdMem.f_bfree);
			// printf("------\n");

			total_mem += hdMem.f_bsize * hdMem.f_blocks;
			free_mem += hdMem.f_bsize * hdMem.f_bavail;
		}
	}
	printf("Total mem: %.2LfG, free mem: %.2LfG, used mem: %.2LfG\n", 
		total_mem/(1024*1024*1024), free_mem/(1024*1024*1024), (total_mem - free_mem)/(1024*1024*1024));
	endmntent(mounting_file);

	return 0;
}
