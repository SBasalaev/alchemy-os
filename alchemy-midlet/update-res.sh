#!/bin/sh 

echo 'Updating installscripts...'
cd ../installscripts/install
# Create empty directories in case git got rid of them
mkdir -p bin lib cfg res dev tmp home cfg/pkg/db/sources cfg/pkg/db/lists
arh c install.arh *
cd -
mv ../installscripts/install/install.arh res/

echo 'Updating library symbols...'
cp ../libs/libcore/libcore31.symbols res/
cp ../libs/libui/libui1.symbols res/
cp ../libs/libnet/libnet1.symbols res/
cp ../libs/libcomm/libcomm1.symbols res/
cp ../libs/libmedia/libmedia1.symbols res/
