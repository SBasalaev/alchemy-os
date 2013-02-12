#!/bin/sh

ec dataio.e -o dataio.o
ec io.e -o io.o
ec list.e -o list.o
ec math.e -o math.o
ec strbuf.e -o strbuf.o
ec string.e -o string.o
ec sys.e -o sys.o
ec textio.e -o textio.o
env "LIBPATH=native/" el dataio.o io.o list.o math.o strbuf.o sys.o textio.o -o libcoree.3.1.so -slibcoree.3.so
echo '#=libcoree.3.1.so' > libcoree.3.0.so
echo '#=libcoree.3.1.so' > libcoree.3.so
echo '#=libcoree.3.1.so' > libcoree.so
chmod +x libcoree.3.0.so
chmod +x libcoree.3.so
chmod +x libcoree.so
