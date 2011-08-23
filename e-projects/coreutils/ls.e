/* Alchemy coreutils
 * (C) 2011, Sergey Basalaev
 * Licensed under GPL v3
 */

use "io";
use "string";

def main(args : Array) {
  var dir : File =
    if (args.len == 0) {
      get_cwd();
    } else {
      to_file(to_str(args[0]));
    }
  var list = flist(dir);
  var len = list.len;
  var i = 0;
  while (i < len) {
    println(stdout(),list[i]);
    i = i+1;
  }
}

