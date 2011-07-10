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
      "mkdir: no arguments"
    );
    1;
  } else {
    var i = 0;
    while (i < len) {
      var dirname : String =
        to_str(aload(args,i));
      mkdir(to_file(dirname));
      i = i+1;
    }
    0;
  }
}

