/* dynamic loading support. */

use "io"

type Library < Any

def loadlibrary(libname: String): Library
def buildlibrary(in: IStream): Library
def Library.getFunction(sig: String): Function
