/* Character buffer. */

type StrBuf < Any;

def StrBuf.new(): StrBuf;
def StrBuf.get(at: Int): Char;
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
