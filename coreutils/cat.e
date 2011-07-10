/* Alchemy coreutils
 * (C) 2011, Sergey Basalaev
 * Licensed under GPL v3
 */

use "array";
use "io";
use "string";

def main(args : Array) {
  var input : IStream =
    if (alen(args) == 0) {
      stdin();
    } else {
      var f = to_str(aload(args,0));
      fread(to_file(f));
    }
  var buf = new_ba(256);
  var len = readarray(input, buf, 0, 256);
  while (len > 0) {
    writearray(stdout(), buf, 0, len);
    len = readarray(input, buf, 0, 256);
  }
  flush(stdout());
}
