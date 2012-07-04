#!/bin/sh

echo '#=libcore.so' > native/libcoree.so
ec io.e -o io.o
ec dataio.e -o dataio.o
ec textio.e -o textio.o
ec vector.e -o vector.o
env LIBPATH=./native el -o libcoree.2.1.so -slibcoree.2.1.so io.o dataio.o textio.o vector.o
echo '#=libcoree.2.1.so' > libcoree.2.0.so
echo '#=libcoree.2.1.so' > libcoree.2.so
echo '#=libcoree.2.1.so' > libcoree.so
chmod +x libcoree.2.0.so
chmod +x libcoree.2.so
chmod +x libcoree.so
