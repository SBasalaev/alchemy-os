/* I/O library
 * (C) 2011, Sergey Basalaev
 * Licensed under LGPL v3
 */

use "dataio.eh"
use "math"
use "string"

def readubyte(in: IStream): Int {
  var b = read(in)
  if (b >= 0)
    b
  else
    cast (Int) null
}

def readbyte(in: IStream): Int {
  var b = read(in)
  if (b >= 128)
    b-256
  else if (b >= 0)
    b
  else
    cast (Int) null
}

def readushort(in: IStream): Int {
  var b1 = read(in)
  var b2 = read(in)
  if (b2 < 0)
    cast (Int) null
  else
    (b1 << 8) | b2
}

def readshort(in: IStream): Int {
  var b1 = read(in)
  var b2 = read(in)
  if (b2 < 0)
    cast (Int) null
  else if (b1 >= 128)
    ((b1 << 8) | b2) - 65536
  else
    (b1 << 8) | b2
}

def readint(in: IStream): Int {
  var b1 = read(in)
  var b2 = read(in)
  var b3 = read(in)
  var b4 = read(in)
  if (b4 < 0)
    cast (Int) null
  else
    (b1 << 24) | (b2 << 16) | (b3 << 8) | b4
}

def readlong(in: IStream): Long {
  var b1:Long = read(in)
  var b2:Long = read(in)
  var b3:Long = read(in)
  var b4:Long = read(in)
  var b5:Long = read(in)
  var b6:Long = read(in)
  var b7:Long = read(in)
  var b8:Long = read(in)
  if (b8 < 0)
    cast (Long) null
  else
    (b1 << 56) | (b2 << 48) | (b3 << 40) |
    (b4 << 32) | (b5 << 24) | (b6 << 16) |
    (b7 << 8) | b8
}

def readfloat(in: IStream): Float {
  var i = readint(in)
  if (i == null)
    cast (Float) null
  else
    ibits2f(i)
}

def readdouble(in: IStream): Double {
  var l = readlong(in)
  if (l == null)
    cast (Double) null
  else
    lbits2d(l)
}

def readutf(in: IStream): String {
  var len = readushort(in)
  if (len == null) {
    cast (String) null
  } else {
    var buf = new BArray(len)
    if (readarray(in, buf, 0, len) < len) {
      cast (String) null
    } else {
      ba2utf(buf)
    }
  }
}

def writebyte(out: OStream, b: Int) {
  write(out, b)
}

def writeshort(out: OStream, s: Int) {
  write(out, s >> 8)
  write(out, s)
}

def writeint(out: OStream, i: Int) {
  write(out, i >> 24)
  write(out, i >> 16)
  write(out, i >> 8)
  write(out, i)
}

def writelong(out: OStream, l: Long) {
  write(out, l >> 56)
  write(out, l >> 48)
  write(out, l >> 40)
  write(out, l >> 32)
  write(out, l >> 24)
  write(out, l >> 16)
  write(out, l >> 8)
  write(out, l)
}

def writefloat(out: OStream, f: Float) {
  writeint(out, f2ibits(f))
}

def writedouble(out: OStream, d: Double) {
  writelong(out, d2lbits(d))
}

def writeutf(out: OStream, str: String) {
  var buf = utfbytes(str)
  writeshort(out, buf.len)
  writearray(out, buf, 0, buf.len)
}
