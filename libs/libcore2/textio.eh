// text I/O routines

use "/inc/textio.eh"

const freadch = `Reader.read`
const freadstr = `Reader.readstr`
const freadca = `Reader.readarray`
const freadline = `Reader.readline`

def readstr(len: Int): String = utfreader(stdin()).readstr(len)
def readca(ca: CArray, ofs: Int, len: Int) = utfreader(stdin()).readarray(ca, ofs, len)

const fwritech = `Writer.write`
const fwritestr = `Writer.print`
const fwriteca = `Writer.writearray`

def writech(ch: Int) = utfwriter(stdout()).write(ch)
const writestr = print
def writeca(ca: CArray, ofs: Int, len: Int) = utfwriter(stdout()).writearray(ca, ofs, len)

def readch_utf8(in: IStream): Int;
def writech_utf8(out: OStream, ch: Int);

def readch_latin1(in: IStream): Int;
def writech_latin1(out: OStream, ch: Int);
