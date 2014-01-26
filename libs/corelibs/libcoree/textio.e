/* Core library: Text I/O
 * (C) 2011-2013 Sergey Basalaev
 * Licensed under GPL v3 with linkage exception
 */

use "io"
use "strbuf"

type Reader {
  inp: IStream,
  dec: (IStream):Int
}

def Reader.new(inp: IStream, dec: (IStream):Int) {
  this.inp = inp
  this.dec = dec
}

def Reader.read(): Int {
  return this.dec(this.inp)
}

def Reader.readArray(buf: [Char], ofs: Int, len: Int): Int {
  if (len == 0) return 0
  var n = 0
  var ch = this.read()
  while (ch >= 0 && n < len) {
    buf[ofs+n] = ch
    n += 1
    ch = this.read()
  }
  if (n == 0) return EOF
  else return n
}

def Reader.readString(len: Int): String {
  var ch = this.read()
  if (ch < 0) return null
  var sb = new StrBuf()
  var n = 0
  while (n < len && ch >= 0) {
    sb.addch(ch)
    ch = this.read()
  }
  return sb.tostr()
}

def Reader.readLine(): String {
  var ch = this.read()
  if (ch < 0) return null
  var sb = new StrBuf()
  while (ch != '\n' && ch >= 0) {
    if (ch != '\r') sb.addch(ch)
    ch = this.read()
  }
  return sb.tostr()
}

def Reader.skip(n: Long): Long {
  if (n <= 0) return 0L
  var realskip = 0l
  var ch = this.read()
  while (ch >= 0) {
    realskip = realskip+1
    ch = this.read()
  }
  return realskip
}

def Reader.reset() {
  this.inp.reset()
}

def Reader.close() {
  this.inp.close()
}

type Writer {
  out: OStream,
  enc: (OStream,Int)
}

def Writer.new(out: OStream, enc: (OStream,Int)): Writer {
  this.out = out
  this.enc = enc
}

def Writer.write(ch: Int) {
  this.enc(this.out, ch)
}

def Writer.writeArray(buf: [Char], ofs: Int, len: Int) {
  for (var i=0, i<len, i += 1) {
    this.write(buf[ofs+i])
  }
}

def Writer.print(str: String) {
  var chars = str.chars()
  this.writeArray(chars, 0, chars.len)
}

def Writer.println(str: String) {
  this.print(str)
  this.write('\n')
}

def Writer.printf(fmt: String, args: [Any]) {
  this.print(fmt.format(args))
}

def Writer.flush() {
  this.out.flush()
}

def Writer.close() {
  this.out.close()
}

/* ISO 8859-1 encoding. */
def latin1reader(inp: IStream): Reader = new Reader(inp, `IStream.read`)
def latin1writer(out: OStream): Writer = new Writer(out, `OStream.write`)

/* UTF-8 encoding */
def readch_utf8(inp: IStream): Int {
  var b1 = inp.read()
  if (b1 < 0)
    return EOF
  else if (b1 < 0x7f)
    return b1
  else if ((b1 & 0xe0) == 0xc0) {
    var b2 = inp.read()
    if (b2 < 0 || (b2 & 0xc0) != 0x80)
      return '\uFFFD'
    else
      return ((b1 & 0x1f) << 6) | (b2 & 0x3f)
  } else if ((b1 & 0xf0) == 0xe0) {
    var b2 = inp.read()
    var b3 = inp.read()
    if (b3 < 0 || (b2 & 0xc0) != 0x80 || (b3 & 0xc0) != 0x80)
      return '\uFFFD'
    else
      return ((b1 & 0xf) << 12) | ((b2 & 0x3f) << 6) | (b3 & 0x3f)
  } else {
    return '\uFFFD'
  }
}

def writech_utf8(out: OStream, ch: Int) {
  if (ch == 0) {
    out.write(0xc0)
    out.write(0x80)
  } else if (ch <= 0x7f) {
    out.write(ch)
  } else if (ch <= 0x7ff) {
    out.write(0xc0 | (ch >> 6))
    out.write(0x80 | (ch & 0x3f))
  } else {
    out.write(0xe0 | (ch >> 12))
    out.write(0x80 | ((ch >> 6) & 0x3f))
    out.write(0x80 | (ch & 0x3f))
  }
}

def utfreader(inp: IStream): Reader = new Reader(inp, readch_utf8)
def utfwriter(out: OStream): Writer = new Writer(out, writech_utf8)

/* UTF-16 encoding */
def readch_utf16(inp: IStream): Int {
  var b1 = inp.read()
  if (b1 < 0) return EOF
  var b2 = inp.read()
  if (b2 < 0) return '\uFFFD'
  else return (b1 << 8) | b2
}

def writech_utf16(out: OStream, ch: Int) {
  out.write(ch >> 8)
  out.write(ch)
}

def utf16reader(inp: IStream): Reader = new Reader(inp, readch_utf16)
def utf16writer(out: OStream): Writer = new Writer(out, writech_utf16)
