/* quine - program that prints itself */
use "io"
def main(args: Array) {
  var a = new Array(18)
  a[0] = "/* quine - program that prints itself */"
  a[1] = "use "
  a[2] = "io"
  a[3] = "def main(args: Array) {"
  a[4] = "  var a = new Array(18)"
  a[5] = "  a["
  a[6] = "] = "
  a[7] = "  println(a[0]); print(a[1])"
  a[8] = "  write(34); print(a[2])"
  a[9] = "  write(34); write(10)"
  a[10] = "  println(a[3]); println(a[4])"
  a[11] = "  for (var i=0, i<18, i=i+1) {"
  a[12] = "    print(a[5]); print(i); print(a[6])"
  a[13] = "    write(34); print(a[i])"
  a[14] = "    write(34); write(10)"
  a[15] = "  }"
  a[16] = "  for (var i=7, i<18, i=i+1) println(a[i])"
  a[17] = "}"
  println(a[0]); print(a[1])
  write(34); print(a[2])
  write(34); write(10)
  println(a[3]); println(a[4])
  for (var i=0, i<18, i=i+1) {
    print(a[5]); print(i); print(a[6])
    write(34); print(a[i])
    write(34); write(10)
  }
  for (var i=7, i<18, i=i+1) println(a[i])
}
