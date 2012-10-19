/* Core library: data I/O functions
 * (C) 2011-2012 Sergey Basalaev
 * Licensed under GPL v3 with linkage exception
 */

use "dataio.eh"
use "math.eh"
use "string.eh"

def IStream.readbool(): Bool {
  var b = this.read()
  if (b >= 0)
    b != 0
  else
    null
}

def IStream.readubyte(): Int {
  var b = this.read()
  if (b >= 0)
    b
  else
    null
}

def IStream.readbyte(): Int {
  var b = this.read()
  if (b >= 128)
    b-256
  else if (b >= 0)
    b
  else
    null
}

def IStream.readushort(): Int {
  var b1 = this.read()
  var b2 = this.read()
  if (b2 < 0)
    null
  else
    (b1 << 8) | b2
}

def IStream.readshort(): Int {
  var b1 = this.read()
  var b2 = this.read()
  if (b2 < 0)
    null
  else if (b1 >= 128)
    ((b1 << 8) | b2) - 65536
  else
    (b1 << 8) | b2
}

def IStream.readint(): Int {
  var b1 = this.read()
  var b2 = this.read()
  var b3 = this.read()
  var b4 = this.read()
  if (b4 < 0)
    null
  else
    (b1 << 24) | (b2 << 16) | (b3 << 8) | b4
}

def IStream.readlong(): Long {
  var buf = new BArray(8)
  if (this.readarray(buf, 0, 8) < 8)
    null
  else {
    var l = 0l
    for (var i=0, i<8, i=i+1) {
      l = (l << 8) | (buf[i] & 0xff)
    }
    l
  }
}

def IStream.readfloat(): Float {
  var i = this.readint()
  if (i == null)
    null
  else
    ibits2f(i)
}

def IStream.readdouble(): Double {
  var l = this.readlong()
  if (l == null)
    null
  else
    lbits2d(l)
}

def IStream.readutf(): String {
  var len = this.readushort()
  if (len == null) {
    null
  } else {
    var buf = new BArray(len)
    if (this.readarray(buf, 0, len) < len) {
      null
    } else {
      ba2utf(buf)
    }
  }
}

def readbool(): Bool = stdin().readbool()
def readbyte(): Int = stdin().readbyte()
def readubyte(): Int = stdin().readubyte()
def readshort(): Int = stdin().readshort()
def readushort(): Int = stdin().readushort()
def readint(): Int = stdin().readint()
def readlong(): Long = stdin().readlong()
def readfloat(): Float = stdin().readfloat()
def readdouble(): Double = stdin().readdouble()
def readutf(): String = stdin().readutf()

def OStream.writebool(b: Bool) {
  this.write(if (b) 1 else 0)
}

def OStream.writebyte(b: Int) {
  this.write(b)
}

def OStream.writeshort(s: Int) {
  this.write(s >> 8)
  this.write(s)
}

def OStream.writeint(i: Int) {
  this.write(i >> 24)
  this.write(i >> 16)
  this.write(i >> 8)
  this.write(i)
}

def OStream.writelong(l: Long) {
  this.write(l >> 56)
  this.write(l >> 48)
  this.write(l >> 40)
  this.write(l >> 32)
  this.write(l >> 24)
  this.write(l >> 16)
  this.write(l >> 8)
  this.write(l)
}

def OStream.writefloat(f: Float) {
  this.writeint(f2ibits(f))
}

def OStream.writedouble(d: Double) {
  this.writelong(d2lbits(d))
}

def OStream.writeutf(str: String) {
  var buf = str.utfbytes()
  this.writeshort(buf.len)
  this.writearray(buf, 0, buf.len)
}

def writebool(b: Bool) = stdout().writebool(b)
def writebyte(b: Int) = stdout().writebyte(b)
def writeshort(s: Int) = stdout().writeshort(s)
def writeint(i: Int) = stdout().writeint(i)
def writelong(l: Long) = stdout().writelong(l)
def writefloat(f: Float) = stdout().writefloat(f)
def writedouble(d: Double) = stdout().writedouble(d)
def writeutf(str: String) = stdout().writeutf(str)
