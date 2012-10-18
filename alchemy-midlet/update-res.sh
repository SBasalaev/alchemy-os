#!/bin/sh 

echo 'Updating installscripts...'
cd ../installscripts/install
arh c install.arh *
cd -
mv ../installscripts/install/install.arh res/

echo 'Updating library symbols...'
cp ../libs/libcore3/libcore30.symbols res/
cp ../libs/libui1/libui1.symbols res/
cp ../libs/libnet1/libnet1.symbols res/
cp ../libs/libcomm1/libcomm1.symbols res/