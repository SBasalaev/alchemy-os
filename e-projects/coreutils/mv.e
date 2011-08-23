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
      "mv: missing argument"
    );
    1;
  } else if (len == 1) {
    println(
      stderr(),
      "mv: missing destination"
    );
    1;
  } else {
    var src  = to_str(args[0]);
    var dest = to_str(args[1]);
    fmove(to_file(src), to_file(dest));
    0;
  }
}
