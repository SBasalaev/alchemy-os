
all:

package: all
 # libmedia1
 mkdir -p lib
 install native/libmedia.1.so lib
 cat libmedia1.package > PACKAGE
 arh c libmedia1.pkg PACKAGE lib
 rm -r lib
 # libmedia-dev
 mkdir -p lib inc
 install native/libmedia.so lib
 install media.eh inc
 cat libmedia-dev.package > PACKAGE
 arh c libmedia-dev.pkg PACKAGE lib inc
 rm -r PACKAGE lib inc