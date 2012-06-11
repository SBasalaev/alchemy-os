/* Arh utility
 * Version 1.0.3
 * (C) 2011-2012, Sergey Basalaev
 * Licensed under GPL v3
 */

use "dataio.eh"

const VERSION = "arh 1.0.3"
const HELP = "Usage:\narh c archive files...\narh t archive\narh x archive"

//attribute flags
const A_DIR = 16
const A_READ = 4
const A_WRITE = 2
const A_EXEC = 1

def arhpath(path: String): String
  = relpath("./"+abspath("/"+path))

def arhlist(in: IStream) {
  var f = freadutf(in)
  while (f != null) {
    print(f)
    fskip(in, 8)
    var attrs = freadubyte(in)
    if ((attrs & 16) != 0) {
      println("/")
    } else {
      println("")
      fskip(in, freadint(in))
    }
    f = freadutf(in)
  }
}

def unarh(in: IStream) {
  var f = freadutf(in)
  while (f != null) {
    f = arhpath(f)
    fskip(in,8)
    var attrs = freadubyte(in)
    if ((attrs & A_DIR) != 0) {
      if (!exists(f)) mkdir(f)
    } else {
      var out = fopen_w(f)
      var len = freadint(in)
      if (len > 0) {
        var buf = new BArray(4096)
        while (len > 4096) {
          freadarray(in, buf, 0, 4096)
          fwritearray(out, buf, 0, 4096)
          len = len - 4096
        }
        freadarray(in, buf, 0, len)
        fwritearray(out, buf, 0, len)
      }
      fflush(out)
      fclose(out)
    }
    set_read(f, (attrs & A_READ) != 0)
    set_write(f, (attrs & A_WRITE) != 0)
    set_exec(f, (attrs & A_EXEC) != 0)
    f = freadutf(in)
  }
}

def arhwrite(out: OStream, f: String) {
  fwriteutf(out, arhpath(f))
  fwritelong(out, fmodified(f))
  var attrs = 0
  if (can_read(f)) attrs = attrs | A_READ
  if (can_write(f)) attrs = attrs | A_WRITE
  if (can_exec(f)) attrs = attrs | A_EXEC
  if (is_dir(f)) {
    fwritebyte(out, attrs | A_DIR)
    var subs = flist(f)
    for (var i=0, i<subs.len, i=i+1) {
      arhwrite(out, f+"/"+subs[i])
    }
  } else {
    fwritebyte(out, attrs)
    var len = fsize(f)
    fwriteint(out, len)
    if (len > 0) {
      var filein = fopen_r(f)
      var buf = new BArray(4096)
      var l = freadarray(filein, buf, 0, 4096)
      while (l > 0) {
        fwritearray(out, buf, 0, l)
        l = freadarray(filein, buf, 0, 4096)
      }
      fclose(filein)
    }
  }
}

def main(args: Array): Int {
  if (args.len == 0) {
    fprintln(stderr(), "arh: no command")
    1
  } else {
    var cmd = args[0]
    if (cmd == "v" || cmd == "-v") {
      println(VERSION)
      0
    } else if (cmd == "h" || cmd == "-h") {
      println(HELP)
      0
    } else if (args.len < 2) {
      fprintln(stderr(), "arh: no archive")
      1
    } else if (cmd == "t" || cmd == "-t") {
      arhlist(fopen_r(to_str(args[1])))
      0
    } else if (cmd == "x" || cmd == "-x") {
      unarh(fopen_r(to_str(args[1])))
      0
    } else if (cmd == "c" || cmd == "-c") {
      var out = fopen_w(to_str(args[1]))
      for (var i=2, i<args.len, i=i+1) {
        arhwrite(out, to_str(args[i]))
      }
      fflush(out)
      fclose(out)
      0
    } else {
      fprint(stderr(), "arh: unknown command: ")
      fprintln(stderr(), cmd)
      1
    }
  }
}
