// various system functions

def getenv(key: String): String;
def setenv(key: String, val: String);

def sleep(millis: Int);

def exec_wait(prog: String, args: [String]): Int;
def exec(prog: String, args: [String]);

def sys_property(str: String): String;

def platformRequest(url: String): Bool;