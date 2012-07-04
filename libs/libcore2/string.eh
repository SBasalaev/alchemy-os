// operations on strings
// and string buffers

type StrBuf;

def to_str(a: Any): String;

def strlen(s: String): Int;
def strchr(s: String, at: Int): Int;
def strindex(s: String, ch: Int): Int;
def strlindex(s: String, ch: Int): Int;
def strstr(s: String, sub: String): Int;
def substr(s: String, from: Int, to: Int): String;
def strucase(s: String): String;
def strlcase(s: String): String;
def strcat(s1: String, s2: String): String;
def strcmp(s1: String, s2: String): Int;
def strtrim(s: String): String;
def strsplit(s: String, ch: Int): [String];
def sprintf(fmt: String, args: Array): String;

def strchars(s1: String): CArray;
def utfbytes(s: String): BArray;
def ca2str(ca: CArray): String;
def ba2utf(ba: BArray): String;

def parsei(s: String): Int;
def parsel(s: String): Long;
def parsef(s: String): Float;
def parsed(s: String): Double;

def new_sb(): StrBuf;
def sb_append(sb: StrBuf, a: Any): StrBuf;
def sb_addch(sb: StrBuf, ch: Int): StrBuf;
def sb_delete(sb: StrBuf, from: Int, to: Int): StrBuf;
def sb_delch(sb: StrBuf, ch: Int): StrBuf;
def sb_insert(sb: StrBuf, at: Int, a: Any): StrBuf;
def sb_insch(sb: StrBuf, at: Int, ch: Int): StrBuf;
def sb_setch(sb: StrBuf, at: Int, ch: Int): StrBuf;
def sb_len(sb: StrBuf): Int;
