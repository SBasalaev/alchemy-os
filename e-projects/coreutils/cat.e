/* Alchemy coreutils
 * (C) 2011, Sergey Basalaev
 * Licensed under GPL v3
 */

use "io";
use "string";

def main(args : Array) {
  var input : IStream =
    if (args.len == 0) {
      stdin();
    } else {
      var f = to_str(args[0]);
      fread(to_file(f));
    }
  var buf = new BArray(256);
  var len = readarray(input, buf, 0, 256);
  while (len > 0) {
    writearray(stdout(), buf, 0, len);
    len = readarray(input, buf, 0, 256);
  }
  flush(stdout());
}
