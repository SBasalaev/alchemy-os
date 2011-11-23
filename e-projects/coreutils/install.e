/* Alchemy coreutils
 * (C) 2011, Sergey Basalaev
 * Licensed under GPL v3
 */

use "io"
use "string"

def main(args: Array): Int {
  var len = args.len
  if (len == 0) {
    fprintln(stderr(), "install: missing argument")
    1
  } else if (len == 1) {
    fprintln(stderr(), "install: missing destination")
    1
  } else {
    var destdir = to_file(to_str(args[len-1]))
    var i = 0
    while (i < len-1) {
      var src = to_file(to_str(args[i]))
      var path = new_sb()
      sb_append(path, destdir)
      sb_addch(path, '/')
      sb_append(path, fname(src))
      var dest = to_file(to_str(path))
      fcopy(src, dest)
      set_read(dest, can_read(src))
      set_write(dest, can_write(src))
      set_exec(dest, can_exec(src))
      i = i+1
    }
    0
  }
}
