/* Core library: I/O functions
 * (C) 2011-2012 Sergey Basalaev
 * Licensed under GPL v3 with linkage exception
 */

use "string.eh"
use "textio.eh"

def read(): Int = stdin().read()
def readarray(buf: BArray, ofs: Int, len: Int): Int = stdin().readarray(buf, ofs, len)
def skip(num: Long): Long = stdin().skip(num)
def readline(): String = utfreader(stdin()).readline()

def write(b: Int) = stdout().write(b)
def writearray(buf: BArray, ofs: Int, len: Int) = stdout().writearray(buf, ofs, len)
def print(a: Any) = stdout().print(a)
def println(a: Any) = stdout().println(a)
def flush() = stdout().flush()

def OStream.printf(fmt: String, args: Array) = this.print(fmt.format(args))
def printf(fmt: String, args: Array) = stdout().printf(fmt, args)
