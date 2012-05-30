/* urlget 1.1
 * (C) 2011, Sergey Basalaev
 * Licensed under GPL v3
 */

use "io"
use "net"
use "string"

def main(args: Array): Int {
  if (args.len == 0) {
    fprintln(stderr(), "urlget: No URL specified")
    1
  } else {
    var url = to_str(args[0])
    var file = if (args.len == 1)
      substr(url, strlindex(url, '/')+1, strlen(url))
    else
      to_str(args[1])
    var in = netread(to_str(args[0]))
    var out = fopen_w(file)
    var buf = new BArray(4096)
    var len = freadarray(in, buf, 0, 4096)
    while (len > 0) {
      fwritearray(out, buf, 0, len)
      len = freadarray(in, buf, 0, 4096)
    }
    0
  }
}
