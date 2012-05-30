/* Alchemy coreutils
 * (C) 2011, Sergey Basalaev
 * Licensed under GPL v3
 */

use "io"
use "string"

def main(args: Array): Int {
  var len = args.len
  if (len == 0) {
    fprintln(stderr(), "mv: missing argument")
    1
  } else if (len == 1) {
    fprintln(stderr(), "mv: missing destination")
    1
  } else {
    var dest = to_str(args[len-1])
    if (is_dir(dest)) {
      for (var i=0, i < len-1, i = i+1) {
        var srcfile = to_str(args[i])
        var path = new_sb()
        sb_append(path, dest)
        sb_addch(path, '/')
        sb_append(path, pathfile(srcfile))
        var destfile = to_str(path)
        fmove(srcfile, destfile)
      }
      0
    } else if (len == 2) {
      var src = to_str(args[0])
      fmove(src, dest)
      0
    } else {
      fprintln(stderr(), "mv: many arguments but target is not directory")
      1
    }
  }
}
