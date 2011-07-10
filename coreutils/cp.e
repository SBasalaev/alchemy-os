/* Alchemy coreutils
 * (C) 2011, Sergey Basalaev
 * Licensed under GPL v3
 */

use "array";
use "io";
use "string";

def main(args : Array) : Int {
  var len = alen(args);
  if (len == 0) {
    println(
      stderr(),
      "cp: missing argument"
    );
    1;
  } else if (len == 1) {
    println(
      stderr(),
      "cp: missing destination"
    );
    1;
  } else {
    var src  = to_str(aload(args,0));
    var dest = to_str(aload(args,1));
    fcopy(to_file(src), to_file(dest));
    0;
  }
}
