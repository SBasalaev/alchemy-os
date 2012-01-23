/* I/O library
 * (C) 2011, Sergey Basalaev
 * Licensed under LGPL v3
 */

use "io"

def readbyte(in: IStream): Int;
def readubyte(in: IStream): Int;
def readshort(in: IStream): Int;
def readushort(in: IStream): Int;
def readint(in: IStream): Int;
def readlong(in: IStream): Long;
def readfloat(in: IStream): Float;
def readdouble(in: IStream): Double;
def readutf(in: IStream): String;

def writebyte(out: OStream, b: Int);
def writeshort(out: OStream, s: Int);
def writeint(out: OStream, i: Int);
def writelong(out: OStream, l: Long);
def writefloat(out: OStream, f: Float);
def writedouble(out: OStream, d: Double);
def writeutf(out: OStream, str: String);
