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

def String.len();
def String.ch(at: Int): Int;
def String.indexof(ch: Int): Int;
def String.lindexof(ch: Int): Int;
def String.find(sub: String): Int;
def String.substr(from: Int, to: Int): String;
def String.ucase(): String;
def String.lcase(): String;
def String.concat(str: String): String;
def String.cmp(str: String): Int;
def String.trim(): String;
def String.split(ch: Int): [String];
def String.format(args: [Any]): String;
def String.chars(): CArray;
def String.utfbytes(): BArray;

def ca2str(ca: CArray): String;
def ba2utf(ba: BArray): String;
