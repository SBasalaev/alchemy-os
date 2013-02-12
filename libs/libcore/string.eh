/* String operations. */

def Any.tostr(): String;

def Int.tobin(): String;
def Int.tooct(): String;
def Int.tohex(): String;
def Int.tobase(base: Int): String;
def Long.tobase(base: Int): String;

def String.toint(): Int;
def String.tointbase(base: Int): Int;
def String.tolong(): Long;
def String.tolongbase(base: Int): Long;
def String.tofloat(): Float;
def String.todouble(): Double;

def String.len(): Int;
def String.indexof(ch: Char): Int;
def String.lindexof(ch: Char): Int;
def String.find(sub: String): Int;
def String.ucase(): String;
def String.lcase(): String;
def String.concat(str: String): String;
def String.cmp(str: String): Int;
def String.trim(): String;
def String.split(ch: Char): [String];
def String.format(args: [Any]): String;
def String.chars(): [Char];
def String.utfbytes(): [Byte];
def String.startswith(prefix: String, ofs: Int = 0): Bool;
def String.endswith(suffix: String): Bool;
def String.hash(): Int;

def ca2str(ca: [Char]): String;
def ba2utf(ba: [Byte]): String;
