/* Core library: I/O functions
 * (C) 2011-2012 Sergey Basalaev
 * Licensed under GPL v3 with linkage exception
 */

use "list.eh"
use "string.eh"
use "textio.eh"

def read(): Int = stdin().read()
def readarray(buf: [Byte], ofs: Int, len: Int): Int = stdin().readarray(buf, ofs, len)
def skip(num: Long): Long = stdin().skip(num)
def readline(): String = utfreader(stdin()).readline()

def write(b: Int) = stdout().write(b)
def writearray(buf: [Byte], ofs: Int, len: Int) = stdout().writearray(buf, ofs, len)
def print(a: Any) = stdout().print(a)
def println(a: Any) = stdout().println(a)
def flush() = stdout().flush()

def OStream.printf(fmt: String, args: [Any]) = this.print(fmt.format(args))
def printf(fmt: String, args: [Any]) = stdout().printf(fmt, args)

def flistfilter(path: String, glob: String): [String] {
  var files = flist(path)
  var list = new_list()
  for (var i=0, i < files.len, i += 1) {
    var file = files[i]
    if (matches_glob(file, glob)) list.add(file)
  }
  // small hack, TODO: typed lists
  var a: Array = list.toarray()
  a
}
