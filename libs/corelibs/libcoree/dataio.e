/* Core library: data I/O functions
 * (C) 2011-2013 Sergey Basalaev
 * Licensed under GPL v3 with linkage exception
 */

use "dataio.eh"

def IStream.readBool(): Bool {
  var b = this.read()
  if (b < 0) throw(ERR_IO, "End of stream")
  b != 0
}

def IStream.readUByte(): Int {
  var b = this.read()
  if (b < 0) throw(ERR_IO, "End of stream")
  b
}

def IStream.readByte(): Byte {
  var b = this.read()
  if (b < 0) throw(ERR_IO, "End of stream")
  b.cast(Byte)
}

def IStream.readUShort(): Int {
  var b1 = this.read()
  var b2 = this.read()
  if (b2 < 0) throw(ERR_IO, "End of stream")
  ;(b1 << 8) | b2
}

def IStream.readShort(): Short {
  var b1 = this.read()
  var b2 = this.read()
  if (b2 < 0) throw(ERR_IO, "End of stream")
  ;((b1 << 8) | b2).cast(Short)
}

def IStream.readInt(): Int {
  var b1 = this.read()
  var b2 = this.read()
  var b3 = this.read()
  var b4 = this.read()
  if (b4 < 0) throw(ERR_IO, "End of stream")
  ;(b1 << 24) | (b2 << 16) | (b3 << 8) | b4
}

def IStream.readLong(): Long {
  var buf = new [Byte](8)
  if (this.readArray(buf, 0, 8) < 8)
    throw(ERR_IO, "End of stream")
  var l = 0l
  for (var i=0, i<8, i+=1) {
    l = (l << 8) | (buf[i] & 0xff)
  }
  l
}

def IStream.readFloat(): Float {
  ibits2f(this.readInt())
}

def IStream.readDouble(): Double {
  lbits2d(this.readLong())
}

def IStream.readUTF(): String {
  var len = this.readUShort()
  var buf = new [Byte](len)
  if (len > 0 && this.readArray(buf, 0, len) < len)
    throw(ERR_IO, "End of stream")
  ba2utf(buf)
}

def readBool(): Bool = stdin().readBool()
def readByte(): Byte = stdin().readByte()
def readUByte(): Int = stdin().readUByte()
def readShort(): Short = stdin().readShort()
def readUShort(): Int = stdin().readUShort()
def readInt(): Int = stdin().readInt()
def readLong(): Long = stdin().readLong()
def readFloat(): Float = stdin().readFloat()
def readDouble(): Double = stdin().readDouble()
def readUTF(): String = stdin().readUTF()

def OStream.writeBool(b: Bool) {
  this.write(if (b) 1 else 0)
}

def OStream.writeByte(b: Int) {
  this.write(b)
}

def OStream.writeShort(s: Int) {
  this.write(s >> 8)
  this.write(s)
}

def OStream.writeInt(i: Int) {
  this.write(i >> 24)
  this.write(i >> 16)
  this.write(i >> 8)
  this.write(i)
}

def OStream.writeLong(l: Long) {
  this.write(l >> 56)
  this.write(l >> 48)
  this.write(l >> 40)
  this.write(l >> 32)
  this.write(l >> 24)
  this.write(l >> 16)
  this.write(l >> 8)
  this.write(l)
}

def OStream.writeFloat(f: Float) {
  this.writeint(f2ibits(f))
}

def OStream.writeDouble(d: Double) {
  this.writelong(d2lbits(d))
}

def OStream.writeUTF(str: String) {
  var buf = str.utfbytes()
  this.writeShort(buf.len)
  if (buf.len > 0)
    this.writeArray(buf, 0, buf.len)
}

def writeBool(b: Bool) = stdout().writeBool(b)
def writeByte(b: Int) = stdout().writeByte(b)
def writeShort(s: Int) = stdout().writeShort(s)
def writeInt(i: Int) = stdout().writeInt(i)
def writeLong(l: Long) = stdout().writeLong(l)
def writeFloat(f: Float) = stdout().writeFloat(f)
def writeDouble(d: Double) = stdout().writeDouble(d)
def writeUTF(str: String) = stdout().writeUTF(str)
