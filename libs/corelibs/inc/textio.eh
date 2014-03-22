/* Text I/O streams. */

use "io.eh"

type Reader < Any

def Reader.new(input: IStream, dec: (IStream):Int)
def Reader.read(): Int
def Reader.readArray(buf: [Char], ofs: Int, len: Int): Int
def Reader.readString(len: Int): String
def Reader.readLine(): String
def Reader.skip(n: Long): Long
def Reader.reset()
def Reader.close()

type Writer < Any

def Writer.new(out: OStream, enc: (OStream,Int)): Writer

def Writer.write(ch: Int)
def Writer.writeArray(buf: [Char], ofs: Int, len: Int)
def Writer.print(str: String)
def Writer.println(str: String)
def Writer.printf(fmt: String, args: [Any])
def Writer.flush()
def Writer.close()

/* UTF-8 encoding */
def utfreader(input: IStream): Reader
def utfwriter(out: OStream): Writer

/* ISO 8859-1 encoding. */
def latin1reader(input: IStream): Reader
def latin1writer(out: OStream): Writer

/* UTF-16 encoding */
def utf16reader(input: IStream): Reader
def utf16writer(out: OStream): Writer

/* Reader from string. */
type StringReader < Reader
def StringReader.new(str: String)

/* Writer to string. */
type StringWriter < Writer
def StringWriter.new()
def StringWriter.tostr(): String
def StringWriter.reset()
