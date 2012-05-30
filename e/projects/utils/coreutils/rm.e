/* Alchemy coreutils
 * (C) 2011, Sergey Basalaev
 * Licensed under GPL v3
 */

use "io"
use "string"

def main(args: Array): Int {
  var len = args.len
  if (len == 0) {
    fprintln(stderr(), "rm: no arguments")
    1
  } else {
    for (var i=0, i < len, i = i+1) {
      fremove(to_str(args[i]))
    }
    0
  }
}

