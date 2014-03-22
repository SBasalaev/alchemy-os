// data I/O routines

use "io.eh"

def IStream.readBool(): Bool
def IStream.readByte(): Byte
def IStream.readUByte(): Int
def IStream.readShort(): Short
def IStream.readUShort(): Int
def IStream.readInt(): Int
def IStream.readLong(): Long
def IStream.readFloat(): Float
def IStream.readDouble(): Double
def IStream.readUTF(): String

def readBool(): Bool
def readByte(): Byte
def readUByte(): Int
def readShort(): Short
def readUShort(): Int
def readInt(): Int
def readLong(): Long
def readFloat(): Float
def readDouble(): Double
def readUTF(): String

def OStream.writeBool(b: Bool)
def OStream.writeByte(b: Int)
def OStream.writeShort(s: Int)
def OStream.writeInt(i: Int)
def OStream.writeLong(l: Long)
def OStream.writeFloat(f: Float)
def OStream.writeDouble(d: Double)
def OStream.writeUTF(str: String)

def writeBool(b: Bool)
def writeByte(b: Int)
def writeShort(s: Int)
def writeInt(i: Int)
def writeLong(l: Long)
def writeFloat(f: Float)
def writeDouble(d: Double)
def writeUTF(str: String)
