ec vector.e -o vector.o
el vector.o -o libvector.0.1.so -slibvector.0.so
echo '#=libvector.0.1.so' > libvector.0.so
cp libvector.0.so libvector.so
chmod +x libvector.0.so
chmod +x libvector.so
