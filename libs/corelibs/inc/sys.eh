// various system functions

def systime(): Long;

def getenv(key: String): String;
def setenv(key: String, val: String);

def sleep(millis: Int);

def execWait(prog: String, args: [String]): Int;
def exec(prog: String, args: [String]);

def sysProperty(str: String): String;
def platformRequest(url: String): Bool;