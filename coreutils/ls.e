/* Alchemy coreutils
 * (C) 2011, Sergey Basalaev
 * Licensed under GPL v3
 */

use "array";
use "io";
use "string";

def main(args : Array) {
  var dir : File =
    if (alen(args) == 0) {
      get_cwd();
    } else {
      to_file(to_str(aload(args,0)));
    }
  var list = flist(dir);
  var len = alen(list);
  var i = 0;
  while (i < len) {
    println(stdout(),aload(list,i));
    i = i+1;
  }
}

