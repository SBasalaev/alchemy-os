/* I/O library
 * (C) 2011, Sergey Basalaev
 * Licensed under LGPL v3
 */

use "textio.eh"
use "string"
use "sys"

def freadch(r: Reader): Int = r.dec(r.in)

def freadstr(r: Reader, len: Int): String {
  var ca = new CArray(len)
  var n = freadca(r, ca, 0, len)
  if (n < len) {
    var newca = new CArray(n)
    cacopy(ca, 0, newca, 0, n)
    ca = newca
  }
  ca2str(ca)
}

def freadca(r: Reader, ca: CArray, ofs: Int, len: Int): Int {
  if (len == 0) {
    0
  } else {
    var n = 0
    var ch = freadch(r)
    while (ch >= 0) {
      ca[ofs+n] = ch
      n = n + 1
      ch = if (n < len) freadch(r) else -1
    }
    n
  }
}

def freadline(r: Reader): String {
  var ch = freadch(r)
  if (ch < 0)
    cast (String) null
  else {
    var sb = new_sb()
    while (ch != '\n' && ch >= 0) {
      sb_addch(sb, ch)
      ch = freadch(r)
    }
    to_str(sb)
  }
}

def fwritech(w: Writer, ch: Int) = w.enc(w.out, ch)

def fwritestr(w: Writer, str: String) = fwriteca(w, strchars(str), 0, strlen(str))

def fwriteca(w: Writer, ca: CArray, ofs: Int, len: Int) {
  var i = 0
  while (i < len) {
    fwritech(w, ca[ofs+i])
    i = i+1
  }
}

/* UTF-8 encoding */

def utfreader(in: IStream): Reader {
  var r = new Reader()
  r.in = in
  r.dec = readch_utf8
  r
}

def utfwriter(out: OStream): Writer {
  var w = new Writer()
  w.out = out
  w.enc = writech_utf8
  w
}

def readch_utf8(in: IStream): Int {
  var b1 = fread(in)
  if (b1 < 0)
    -1
  else if (b1 < 0x7f)
    b1
  else if ((b1 & 0xe0) == 0xc0) {
    var b2 = fread(in)
    if (b2 < 0 || (b2 & 0xc0) != 0x80)
      '?'
    else
      ((b1 & 0x1f) << 6) | (b2 & 0x3f)
  } else if ((b1 & 0xf0) == 0xe0) {
    var b2 = fread(in)
    var b3 = fread(in)
    if (b3 < 0 || (b2 & 0xc0) != 0x80 || (b3 & 0xc0) != 0x80)
      '?'
    else
      ((b1 & 0xf) << 12) | ((b2 & 0x3f) << 6) | (b3 & 0x3f)
  } else {
    '?'
  }
}

def writech_utf8(out: OStream, ch: Int) {
  if (ch == 0) {
    fwrite(out, 0xc0)
    fwrite(out, 0x80)
  } else if (ch <= 0x7f) {
    fwrite(out, ch)
  } else if (ch <= 0x7ff) {
    fwrite(out, 0xc0 | (ch >> 6))
    fwrite(out, 0x80 | (ch & 0x3f))
  } else {
    fwrite(out, 0xe0 | (ch >> 12))
    fwrite(out, 0x80 | ((ch >> 6) & 0x3f))
    fwrite(out, 0x80 | (ch & 0x3f))
  }
}

/* ISO 8859-1 encoding. */

def latin1reader(in: IStream): Reader {
  var r = new Reader()
  r.in = in
  r.dec = readch_latin1
  r
}

def latin1writer(out: OStream): Writer {
  var w = new Writer()
  w.out = out
  w.enc = writech_latin1
  w
}

def readch_latin1(in: IStream): Int {
  var b = fread(in)
  if (b >= 0) b else -1
}

def writech_latin1(out: OStream, ch: Int) {
  fwrite(out, ch)
}
