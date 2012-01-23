ec dataio.e -o dataio.o
ec textio.e -o textio.o
el dataio.o textio.o -o libio.0.so -slibio.0.so
echo '#=libio.0.so' > libio.so
chmod +x libio.so
