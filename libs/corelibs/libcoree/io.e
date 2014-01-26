/* Core library: I/O functions
 * (C) 2011-2014 Sergey Basalaev
 * Licensed under GPL v3 with linkage exception
 */

use "list.eh"
use "textio.eh"

def read(): Int = stdin().read()
def readArray(buf: [Byte], ofs: Int, len: Int): Int = stdin().readArray(buf, ofs, len)
def skip(num: Long): Long = stdin().skip(num)
def readline(): String = utfreader(stdin()).readLine()

def write(b: Int) = stdout().write(b)
def writeArray(buf: [Byte], ofs: Int, len: Int) = stdout().writeArray(buf, ofs, len)
def print(a: Any) = stdout().print(a)
def println(a: Any) = stdout().println(a)
def flush() = stdout().flush()

def OStream.printf(fmt: String, args: [Any]) = this.print(fmt.format(args))
def printf(fmt: String, args: [Any]) = stdout().print(fmt.format(args))

def flistfilter(path: String, glob: String): [String] {
  var files = flist(path)
  var list = new List()
  for (var file in files) {
    if (matchesGlob(file, glob) || matchesGlob(file, glob + '/')) list.add(file)
  }
  var strings = new [String](list.len())
  list.copyInto(0, strings, 0, strings.len)
  return strings
}
