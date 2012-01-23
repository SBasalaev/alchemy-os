#!/bin/sh

ec cat.e -o cat.o
ec chmod.e -o chmod.o
ec cp.e -o cp.o
ec date.e -o date.o
ec echo.e -o echo.o
ec env.e -o env.o
ec install.e -o install.o
ec ls.e -o ls.o
ec mkdir.e -o mkdir.o
ec mv.e -o mv.o
ec rm.e -o rm.o

el cat.o -o cat
el chmod.o -o chmod
el cp.o -o cp
el date.o -o date
el echo.o -o echo
el env.o -o env
el install.o -o install
el ls.o -o ls
el mkdir.o -o mkdir
el mv.o -o mv
el rm.o -o rm
