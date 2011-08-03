/* Alchemy coreutils
 * (C) 2011, Sergey Basalaev
 * Licensed under GPL v3
 */

use "array";
use "io";

def main(args : Array) {
  var len = alen(args);
  // -1 unset
  // 0  don't change
  // 1  set
  var readflag = 0;
  var writeflag = 0;
  var execflag = 0;
  // processing options
  var i=0;
  while (i < len) {
    var arg = to_str(aload(args,i));
    if (arg=="-r") readflag = -1
    else if (arg=="+r") readflag = 1
    else if (arg=="-w") writeflag = -1
    else if (arg=="+w") writeflag = 1
    else if (arg=="-x") writeflag = -1
    else if (arg=="+x") writeflag = 1
    i = i+1;
  }
  // setting attributes
  i = 0;
  while (i < len) {
    var arg = to_str(aload(args,i));
    var first = strchr(arg,0);
    if (first != '-' && first != '+') {
      var file = to_file(arg);
      if (readflag==1) set_read(file,true)
      else if (readflag==-1) set_read(file,false);
      if (writeflag==1) set_write(file,true)
      else if (writeflag==-1) set_write(file,false);
      if (execflag==1) set_exec(file,true)
      else if (execflag==-1) set_exec(file,false);
    }
    i = i+1;
  }
}
