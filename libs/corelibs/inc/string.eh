/* String functions. */

def Any.tostr(): String;
def Bool.tostr(): String;
def Char.tostr(): String;

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

def String.get(at: Int): Char;
def String.len(): Int;
def String.range(from: Int, to: Int): String;

def String.indexof(ch: Char, from: Int = 0): Int;
def String.lindexof(ch: Char): Int;
def String.find(sub: String, from: Int = 0): Int;
def String.ucase(): String;
def String.lcase(): String;
def String.concat(str: String): String;
def String.cmp(str: String): Int;
def String.trim(): String;
def String.split(ch: Char, skipEmpty: Bool = false): [String];
def String.format(args: [Any]): String;
def String.chars(): [Char];
def String.utfbytes(): [Byte];
def String.startswith(prefix: String, from: Int = 0): Bool;
def String.endswith(suffix: String): Bool;
def String.replace(oldch: Char, newch: Char): String;
def String.hash(): Int;

def ca2str(ca: [Char]): String;
def ba2utf(ba: [Byte]): String;

const chstr = `Char.tostr`;
const `String.ch` = `String.get`;
const `String.substr` = `String.range`;
