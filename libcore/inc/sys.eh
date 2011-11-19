// various system functions

use "io";

def getenv(key: String): String;
def setenv(key: String, val: String);

def exec_wait(prog: String, args: Array): Int;
def exec(prog: String, args: Array);

def acopy(src: Array, sofs: Int, dest: Array, dofs: Int, len: Int);
def bacopy(src: BArray, sofs: Int, dest: BArray, dofs: Int, len: Int);
def cacopy(src: CArray, sofs: Int, dest: CArray, dofs: Int, len: Int);

def hash(a: Any): Int;

def readresource(path: String): IStream;

def clone(struct: Any): Any;