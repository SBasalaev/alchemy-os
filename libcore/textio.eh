// text I/O routines

use "io"

type ReadChar = (IStream):Int;
type WriteChar = (OStream,Int);

type Reader {
  in: IStream,
  dec: ReadChar
}

type Writer {
  out: OStream,
  enc: WriteChar
}

def freadch(r: Reader): Int;
def freadstr(r: Reader, len: Int): String;
def freadca(r: Reader, ca: CArray, ofs: Int, len: Int): Int;
def freadline(r: Reader): String;

def fwritech(w: Writer, ch: Int);
def fwritestr(w: Writer, str: String);
def fwriteca(w: Writer, ca: CArray, ofs: Int, len: Int);

/* UTF-8 encoding */
def utfreader(in: IStream): Reader;
def utfwriter(out: OStream): Writer;

def readch_utf8(in: IStream): Int;
def writech_utf8(out: OStream, ch: Int);

/* ISO 8859-1 encoding. */
def latin1reader(in: IStream): Reader;
def latin1writer(out: OStream): Writer;

def readch_latin1(in: IStream): Int;
def writech_latin1(out: OStream, ch: Int);
