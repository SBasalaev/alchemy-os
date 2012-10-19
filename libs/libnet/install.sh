#!/bin/sh

mkdir /lib/net
install connection.eh http.eh secinfo.eh serversocket.eh socket.eh /lib/net
install native/libnet.1.so native/libnet.so /lib
