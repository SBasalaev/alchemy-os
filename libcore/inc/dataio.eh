// data I/O routines

use "io"

def freadbool(in: IStream): Bool;
def freadbyte(in: IStream): Int;
def freadubyte(in: IStream): Int;
def freadshort(in: IStream): Int;
def freadushort(in: IStream): Int;
def freadint(in: IStream): Int;
def freadlong(in: IStream): Long;
def freadfloat(in: IStream): Float;
def freaddouble(in: IStream): Double;
def freadutf(in: IStream): String;

def readbool(): Bool;
def readbyte(): Int;
def readubyte(): Int;
def readshort(): Int;
def readushort(): Int;
def readint(): Int;
def readlong(): Long;
def readfloat(): Float;
def readdouble(): Double;
def readutf(): String;

def fwritebool(out: OStream, b: Bool);
def fwritebyte(out: OStream, b: Int);
def fwriteshort(out: OStream, s: Int);
def fwriteint(out: OStream, i: Int);
def fwritelong(out: OStream, l: Long);
def fwritefloat(out: OStream, f: Float);
def fwritedouble(out: OStream, d: Double);
def fwriteutf(out: OStream, str: String);

def writebool(b: Bool);
def writebyte(b: Int);
def writeshort(s: Int);
def writeint(i: Int);
def writelong(l: Long);
def writefloat(f: Float);
def writedouble(d: Double);
def writeutf(str: String);
