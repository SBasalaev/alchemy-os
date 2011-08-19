/* Alchemy coreutils
 * (C) 2011, Sergey Basalaev
 * Licensed under GPL v3
 */

use "io";
use "string";

def main(args : Array) : Int {
  var len = args.len;
  if (len == 0) {
    println(
      stderr(),
      "rm: no arguments"
    );
    1;
  } else {
    var i = 0;
    while (i < len) {
      var dirname = to_str(args[i]);
      fremove(to_file(dirname));
      i = i+1;
    }
    0;
  }
}

