/* Alchemy coreutils
 * (C) 2011, Sergey Basalaev
 * Licensed under GPL v3
 */

use "io"
use "string"
use "sys"

def main(args: Array): Int {
  var len = args.len
  var ofs = 0
  var envread = false
  while (ofs < len & !envread) {
    var arg = to_str(args[ofs])
    var eqindex = strindex(arg,'=')
    if (eqindex < 0) {
      envread = true
    } else {
      setenv(
        substr(arg,0,eqindex),
        substr(arg,eqindex+1,strlen(arg))
      )
      ofs = ofs + 1
    }
  }
  if (ofs == len) {
    fprintln(stderr(), "env: no command")
    1
  } else {
    var argbuf = new Array(len-ofs-1)
    acopy(args,ofs+1,argbuf,0,len-ofs-1)
    exec_wait(to_str(args[ofs]), argbuf)
  }
}
