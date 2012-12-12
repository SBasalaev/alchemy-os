// various system functions

def getenv(key: String): String;
def setenv(key: String, val: String);

def sleep(millis: Int);

def exec_wait(prog: String, args: [String]): Int;
def exec(prog: String, args: [String]);

def acopy(src: Array, sofs: Int, dest: Array, dofs: Int, len: Int);
const bacopy = acopy;
const cacopy = acopy;

def Structure.clone(): Structure;
