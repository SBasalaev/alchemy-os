/* Alchemy coreutils
 * (C) 2011, Sergey Basalaev
 * Licensed under GPL v3
 */

use "io"

def main(args: Array) {
  for (var i=0, i < args.len, i = i+1) {
    if (i != 0) print(" ")
    print(args[i])
  }
  println("")
}
