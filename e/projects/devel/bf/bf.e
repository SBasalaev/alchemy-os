/* brainfuck interpreter
 * Version 1.0
 * (C) 2012, Sergey Basalaev
 * Licensed under GPL v3
 */

use "io"

def main(args: Array) {
  // reading program
  var file = to_str(args[0])
  var prog = new BArray(fsize(file))
  var in = fopen_r(file)
  freadarray(in, prog, 0, prog.len)
  fclose(in)
  // executing
  var array = new BArray(4000)
  var pos = 0 // position in array
  var ct = 0  // program counter
  while (ct < prog.len) {
    var instr = prog[ct]
    if (instr == '<') {
      pos = pos-1
    } else if (instr == '>') {
      pos = pos+1
    } else if (instr == '-') {
      array[pos] = array[pos]-1
    } else if (instr == '+') {
      array[pos] = array[pos]+1
    } else if (instr == '.') {
      write(array[pos])
    } else if (instr == ',') {
      array[pos] = read()
    } else if (instr == '[' && array[pos] == 0) {
      var br = 1
      while (br != 0) {
        ct = ct+1
        if (prog[ct] == '[') br = br+1
        else if (prog[ct] == ']') br = br-1
      }
    } else if (instr == ']' && array[pos] != 0) {
      var br = 1
      while (br != 0) {
        ct = ct-1
        if (prog[ct] == ']') br = br+1
        else if (prog[ct] == '[') br = br-1
      }
    }
    ct = ct+1
  }
  flush()
}
