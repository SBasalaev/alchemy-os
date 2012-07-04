// various system functions

def getenv(key: String): String;
def setenv(key: String, val: String);

def sleep(millis: Int);

def exec_wait(prog: String, args: [String]): Int;
def exec(prog: String, args: [String]);

def acopy(src: Array, sofs: Int, dest: Array, dofs: Int, len: Int);
def bacopy(src: BArray, sofs: Int, dest: BArray, dofs: Int, len: Int);
def cacopy(src: CArray, sofs: Int, dest: CArray, dofs: Int, len: Int);

def hash(a: Any): Int;

type IStream;
def readresource(path: String): IStream;

def clone(struct: Structure): Structure;
