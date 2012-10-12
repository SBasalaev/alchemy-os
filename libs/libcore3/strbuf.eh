/* String buffers. */

type StrBuf < Any;

def new_strbuf(): StrBuf;

def StrBuf.ch(at: Int): Int;
const `StrBuf.get` = `StrBuf.ch`
def StrBuf.chars(from: Int, to: Int, buf: CArray, ofs: Int); 
def StrBuf.append(a: Any): StrBuf;
def StrBuf.addch(ch: Int): StrBuf;
def StrBuf.insert(at: Int, a: Any): StrBuf;
def StrBuf.insch(at: Int, ch: Int): StrBuf;
def StrBuf.replace(from: Int, to: Int, by: Any): StrBuf;
def StrBuf.setch(at: Int, ch: Int): StrBuf;
def StrBuf.delete(from: Int, to: Int): StrBuf;
def StrBuf.delch(at: Int): StrBuf;
def StrBuf.len(): Int;
