LIB=libcoree.3.1.so
SONAME=libcoree.3.so
LINKS=libcoree.3.0.so libcoree.3.so libcoree.so
HEADERS=builtin.eh connection.eh dataio.eh dict.eh dl.eh error.eh func.eh i18n.eh io.eh list.eh math.eh pipe.eh process.eh rnd.eh strbuf.eh string.eh sys.eh textio.eh time.eh
SOURCES=dataio.e i18n.e io.e list.e math.e strbuf.e string.e sys.e textio.e
OBJECTS=dataio.o i18n.o io.o list.o math.o strbuf.o string.o sys.o textio.o

all: ${LIB} ${LINKS}

${LIB}: ${OBJECTS}
 el ${OBJECTS} -o ${LIB} -s${SONAME}

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

i18n.o: i18n.e
 ec i18n.e -o i18n.o

io.o: io.e
 ec io.e -o io.o

list.o: list.e
 ec list.e -o list.o

math.o: math.e
 ec math.e -o math.o

strbuf.o: strbuf.e
 ec strbuf.e -o strbuf.o

string.o: string.e
 ec string.e -o string.o

sys.o: sys.e
 ec sys.e -o sys.o

textio.o: textio.e
 ec textio.e -o textio.o

clean:
 rm -f ${OBJECTS} ${LIB} ${LINKS}

install:
 install native/libcore.3.1.so native/libcore.3.0.so native/libcore.3.so native/libcore.so ${DESTDIR}/lib
 install ${LIB} ${LINKS} ${DESTDIR}/lib
 install ${HEADERS} ${DESTDIR}/inc
