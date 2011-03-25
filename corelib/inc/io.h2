// IO operations

use "array";

type File;
type IStream;
type OStream;

def close(stream: Any);

def read(s: IStream): Int;
def readarray(s: IStream, buf: BArray, ofs: Int, len: Int): Int;
def available(s: IStream);
def skip(s: IStream, n: Long);

def write(s: OStream, b: Int);
def writearray(s: OStream, buf: BArray, ofs: Int, len: Int);
def print(s: OStream, a: Any);
def println(s: OStream, a: Any);
def flush(s: OStream);

def to_file(path: String): File;
def fname(f: File): String;
def fpath(f: File): String;
def fparent(f: File): File;
def relpath(f: File): String;

def fcreate(f: File);
def fremove(f: File);
def mkdir(f: File);
def fcopy(src: File, dst: File);
def fmove(src: File, dst: File);
def exists(f: File): Bool;
def is_dir(f: File): Bool;
def fread(f: File): IStream;
def fwrite(f: File): OStream;
def fappend(f: File): OStream;
def flist(f: File): AArray;
def fmodified(f: File): Long;
def fsize(f: File): Int;

def set_read(f: File, on: Bool);
def set_write(f: File, on: Bool);
def set_exec(f: File, on: Bool);
def can_read(f: File): Bool;
def can_write(f: File): Bool;
def can_exec(f: File): Bool;

def get_cwd(): File;
def set_cwd(dir: File);

def space_total(): Long;
def space_free(): Long;
def space_used(): Long;

def stdin(): IStream;
def stdout(): OStream;
def stderr(): OStream;
def setin(s: IStream);
def setout(s: OStream);
def seterr(s: OStream);

