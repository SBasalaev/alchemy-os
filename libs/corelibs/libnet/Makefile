HEADERS=http.eh secinfo.eh serversocket.eh socket.eh

all:

package:
 # libnet1
 mkdir -p lib
 install native/libnet.1.so lib
 cat libnet1.package > PACKAGE
 arh c libnet1.pkg PACKAGE lib
 rm -rf lib inc
 # libnet-dev
 mkdir -p lib inc/net
 install native/libnet.so lib
 install ${HEADERS} inc/net
 cat libnet-dev.package > PACKAGE
 arh c libnet-dev.pkg PACKAGE lib inc
 rm -r PACKAGE lib inc