
all:

package: all
 # libcomm1
 mkdir -p lib
 install native/libcomm.1.so lib
 cat libcomm1.package > PACKAGE
 arh c libcomm1.pkg PACKAGE lib
 rm -r lib
 # libcomm-dev
 mkdir -p inc lib
 install native/libcomm.so lib
 install comm.eh inc
 cat libcomm-dev.package > PACKAGE
 arh c libcomm-dev.pkg PACKAGE inc lib
 rm -r PACKAGE lib inc