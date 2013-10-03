/* String buffers. */

type StrBuf < Any;

def new_strbuf(): StrBuf;
const `StrBuf.new` = new_strbuf

def StrBuf.ch(at: Int): Char;
const `StrBuf.get` = `StrBuf.ch`

def StrBuf.chars(from: Int, to: Int, buf: [Char], ofs: Int); 
def StrBuf.append(a: Any): StrBuf;
def StrBuf.addch(ch: Char): StrBuf;
def StrBuf.insert(at: Int, a: Any): StrBuf;
def StrBuf.insch(at: Int, ch: Char): StrBuf;
def StrBuf.replace(from: Int, to: Int, by: String): StrBuf;
def StrBuf.setch(at: Int, ch: Char): StrBuf;
def StrBuf.delete(from: Int, to: Int): StrBuf;
def StrBuf.delch(at: Int): StrBuf;
def StrBuf.len(): Int;
