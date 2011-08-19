/* Alchemy coreutils
 * (C) 2011, Sergey Basalaev
 * Licensed under GPL v3
 */

use "io";
use "string";

def main(args : Array) : Int {
  var len = args.len;
  if (len == 0) {
    println(stderr(),
      "install: missing argument");
    1;
  } else if (len == 1) {
    println(stderr(),
      "install: missing destination");
    1;
  } else {
    var destdir : String = strcat(
      fpath(to_file(to_str(args[len-1]))),
      "/"
    );
    var i = 0;
    while (i < len-1) {
      var src = to_file(to_str(args[i]));
      var dest = to_file(strcat(destdir, fname(src)));
      fcopy(src, dest);
      set_read(dest, can_read(src));
      set_write(dest, can_write(src));
      set_exec(dest, can_exec(src));
      i = i+1;
    }
    0;
  }
}
