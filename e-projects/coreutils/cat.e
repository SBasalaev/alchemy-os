/* Alchemy coreutils
 * (C) 2011, Sergey Basalaev
 * Licensed under GPL v3
 */

use "io"
use "string"

def _cat(buf: BArray) {
  var len = readarray(buf, 0, 256)
  while (len > 0) {
    writearray(buf, 0, len)
    len = readarray(buf, 0, 256)
  }
  flush()
}

def main(args: Array) {
  var count = args.len
  var buf = new BArray(256)
  if (count == 0) {
    _cat(buf)
  } else while (count > 0) {
    count = count-1
    setin(fopen_r(to_str(args[0])))
    _cat(buf)
  }
}
