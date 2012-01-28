/* Alchemy coreutils
 * (C) 2011, Sergey Basalaev
 * Licensed under GPL v3
 */

use "io"
use "string"

def _ls(f: String) {
  if (is_dir(f)) {
    var list = flist(f)
    for (var i=0, i < list.len, i = i+1) {
      println(list[i])
    }
  } else {
    println(pathfile(f))
  }
}

def main(args: Array) {
  if (args.len == 0) {
    _ls(get_cwd())
  } else {
    for (var i=0, i < args.len, i = i+1) {
      _ls(to_str(args[i]))
    }
  }
}

