/* Wrapper for ec/el
 * Version 1.0
 * (C) 2012, Sergey Basalaev
 * Licensed under GPL v3
 */

use "io"
use "string"
use "sys"
use "vector"

def main(args: Array): Int {
  /* initializing */
  var sources = new_vector()
  var objects = new_vector()
  var ecflags = new_vector()
  var elflags = new_vector()
  var outname = "a.out"
  /* parsing arguments */
  var mode = 0
  // 0 - normal
  // 1 - waiting outname
  // 2 - help / version
  // 3 - error
  for (var i=0, i < args.len, i = i+1) {
    var arg = to_str(args[i])
    var len = strlen(arg)
    if (mode == 1) {
      outname = arg
      mode = 0
    } else if (len < 2 || strchr(arg, 0) != '-') {
      var ext = substr(arg, len-2, len)
      if (ext == ".e") {
        v_add(sources, arg)
        v_add(objects, substr(arg, 0, strlen(arg)-2) +".o")
      } else if (ext == ".o") {
        v_add(objects, arg)
      } else {
        fprintln(stderr(), "Unknown source: "+arg)
        mode = 2
      }
    } else {
      var opt = strchr(arg, 1)
      if (opt == 'h') {
        exec_wait("ec", new Array {"-h"})
        exec_wait("el", new Array {"-h"})
        mode = 2
      } else if (opt == 'v') {
        println("ex 1.0")
        exec_wait("ec", new Array {"-v"})
        exec_wait("el", new Array {"-v"})
        mode = 2
      } else if (opt == 'o') {
        mode = 1
      } else if (strindex("lLs", opt) >= 0) {
        v_add(elflags, arg)
      } else if (strindex("ItO", opt) >= 0) {
        v_add(ecflags, arg)
      } else {
        fprintln(stderr(), "Unknown option: "+arg)
        mode = 3
      }
    }   
  }
  if (mode == 1) {
    fprintln(stderr(), "-o requires name")
    mode = 3
  }
  /* if mode != 0 exit else process */
  if (mode != 0) {
    mode-2
  } else {
    var exitcode = 0
    /* prepare ec flags */
    var opts = new Array(v_size(ecflags) + 3)
    opts[1] = "-o"
    acopy(v_toarray(ecflags), 0, opts, 3, v_size(ecflags))
    /* compile sources */
    var count = v_size(sources)
    for (var i=0, i < count && exitcode == 0, i = i+1) {
      var srcname = to_str(v_get(sources, i))
      opts[0] = srcname
      opts[2] = substr(srcname, 0, strlen(srcname)-2) + ".o"
      exitcode = exec_wait("ec", opts)
    }
    /* prepare el flags */
    if (exitcode == 0) {
      opts = new Array(v_size(objects) + v_size(elflags) + 2)
      acopy(v_toarray(objects), 0, opts, 0, v_size(objects))
      acopy(v_toarray(elflags), 0, opts, v_size(objects), v_size(elflags))
      opts[opts.len-2] = "-o"
      opts[opts.len-1] = outname
      exec_wait("el", opts)
    } else {
      exitcode
    }
  }
}
