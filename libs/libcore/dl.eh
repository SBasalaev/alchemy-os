/* dynamic loading support. */

type Library < Any;

def loadlibrary(libname: String): Library;
def Library.getfunc(sig: String): Function;
