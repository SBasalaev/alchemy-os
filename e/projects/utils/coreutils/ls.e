/* Alchemy coreutils
 * (C) 2011, Sergey Basalaev
 * Licensed under GPL v3
 */

use "io"
use "string"

def _ls(f: String): Int {
  if (is_dir(f)) {
    var list = flist(f)
    for (var i=0, i < list.len, i = i+1) {
      println(list[i])
    }
    1
  } else if (exists(f)) {
    println(pathfile(f))
    1
  } else {
    fprintln(stderr(), "ls: file not found: "+f)
    0
  }
}

def main(args: Array): Int {
  if (args.len == 0) {
    _ls(get_cwd())
  } else {
    var result = 0
    for (var i=0, i<args.len && result == 0, i = i+1) {
      result = _ls(to_str(args[i]))
    }
    result
  }
}

