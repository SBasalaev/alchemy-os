/* Example of work with structures.
 * Also demonstrates output using
 * concatenation and using printf.
 */

use "io"

/* Complex number */
type Complex { re: Double, im: Double }

/* Sum of two complex numbers */
def cadd(z1: Complex, z2: Complex): Complex
  = new Complex(
    re = z1.re + z2.re,
    im = z1.im + z2.im
  )

def main(args: Array) {
  var z1 = new Complex(re=1, im=2)
  var z2 = new Complex(re=3, im=4)
  var z3 = cadd(z1, z2)
  /* Printing using printf */
  printf("%0+%1i + %2+%3i", new Array { z1.re, z1.im, z2.re, z2.im })
  /* Printing using concatenation */
  println(" = " + z3.re + "+" + z3.im + "i")
}
