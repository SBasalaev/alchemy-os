// data I/O routines

use "io"

def IStream.readbool(): Bool;
def IStream.readbyte(): Int;
def IStream.readubyte(): Int;
def IStream.readshort(): Int;
def IStream.readushort(): Int;
def IStream.readint(): Int;
def IStream.readlong(): Long;
def IStream.readfloat(): Float;
def IStream.readdouble(): Double;
def IStream.readutf(): String;

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

def OStream.writebool(b: Bool);
def OStream.writebyte(b: Int);
def OStream.writeshort(s: Int);
def OStream.writeint(i: Int);
def OStream.writelong(l: Long);
def OStream.writefloat(f: Float);
def OStream.writedouble(d: Double);
def OStream.writeutf(str: String);

def writebool(b: Bool);
def writebyte(b: Int);
def writeshort(s: Int);
def writeint(i: Int);
def writelong(l: Long);
def writefloat(f: Float);
def writedouble(d: Double);
def writeutf(str: String);
