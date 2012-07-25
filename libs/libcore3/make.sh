#!/bin/sh

ec dataio.e
ec io.e
ec list.e
ec math.e
ec strbuf.e
ec textio.e
el dataio.o io.o list.o math.o strbuf.o textio.o -o libcoree.3.0.so -slibcoree.3.so
echo '#=libcoree.3.0.so' > libcoree.3.so
echo '#=libcoree.3.0.so' > libcoree.so
chmod +x libcoree.3.so
chmod +x libcoree.so
