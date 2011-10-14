/* urlget
 * (C) 2011, Sergey Basalaev
 * Licensed under GPL v3
 */

use "net"
use "string"

def main(args: Array): Int {
  if (args.len == 0) {
    println(stderr(), "urlget: No URL specified")
    1
  } else {
    var in = netread(to_str(args[0]))
    var out = if (args.len == 1)
      stdout()
    else
      fwrite(to_file(to_str(args[1])))
    var buf = new BArray(4096)
    var len = readarray(in, buf, 0, 4096)
    while (len > 0) {
      writearray(out, buf, 0, len)
      len = readarray(in, buf, 0, 4096)
    }
    0
  }
}
