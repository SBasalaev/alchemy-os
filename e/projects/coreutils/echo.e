/* Alchemy coreutils
 * (C) 2011, Sergey Basalaev
 * Licensed under GPL v3
 */

use "io"

def main(args: Array) {
  var len = args.len
  var i = 0
  while (i < len) {
    if (i != 0) print(" ")
    print(args[i])
    i = i+1
  }
  println("")
}
