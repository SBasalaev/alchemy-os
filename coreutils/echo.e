/* Alchemy coreutils
 * (C) 2011, Sergey Basalaev
 * Licensed under GPL v3
 */

use "array";
use "io";

def main(args : Array) {
  var len = alen(args);
  var i = 0;
  while (i < len) {
    if (i != 0) print(stdout(), " ");
    print(stdout(), aload(args,i));
    i = i+1;
  }
  println(stdout(), "");
}
