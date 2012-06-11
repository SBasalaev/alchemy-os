/* Simple program that asks your
 * name and then greets you.
 */

use "textio"

def main(args: Array) {
  println("What is your name?")
  var name = readline()
  println("Hello, "+name+"!")
}
