install dataio.eh textio.eh /inc
install libio.0.0.so /lib
echo '#=libio.0.0.so' > /lib/libio.0.so
echo '#=libio.0.0.so' > /lib/libio.so
chmod +x /lib/libio.0.so
chmod +x /lib/libio.so
