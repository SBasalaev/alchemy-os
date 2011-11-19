// I/O operations

type File;
type IStream;
type OStream;

def stdin(): IStream;
def stdout(): OStream;
def stderr(): OStream;
def setin(s: IStream);
def setout(s: OStream);
def seterr(s: OStream);

def fclose(stream: Any);

def fread(s: IStream): Int;
def freadarray(s: IStream, buf: BArray, ofs: Int, len: Int): Int;
def fskip(s: IStream, n: Long): Long;

def read(): Int = fread(stdin());
def readarray(buf: BArray, ofs: Int, len: Int): Int = freadarray(stdin(), buf, ofs, len);
def skip(n: Long) = fskip(stdin(), n);

def fwrite(s: OStream, b: Int);
def fwritearray(s: OStream, buf: BArray, ofs: Int, len: Int);
def fprint(s: OStream, a: Any);
def fprintln(s: OStream, a: Any);
def fflush(s: OStream);

def write(s: OStream, b: Int) = fwrite(stdout(), b);
def writearray(s: OStream, buf: BArray, ofs: Int, len: Int) = fwritearray(stdout(), buf, ofs, len);
def print(s: OStream, a: Any) = fprint(stdout(), a);
def println(s: OStream, a: Any) = fprintln(stdout(), a);
def flush() = fflush(stdout());

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
def fopen_r(f: File): IStream;
def fopen_w(f: File): OStream;
def fopen_a(f: File): OStream;
def flist(f: File): Array;
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
