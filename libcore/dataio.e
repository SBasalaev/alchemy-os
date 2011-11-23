/* I/O library
 * (C) 2011, Sergey Basalaev
 * Licensed under LGPL v3
 */

use "dataio.eh"
use "math"
use "string"

def freadbool(in: IStream): Bool {
  var b = fread(in)
  if (b >= 0)
    b != 0
  else
    cast (Bool) null
}

def freadubyte(in: IStream): Int {
  var b = fread(in)
  if (b >= 0)
    b
  else
    cast (Int) null
}

def freadbyte(in: IStream): Int {
  var b = fread(in)
  if (b >= 128)
    b-256
  else if (b >= 0)
    b
  else
    cast (Int) null
}

def freadushort(in: IStream): Int {
  var b1 = fread(in)
  var b2 = fread(in)
  if (b2 < 0)
    cast (Int) null
  else
    (b1 << 8) | b2
}

def freadshort(in: IStream): Int {
  var b1 = fread(in)
  var b2 = fread(in)
  if (b2 < 0)
    cast (Int) null
  else if (b1 >= 128)
    ((b1 << 8) | b2) - 65536
  else
    (b1 << 8) | b2
}

def freadint(in: IStream): Int {
  var b1 = fread(in)
  var b2 = fread(in)
  var b3 = fread(in)
  var b4 = fread(in)
  if (b4 < 0)
    cast (Int) null
  else
    (b1 << 24) | (b2 << 16) | (b3 << 8) | b4
}

def freadlong(in: IStream): Long {
  var b1:Long = fread(in)
  var b2:Long = fread(in)
  var b3:Long = fread(in)
  var b4:Long = fread(in)
  var b5:Long = fread(in)
  var b6:Long = fread(in)
  var b7:Long = fread(in)
  var b8:Long = fread(in)
  if (b8 < 0)
    cast (Long) null
  else
    (b1 << 56) | (b2 << 48) | (b3 << 40) |
    (b4 << 32) | (b5 << 24) | (b6 << 16) |
    (b7 << 8) | b8
}

def freadfloat(in: IStream): Float {
  var i = freadint(in)
  if (i == null)
    cast (Float) null
  else
    ibits2f(i)
}

def freaddouble(in: IStream): Double {
  var l = freadlong(in)
  if (l == null)
    cast (Double) null
  else
    lbits2d(l)
}

def freadutf(in: IStream): String {
  var len = freadushort(in)
  if (len == null) {
    cast (String) null
  } else {
    var buf = new BArray(len)
    if (freadarray(in, buf, 0, len) < len) {
      cast (String) null
    } else {
      ba2utf(buf)
    }
  }
}

def readbool(): Bool = freadbool(stdin())
def readbyte(): Int = freadbyte(stdin())
def readubyte(): Int = freadubyte(stdin())
def readshort(): Int = freadshort(stdin())
def readushort(): Int = freadushort(stdin())
def readint(): Int = freadint(stdin())
def readlong(): Long = freadlong(stdin())
def readfloat(): Float = freadfloat(stdin())
def readdouble(): Double = freaddouble(stdin())
def readutf(): String = freadutf(stdin())

def fwritebool(out: OStream, b: Bool) {
  fwrite(out, if (b) 1 else 0)
}

def fwritebyte(out: OStream, b: Int) {
  fwrite(out, b)
}

def fwriteshort(out: OStream, s: Int) {
  fwrite(out, s >> 8)
  fwrite(out, s)
}

def fwriteint(out: OStream, i: Int) {
  fwrite(out, i >> 24)
  fwrite(out, i >> 16)
  fwrite(out, i >> 8)
  fwrite(out, i)
}

def fwritelong(out: OStream, l: Long) {
  fwrite(out, l >> 56)
  fwrite(out, l >> 48)
  fwrite(out, l >> 40)
  fwrite(out, l >> 32)
  fwrite(out, l >> 24)
  fwrite(out, l >> 16)
  fwrite(out, l >> 8)
  fwrite(out, l)
}

def fwritefloat(out: OStream, f: Float) {
  fwriteint(out, f2ibits(f))
}

def fwritedouble(out: OStream, d: Double) {
  fwritelong(out, d2lbits(d))
}

def fwriteutf(out: OStream, str: String) {
  var buf = utfbytes(str)
  fwriteshort(out, buf.len)
  fwritearray(out, buf, 0, buf.len)
}

def writebool(b: Bool) = fwritebool(stdout(), b)
def writebyte(b: Int) = fwritebyte(stdout(), b)
def writeshort(s: Int) = fwriteshort(stdout(), s)
def writeint(i: Int) = fwriteint(stdout(), i)
def writelong(l: Long) = fwritelong(stdout(), l)
def writefloat(f: Float) = fwritefloat(stdout(), f)
def writedouble(d: Double) = fwritedouble(stdout(), d)
def writeutf(str: String) = fwriteutf(stdout(), str)
