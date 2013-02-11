LIB=libcoree.3.1.so
SONAME=libcoree.3.so
LINKS=libcoree.3.0.so libcoree.3.so libcoree.so
HEADERS=builtin.eh connection.eh dataio.eh dict.eh dl.eh error.eh func.eh io.eh list.eh math.eh pipe.eh rnd.eh strbuf.eh string.eh sys.eh textio.eh time.eh
SOURCES=dataio.e io.e list.e math.e strbuf.e textio.e sys.e
OBJECTS=dataio.o io.o list.o math.o strbuf.o textio.o sys.o

all: ${LIB} ${LINKS}

${LIB}: ${OBJECTS}
 env "LIBPATH=native/" el ${OBJECTS} -o ${LIB} -s${SONAME}

libcoree.3.0.so:
 echo '#=libcoree.3.1.so' > libcoree.3.0.so
 chmod +x libcoree.3.0.so

libcoree.3.so:
 echo '#=libcoree.3.1.so' > libcoree.3.so
 chmod +x libcoree.3.so

libcoree.so:
 echo '#=libcoree.3.1.so' > libcoree.so
 chmod +x libcoree.so

dataio.o: dataio.e
 ec dataio.e -o dataio.o

io.o: io.e
 ec io.e -o io.o

list.o: list.e
 ec list.e -o list.o

math.o: math.e
 ec math.e -o math.o

strbuf.o: strbuf.e
 ec strbuf.e -o strbuf.o

textio.o: textio.e
 ec textio.e -o textio.o

sys.o: sys.e
 ec sys.e -o sys.o

clean:
 rm -f ${OBJECTS} ${LIB} ${LINKS}

install:
 install native/libcore.3.1.so native/libcore.3.0.so native/libcore.3.so native/libcore.so /lib
 install ${LIB} ${LINKS} /lib
 install ${HEADERS} /inc

package: all
 # libcore3
 mkdir -p lib
 install native/libcore.3.1.so native/libcore.3.0.so native/libcore.3.so lib
 install libcoree.3.1.so libcoree.3.0.so libcoree.3.so lib
 cat libcore3.package > PACKAGE
 arh c libcore3.pkg PACKAGE lib
 rm -r lib
 # libcore-dev
 mkdir -p lib inc
 install native/libcore.so libcoree.so lib
 install ${HEADERS} inc
 cat libcore-dev.package > PACKAGE
 arh c libcore-dev.pkg PACKAGE lib inc
 rm -r PACKAGE lib inc
