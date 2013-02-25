/* Core library: data I/O functions
 * (C) 2011-2013 Sergey Basalaev
 * Licensed under GPL v3 with linkage exception
 */

use "dataio.eh"
use "math.eh"
use "string.eh"
use "error.eh"

def IStream.readbool(): Bool {
  var b = this.read()
  if (b < 0) error(ERR_IO, "End of stream")
  b != 0
}

def IStream.readubyte(): Int {
  var b = this.read()
  if (b < 0) error(ERR_IO, "End of stream")
  b
}

def IStream.readbyte(): Byte {
  var b = this.read()
  if (b < 0) error(ERR_IO, "End of stream")
  b.cast(Byte)
}

def IStream.readushort(): Int {
  var b1 = this.read()
  var b2 = this.read()
  if (b2 < 0) error(ERR_IO, "End of stream")
  ;(b1 << 8) | b2
}

def IStream.readshort(): Short {
  var b1 = this.read()
  var b2 = this.read()
  if (b2 < 0) error(ERR_IO, "End of stream")
  ;((b1 << 8) | b2).cast(Short)
}

def IStream.readint(): Int {
  var b1 = this.read()
  var b2 = this.read()
  var b3 = this.read()
  var b4 = this.read()
  if (b4 < 0) error(ERR_IO, "End of stream")
  ;(b1 << 24) | (b2 << 16) | (b3 << 8) | b4
}

def IStream.readlong(): Long {
  var buf = new [Byte](8)
  if (this.readarray(buf, 0, 8) < 8)
    error(ERR_IO, "End of stream")
  var l = 0l
  for (var i=0, i<8, i+=1) {
    l = (l << 8) | (buf[i] & 0xff)
  }
  l
}

def IStream.readfloat(): Float {
  ibits2f(this.readint())
}

def IStream.readdouble(): Double {
  lbits2d(this.readlong())
}

def IStream.readutf(): String {
  var len = this.readushort()
  var buf = new [Byte](len)
  if (len > 0 && this.readarray(buf, 0, len) < len)
    error(ERR_IO, "End of stream")
  ba2utf(buf)
}

def readbool(): Bool = stdin().readbool()
def readbyte(): Byte = stdin().readbyte()
def readubyte(): Int = stdin().readubyte()
def readshort(): Short = stdin().readshort()
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
