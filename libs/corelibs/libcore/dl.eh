/* dynamic loading support. */

type Library < Any;
type IStream;

def loadlibrary(libname: String): Library;
def buildlibrary(in: IStream): Library;
def Library.getfunc(sig: String): Function;
