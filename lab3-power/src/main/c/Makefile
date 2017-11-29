srcFolder:=./
buildFolder:=./../../../target/c
glib-include:=${shell pkg-config --cflags glib-2.0}
glib-lib:=${shell pkg-config --libs glib-2.0}
glibio-lib:=${shell pkg-config --libs gio-2.0}

CFLAGS:=-Wall -Werror -pedantic -std=gnu11 -D_REENTRANT ${glib-include} \
	-I./ -I/usr/lib/jvm/java-8-oracle/include -I/usr/lib/jvm/java-8-oracle/include/linux


${buildFolder}/liblab3-c.so: ${buildFolder}/main.o 
	@echo building $@
	@gcc -shared -o $@ $^ ${glibio-lib}

${buildFolder}/main.o: prepare ${srcFolder}/main.c
	@gcc ${CFLAGS} -fpic -c -o $@ ${srcFolder}/main.c -Wl,-Bdynamic ${glibio-lib}

${buildFolder}/main: ${buildFolder}/main.o
	@gcc ${CFLAGS} -o $@ $^ -Wl,-Bdynamic ${glibio-lib}

${buildFolder}/tester: ${srcFolder}/tester.c ${buildFolder}/liblab3-c.so
	@gcc ${CFLAGS} -o $@ ${srcFolder}/tester.c -L${buildFolder} -Wl,-rpath=./ -llab3-c

run: ${buildFolder}/main
	@cd ${buildFolder}&&./main

test: ${buildFolder}/tester
	@cd ${buildFolder}&&./tester

prepare: 
	@mkdir -p ${buildFolder}

# 0. set glib as dynamic lib
# 1. set multithreading options
# 1. build as static library