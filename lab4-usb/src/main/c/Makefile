srcFolder:=./
buildFolder:=./../../../target/c

CFLAGS:=-Wall -Werror -pedantic -std=gnu11 -D_REENTRANT \
	-I./ -I/usr/lib/jvm/java-8-oracle/include -I/usr/lib/jvm/java-8-oracle/include/linux


${buildFolder}/liblab4-c.so: ${buildFolder}/main.o 
	@echo building $@
	@gcc -shared -o $@ $^ -ludev

${buildFolder}/main.o: prepare ${srcFolder}/main.c
	@gcc ${CFLAGS} -fpic -c -o $@ ${srcFolder}/main.c -Wl,-Bdynamic -ludev

${buildFolder}/main: ${buildFolder}/main.o
	@gcc ${CFLAGS} -o $@ $^ -Wl,-Bdynamic -ludev

run: ${buildFolder}/main
	@cd ${buildFolder}&&./main

prepare: 
	@mkdir -p ${buildFolder}