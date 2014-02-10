TARGET=coree
VERSION=4
SOURCES=array.e dataio.e i18n.e io.e math.e strbuf.e string.e sys.e textio.e

LIB=lib${TARGET}.${VERSION}.so
LINK=lib${TARGET}.so

all: ${LIB} ${LINK}

${LIB}: ${SOURCES}
 ex ${SOURCES} -I../inc -o ${LIB} -s${LIB}

${LINK}:
 echo '#=${LIB}' > ${LINK}

clean:
 rm -f ${LIB} ${LINK}

install:
 install ${LIB} ${LINK} ${DESTDIR}/lib
