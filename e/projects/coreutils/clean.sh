#!/bin/sh

ec rm.e -o rm.o
el rm.o -o rm
./rm cat.e.o cat chmod.e.o chmod cp.e.o cp date.e.o date
./rm echo.e.o echo env.e.o env install.e.o install
./rm ls.e.o ls mkdir.e.o mkdir mv.e.o mv rm.e.o
./rm rm
