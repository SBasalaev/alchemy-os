/* Core library: Text I/O
 * (C) 2011-2012 Sergey Basalaev
 * Licensed under GPL v3 with linkage exception
 */

use "io.eh"
use "string.eh"
use "strbuf.eh"

type Reader {
  in: IStream,
  dec: (IStream):Int
}

def new_reader(in: IStream, dec: (IStream):Int): Reader {
  new Reader(in, dec)
}

def Reader.read(): Int {
  this.dec(this.in)
}

def Reader.readarray(buf: CArray, ofs: Int, len: Int): Int {
  if (len == 0) {
    0
  } else {
    var n = 0
    var ch = this.read()
    while (ch >= 0 && n < len) {
      buf[ofs+n] = ch
      n = n+1
      ch = this.read()
    }
    if (n == 0) EOF
    else n
  }
}

def Reader.readstr(len: Int): String {
  var ch = this.read()
  if (ch < 0) {
    null
  } else {
    var sb = new_strbuf()
    var n = 0
    while (n < len && ch >= 0) {
      sb.addch(ch)
      ch = this.read()
    }
    sb.tostr()
  }
}

def Reader.readline(): String {
  var ch = this.read()
  if (ch < 0)
    null
  else {
    var sb = new_strbuf()
    while (ch != '\n' && ch >= 0) {
      if (ch != '\r') sb.addch(ch)
      ch = this.read()
    }
    sb.tostr()
  }
}

def Reader.skip(n: Long): Long {
  if (n <= 0) {
    0l
  } else {
    var realskip = 0l
    var ch = this.read()
    while (ch >= 0) {
      realskip = realskip+1
      ch = this.read()
    }
    realskip
  }
}

def Reader.close() {
  this.in.close()
}

type Writer {
  out: OStream,
  enc: (OStream,Int)
}

def new_writer(out: OStream, enc: (OStream,Int)): Writer {
  new Writer(out, enc)
}

def Writer.write(ch: Int) {
  this.enc(this.out, ch)
}

def Writer.writearray(buf: CArray, ofs: Int, len: Int) {
  for (var i=0, i<len, i=i+1) {
    this.write(buf[ofs+i])
  }
}

def Writer.print(str: String) {
  var chars = str.chars()
  this.writearray(chars, 0, chars.len)
}

def Writer.println(str: String) {
  this.print(str)
  this.write('\n')
}

def Writer.printf(fmt: String, args: Array) {
  this.print(fmt.format(args))
}

def Writer.flush() {
  this.out.flush()
}

def Writer.close() {
  this.out.close()
}

/* UTF-8 encoding */
def readch_utf8(in: IStream): Int {
  var b1 = in.read()
  if (b1 < 0)
    EOF
  else if (b1 < 0x7f)
    b1
  else if ((b1 & 0xe0) == 0xc0) {
    var b2 = in.read()
    if (b2 < 0 || (b2 & 0xc0) != 0x80)
      '\uFFFD'
    else
      ((b1 & 0x1f) << 6) | (b2 & 0x3f)
  } else if ((b1 & 0xf0) == 0xe0) {
    var b2 = in.read()
    var b3 = in.read()
    if (b3 < 0 || (b2 & 0xc0) != 0x80 || (b3 & 0xc0) != 0x80)
      '\uFFFD'
    else
      ((b1 & 0xf) << 12) | ((b2 & 0x3f) << 6) | (b3 & 0x3f)
  } else {
    '\uFFFD'
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

def utfreader(in: IStream): Reader = new_reader(in, readch_utf8)
def utfwriter(out: OStream): Writer = new_writer(out, writech_utf8)

/* ISO 8859-1 encoding. */
def readch_latin1(in: IStream): Int = in.read()
def writech_latin1(out: OStream, ch: Int) = out.write(ch)

def latin1reader(in: IStream): Reader = new_reader(in, readch_latin1)
def latin1writer(out: OStream): Writer = new_writer(out, writech_latin1)

/* UTF-16 encoding */
def readch_utf16(in: IStream): Int {
  var b1 = in.read()
  if (b1 < 0) {
    EOF
  } else {
    var b2 = in.read()
    if (b2 < 0) '\uFFFD'
    else (b1 << 8) | b2
  }
}

def writech_utf16(out: OStream, ch: Int) {
  out.write(ch >> 8)
  out.write(ch)
}

def utf16reader(in: IStream): Reader = new_reader(in, readch_utf16)
def utf16writer(out: OStream): Writer = new_writer(out, writech_utf16)
