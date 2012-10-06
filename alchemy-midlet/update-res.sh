#!/bin/sh 

# Update various resources in res/

cd ../installscripts/install
arh c install.arh *
cd -
mv ../installscripts/install/install.arh res/

cd ../installscripts/update
arh c update.arh *
cd -
mv ../installscripts/update/update.arh res/

cp ../libs/libcore3/libcore30.symbols res/
cp ../libs/libui1/libui1.symbols res/
cp ../libs/libnet1/libnet1.symbols res/
cp ../libs/libcomm1/libcomm1.symbols res/