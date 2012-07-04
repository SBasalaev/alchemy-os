//dynamic loading

type Library < Any;

def loadlibrary(libname: String): Library;
def loadfunc(lib: Library, sig: String) : Function;
