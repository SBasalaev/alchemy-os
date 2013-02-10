// builtin types and functions

def Any.tostr(): String;
def Bool.tostr(): String; 
def Function.curry(a: Any): Function;

def getstatic(name: String): Any;
def getstaticdef(name: String, dflt: Any): Any;
def setstatic(name: String, a: Any);

def chstr(ch: Char): String;
const `Char.tostr` = chstr;

def String.ch(at: Int): Char;
const `String.get` = `String.ch`;

def String.substr(from: Int, to: Int): String;
def String.len(): Int;
const `String.range` = `String.substr`;

def String.hash(): Int;

def acopy(src: Array, sofs: Int, dest: Array, dofs: Int, len: Int);
const bacopy = acopy;
const cacopy = acopy;

def Structure.clone(): Structure;
