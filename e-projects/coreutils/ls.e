/* Alchemy coreutils
 * (C) 2011, Sergey Basalaev
 * Licensed under GPL v3
 */

use "io"
use "string"

def _ls(f: String) {
  if (is_dir(f)) {
    var list = flist(f)
    var i = 0
    while (i < list.len) {
      println(list[i])
      i = i + 1
    }
  } else {
    println(pathfile(f))
  }
}

def main(args: Array) {
  if (args.len == 0) {
    _ls(get_cwd())
  } else {
    var i = 0
    while (i < args.len) {
      _ls(to_str(args[i]))
      i = i+1
    }
  }
}

