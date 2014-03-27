/* Input/output routines. */

const EOF = -1

type Connection < Any

def Connection.close()

type IStream < Connection

def IStream.read(): Int
def IStream.readArray(buf: [Byte], ofs: Int = 0, len: Int = -1): Int
def IStream.readFully(): [Byte]
def IStream.skip(num: Long): Long
def IStream.available(): Int
def IStream.reset()

def read(): Int
def readArray(buf: [Byte], ofs: Int = 0, len: Int = -1): Int
def skip(num: Long): Long
def readline(): String

type OStream < Connection

def OStream.write(b: Int)
def OStream.writeArray(buf: [Byte], ofs: Int = 0, len: Int = -1)
def OStream.print(str: String)
def OStream.println(str: String = "")
def OStream.flush()
def OStream.printf(fmt: String, args: [String])
def OStream.writeAll(input: IStream)

def write(b: Int)
def writeArray(buf: [Byte], ofs: Int = 0, len: Int = -1)
def print(str: String)
def println(str: String = "")
def flush()
def printf(fmt: String, args: [String])

type StreamConnection < Connection

def StreamConnection.openInput(): IStream
def StreamConnection.openOutput(): OStream

def stdin(): IStream
def stdout(): OStream
def stderr(): OStream

def setin(input: IStream)
def setout(out: OStream)
def seterr(err: OStream)

/* Path conversions */
def pathfile(path: String): String
def pathdir(path: String): String
def abspath(path: String): String
def relpath(path: String): String

def fcreate(path: String)
def fremove(path: String)
def fremoveTree(path: String)
def mkdir(path: String)
def mkdirTree(path: String)
def fcopy(source: String, dest: String)
def fmove(source: String, dest: String)
def exists(path: String): Bool
def isDir(path: String): Bool
def fread(path: String): IStream
def fwrite(path: String): OStream
def fappend(path: String): OStream
def flist(path: String): [String]
def flistfilter(path: String, glob: String): [String]
def fmodified(path: String): Long
def fsize(path: String): Long

def setRead(path: String, on: Bool)
def setWrite(path: String, on: Bool)
def setExec(path: String, on: Bool)
def canRead(path: String): Bool
def canWrite(path: String): Bool
def canExec(path: String): Bool

def getCwd(): String
def setCwd(dir: String)

def spaceTotal(root: String): Long
def spaceFree(root: String): Long
def spaceUsed(root: String): Long

def readUrl(url: String): IStream

def matchesGlob(path: String, glob: String): Bool
