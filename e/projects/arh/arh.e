/* Arh utility
 * (C) 2011, Sergey Basalaev
 * Licensed under GPL v3
 */

use "dataio"
use "string"

def arhpath(path: String): String
  = relpath(to_file(strcat("./", path)))


def arhlist(in: IStream) {
  while (available(in) > 0) {
    print(stdout(), readutf(in))
    skip(in, 8)
    var attrs = readubyte(in)
    if ((attrs & 16) != 0) {
      println(stdout(), "/")
    } else {
      println(stdout(), "")
      skip(in, readint(in))
    }
  }
}

def unarh(in: IStream) {
  var A_DIR = 16
  var A_READ = 4
  var A_WRITE = 2
  var A_EXEC = 1
  while (available(in) > 0) {
    var f = to_file(arhpath(readutf(in)))
    skip(in,8)
    var attrs = readubyte(in)
    if ((attrs & A_DIR) != 0) {
      mkdir(f)
    } else {
      var out = fwrite(f)
      var len = readint(in)
      if (len > 0) {
        var buf = new BArray(4096)
        while (len > 4096) {
          readarray(in, buf, 0, 4096)
          writearray(out, buf, 0, 4096)
          len = len - 4096
        }
        readarray(in, buf, 0, len)
        writearray(out, buf, 0, len)
      }
      flush(out)
      close(out)
    }
    set_read(f, (attrs & A_READ) != 0)
    set_write(f, (attrs & A_WRITE) != 0)
    set_exec(f, (attrs & A_EXEC) != 0)
  }
}

def arhwrite(out: OStream, fname: String) {
  var A_DIR = 16
  var A_READ = 4
  var A_WRITE = 2
  var A_EXEC = 1
  var f = to_file(fname)
  writeutf(out, arhpath(fname))
  writelong(out, fmodified(f))
  var attrs = 0
  if (can_read(f)) attrs = attrs | A_READ
  if (can_write(f)) attrs = attrs | A_WRITE
  if (can_exec(f)) attrs = attrs | A_EXEC
  if (is_dir(f)) {
    writebyte(out, attrs | A_DIR)
    var subs = flist(f)
    var i = 0
    while (i < subs.len) {
      var sb = new_sb()
      sb_append(sb, fname)
      sb_append(sb, "/")
      sb_append(sb, subs[i])
      arhwrite(out, to_str(sb))
      i = i + 1
    }
  } else {
    writebyte(out, attrs)
    var len = fsize(f)
    writeint(out, len)
    if (len > 0) {
      var fin = fread(f)
      var buf = new BArray(4096)
      var l = readarray(fin, buf, 0, 4096)
      while (l > 0) {
        writearray(out, buf, 0, l)
        l = readarray(fin, buf, 0, 4096)
      }
      close(fin)
    }
  }
}

def main(args: Array): Int {
  if (args.len == 0) {
    println(stderr(), "arh: no command")
    1
  } else {
    var cmd = args[0]
    if (cmd == "v" || cmd == "-v") {
      println(stdout(), "arh 1.0")
      0
    } else if (cmd == "h" || cmd == "-h") {
      println(stdout(), "Usage:\narh c file files...\narh t file\narh x file")
      0
    } else if (args.len < 2) {
      println(stderr(), "arh: no archive")
      1
    } else if (cmd == "t") {
      arhlist(fread(to_file(to_str(args[1]))))
      0
    } else if (cmd == "x") {
      unarh(fread(to_file(to_str(args[1]))))
      0
    } else if (cmd == "c") {
      var out = fwrite(to_file(to_str(args[1])))
      var i = 2
      while (i < args.len) {
        arhwrite(out, to_str(args[i]))
        i = i + 1
      }
      flush(out)
      close(out)
      0
    } else {
      print(stderr(), "arh: unknown command: ")
      println(stderr(), cmd)
      1
    }
  }
}
