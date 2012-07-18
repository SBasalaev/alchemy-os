/* Text I/O streams. */

use "io.eh"

type Reader < Any;
type Writer < Any;

def new_reader(in: IStream, dec: (IStream):Int): Reader;

def Reader.read(): Int;
def Reader.readarray(buf: CArray, ofs: Int, len: Int): Int;
def Reader.readstr(len: Int): String;
def Reader.readline(): String;
def Reader.skip(n: Long): Long;
def Reader.close();

def new_writer(out: OStream, enc: (OStream,Int)): Writer;

def Writer.write(ch: Int);
def Writer.writearray(buf: CArray, ofs: Int, len: Int);
def Writer.print(str: String);
def Writer.println(str: String);
def Writer.printf(fmt: String, args: Array);
def Writer.flush();
def Writer.close();

/* UTF-8 encoding */
def utfreader(in: IStream): Reader;
def utfwriter(out: OStream): Writer;

/* ISO 8859-1 encoding. */
def latin1reader(in: IStream): Reader;
def latin1writer(out: OStream): Writer;

/* UTF-16 encoding */
def utf16reader(in: IStream): Reader;
def utf16writer(out: OStream): Writer;
