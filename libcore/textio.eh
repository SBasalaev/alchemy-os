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

def readch(r: Reader): Int;
def readstr(r: Reader, len: Int): String;
def readca(r: Reader, ca: CArray, ofs: Int, len: Int): Int;
def readline(r: Reader): String;

def writech(w: Writer, ch: Int);
def writestr(w: Writer, str: String);
def writeca(w: Writer, ca: CArray, ofs: Int, len: Int);

def utfreader(in: IStream): Reader;
def utfwriter(out: OStream): Writer;

/* UTF-8 encoding */
def readchUTF(in: IStream): Int;
def writechUTF(out: OStream, ch: Int);
