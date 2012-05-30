/* Time program
 * Version 1.0.2
 * (C) 2011-2012, Sergey Basalaev
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
  if (cmd != null) exec_wait(cmd, params)
  ms = systime()-ms
  println("" + (ms/1000) + "." + (ms%1000) + " s")
}
