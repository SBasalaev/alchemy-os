/* Input/Output */

use "connection.eh"

const EOF = -1

type IStream < Any;
type OStream < Any;

def stdin(): IStream;
def stdout(): OStream;
def stderr(): OStream;

def setin(in: IStream);
def setout(out: OStream);
def seterr(err: OStream);

def IStream.read(): Int;
def IStream.readarray(buf: BArray, ofs: Int, len: Int): Int;
def IStream.skip(num: Long): Long;
def IStream.close();

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
def OStream.close();

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

def fcreate(path: String);
def fremove(path: String);
def mkdir(path: String);
def fcopy(source: String, dest: String);
def fmove(source: String, dest: String);
def exists(path: String): Bool;
def is_dir(path: String): Bool;
def fopen_r(path: String): IStream;
def fopen_w(path: String): OStream;
def fopen_a(path: String): OStream;
def flist(path: String): [String];
def flistfilter(path: String, glob: String): [String];
def fmodified(path: String): Long;
def fsize(path: String): Long;

def set_read(path: String, on: Bool);
def set_write(path: String, on: Bool);
def set_exec(path: String, on: Bool);
def can_read(path: String): Bool;
def can_write(path: String): Bool;
def can_exec(path: String): Bool;

def get_cwd(): String;
def set_cwd(dir: String);

def space_total(root: String): Long;
def space_free(root: String): Long;
def space_used(root: String): Long;

def readurl(url: String): IStream;

def matches_glob(path: String, glob: String): Bool;

def istream_from_ba(buf: BArray): IStream;

type BArrayOstream < OStream;

def new_baostream(): BArrayOStream;
def BArrayOStream.len(): Int;
def BArrayOStream.tobarray(): BArray;
def BArrayOStream.reset();

def new_pipe(): StreamConnection;