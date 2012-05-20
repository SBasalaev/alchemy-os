/* Core library: I/O functions
 * (C) 2011-2012, Sergey Basalaev
 * Licensed under GPL v3 with linkage exception
 */

use "io.eh"
use "string.eh"

def read(): Int = fread(stdin());
def readarray(buf: BArray, ofs: Int, len: Int): Int = freadarray(stdin(), buf, ofs, len);
def skip(n: Long): Long = fskip(stdin(), n);

def write(b: Int) = fwrite(stdout(), b);
def writearray(buf: BArray, ofs: Int, len: Int) = fwritearray(stdout(), buf, ofs, len);
def print(a: Any) = fprint(stdout(), a);
def println(a: Any) = fprintln(stdout(), a);
def flush() = fflush(stdout());

def fprintf(out: OStream, fmt: String, args: Array) = fprint(out, sprintf(fmt, args));
def printf(fmt: String, args: Array) = fprintf(stdout(), fmt, args);
