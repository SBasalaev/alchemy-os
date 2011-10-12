/* I/O library
 * (C) 2011, Sergey Basalaev
 * Licensed under LGPL v3
 */

use "textio.eh"
use "string"
use "sys"

def readch(r: Reader): Int = r.dec(r.in)

def readstr(r: Reader, len: Int): String {
  var ca = new CArray(len)
  var n = readca(r, ca, 0, len)
  if (n < len) {
    var newca = new CArray(n)
    cacopy(ca, 0, newca, 0, n)
    ca = newca
  }
  ca2str(ca)
}

def readca(r: Reader, ca: CArray, ofs: Int, len: Int): Int {
  if (len == 0) {
    0
  } else {
    var n = 0
    var ch = readch(r)
    while (ch >= 0) {
      ca[ofs+n] = ch
      n = n + 1
      ch = if (n < len) readch(r) else -1
    }
    n
  }
}

def readline(r: Reader): String {
  var ch = readch(r)
  if (ch < 0)
    cast (String) null
  else {
    var sb = new_sb()
    while (ch != '\n' && ch >= 0) {
      sb_addch(sb, ch)
      ch = readch(r)
    }
    to_str(sb)
  }
}

def writech(w: Writer, ch: Int) = w.enc(w.out, ch)

def writestr(w: Writer, str: String) = writeca(w, strchars(str), 0, strlen(str))

def writeca(w: Writer, ca: CArray, ofs: Int, len: Int) {
  var i = 0
  while (i < len) {
    writech(w, ca[ofs+i])
    i = i+1
  }
}

def utfreader(in: IStream): Reader {
  var r = new Reader()
  r.in = in
  r.dec = readchUTF
  r
}

def utfwriter(out: OStream): Writer {
  var w = new Writer()
  w.out = out
  w.enc = writechUTF
  w
}

/* UTF-8 encoding */

def readchUTF(in: IStream): Int {
  var b1 = read(in)
  if (b1 < 0)
    -1
  else if (b1 < 0x7f)
    b1
  else if ((b1 & 0xe0) == 0xc0) {
    var b2 = read(in)
    if (b2 < 0 || (b2 & 0xc0) != 0x80)
      -2
    else
      ((b1 & 0x1f) << 6) | (b2 & 0x3f)
  } else if ((b1 & 0xf0) == 0xe0) {
    var b2 = read(in)
    var b3 = read(in)
    if (b3 < 0 || (b2 & 0xc0) != 0x80 || (b3 & 0xc0) != 0x80)
      -2
    else
      ((b1 & 0xf) << 12) | ((b2 & 0x3f) << 6) | (b3 & 0x3f)
  } else {
    -2
  }
}

def writechUTF(out: OStream, ch: Int) {
  if (ch == 0) {
    write(out, 0xc0)
    write(out, 0x80)
  } else if (ch <= 0x7f) {
    write(out, ch)
  } else if (ch <= 0x7ff) {
    write(out, 0xc0 | (ch >> 6))
    write(out, 0x80 | (ch & 0x3f))
  } else {
    write(out, 0xe0 | (ch >> 12))
    write(out, 0x80 | ((ch >> 6) & 0x3f))
    write(out, 0x80 | (ch & 0x3f))
  }
}
