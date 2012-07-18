/* Input/Output */

const EOF = -1

type Stream < Any;

def Stream.close();

type IStream < Stream;
type OStream < Stream;

def stdin(): IStream;
def stdout(): OStream;
def stderr(): OStream;

def setin(in: IStream);
def setout(out: OStream);
def seterr(err: OStream);

def IStream.read(): Int;
def IStream.readarray(buf: BArray, ofs: Int, len: Int): Int;
def IStream.skip(num: Long): Long;

def read(): Int;
def readarray(buf: BArray, ofs: Int, len: Int): Int;
def skip(num: Long): Long;
def readline(): String;

def OStream.write(b: Int);
def OStream.writearray(buf: BArray, ofs: Int, len: Int);
def OStream.print(a: Any);
def OStream.println(a: Any);
def OStream.flush();
def OStream.printf(fmt: String, args: Array);

def write(b: Int);
def writearray(buf: BArray, ofs: Int, len: Int);
def print(a: Any);
def println(a: Any);
def flush();
def printf(fmt: String, args: Array);

def pathfile(path: String): String;
def pathdir(path: String): String;
def abspath(path: String): String;
def relpath(path: String): String;

def fcreate(file: String);
def fremove(file: String);
def mkdir(file: String);
def fcopy(source: String, dest: String);
def fmove(source: String, dest: String);
def exists(file: String): Bool;
def is_dir(file: String): Bool;
def fopen_r(file: String): IStream;
def fopen_w(file: String): OStream;
def fopen_a(file: String): OStream;
def flist(file: String): [String];
def fmodified(file: String): Long;
def fsize(file: String): Int;

def set_read(file: String, on: Bool);
def set_write(file: String, on: Bool);
def set_exec(file: String, on: Bool);
def can_read(file: String): Bool;
def can_write(file: String): Bool;
def can_exec(file: String): Bool;

def get_cwd(): String;
def set_cwd(dir: String);

def space_total(): Long;
def space_free(): Long;
def space_used(): Long;
