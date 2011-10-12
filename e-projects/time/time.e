/* Time program
 * (C) 2011, Sergey Basalaev
 * Licensed under GPL v3
 */

use "io"
use "string"
use "sys"
use "time"

def main(args: Array) {
  var cmd: String;
  var params: Array;
  if (args.len > 0) {
    cmd = to_str(args[0])
    params = new Array(args.len-1)
    acopy(args, 1, params, 0, args.len-1)
  }
  var ms = systime()
  if (cmd != null) exec(cmd, params);
  ms = systime()-ms
  var out = stdout()
  print(out, ms/1000)
  print(out, ".")
  print(out, ms%1000)
  println(out, " s")
}
