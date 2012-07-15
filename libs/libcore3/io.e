use "io.eh"

def read(): Int = stdin().read()
def readarray(buf: BArray, ofs: Int, len: Int): Int = stdin().readarray(buf, ofs, len)
def skip(num: Long): Long = stdin().skip(num)

def write(b: Int) = stdout().write(b)
def writearray(buf: BArray, ofs: Int, len: Int) = stdout().writearray(buf, ofs, len)
def print(a: Any) = stdout().print(a)
def println(a: Any) = stdout().println(a)
def flush() = stdout().flush()

def OStream.printf(fmt: String, args: Array) = this.print(fmt.format(args))
def printf(fmt: String, args: Array) = stdin().printf(fmt, args)
