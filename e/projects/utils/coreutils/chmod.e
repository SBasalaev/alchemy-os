/* Alchemy coreutils
 * (C) 2011, Sergey Basalaev
 * Licensed under GPL v3
 */

use "io"
use "string"

def main(args: Array) {
  var len = args.len
  //flags:
  // -1 unset
  // 0  don't change
  // 1  set
  var readflag = 0
  var writeflag = 0
  var execflag = 0
  // processing options
  for (var i=0, i < len, i = i+1) {
    var arg = to_str(args[i])
    if (arg=="-r") readflag = -1
    else if (arg=="+r") readflag = 1
    else if (arg=="-w") writeflag = -1
    else if (arg=="+w") writeflag = 1
    else if (arg=="-x") execflag = -1
    else if (arg=="+x") execflag = 1
  }
  // setting attributes
  for (var i=0, i < len, i = i+1) {
    var file = to_str(args[i])
    var first = strchr(file,0)
    if (first != '-' && first != '+') {
      if (readflag==1) set_read(file,true)
      else if (readflag==-1) set_read(file,false)
      if (writeflag==1) set_write(file,true)
      else if (writeflag==-1) set_write(file,false)
      if (execflag==1) set_exec(file,true)
      else if (execflag==-1) set_exec(file,false)
    }
  }
}
