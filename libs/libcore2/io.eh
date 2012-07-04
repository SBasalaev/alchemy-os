// I/O operations

type Stream < Any;

type IStream < Stream;
type OStream < Stream;

def stdin(): IStream;
def stdout(): OStream;
def stderr(): OStream;
def setin(s: IStream);
def setout(s: OStream);
def seterr(s: OStream);

def fclose(stream: Stream);

def fread(s: IStream): Int;
def freadarray(s: IStream, buf: BArray, ofs: Int, len: Int): Int;
def fskip(s: IStream, n: Long): Long;

def read(): Int;
def readarray(buf: BArray, ofs: Int, len: Int): Int;
def skip(n: Long): Long;

def fwrite(s: OStream, b: Int);
def fwritearray(s: OStream, buf: BArray, ofs: Int, len: Int);
def fprint(s: OStream, a: Any);
def fprintln(s: OStream, a: Any);
def fprintf(s: OStream, fmt: String, args: Array);
def fflush(s: OStream);

def write(b: Int);
def writearray(buf: BArray, ofs: Int, len: Int);
def print(a: Any);
def println(a: Any);
def printf(fmt: String, args: Array);
def flush();

def pathfile(path: String): String;
def pathdir(path: String): String;
def abspath(path: String): String;
def relpath(path: String): String;

def fcreate(f: String);
def fremove(f: String);
def mkdir(f: String);
def fcopy(src: String, dst: String);
def fmove(src: String, dst: String);
def exists(f: String): Bool;
def is_dir(f: String): Bool;
def fopen_r(f: String): IStream;
def fopen_w(f: String): OStream;
def fopen_a(f: String): OStream;
def flist(f: String): [String];
def fmodified(f: String): Long;
def fsize(f: String): Int;

def set_read(f: String, on: Bool);
def set_write(f: String, on: Bool);
def set_exec(f: String, on: Bool);
def can_read(f: String): Bool;
def can_write(f: String): Bool;
def can_exec(f: String): Bool;

def get_cwd(): String;
def set_cwd(dir: String);

def space_total(): Long;
def space_free(): Long;
def space_used(): Long;
