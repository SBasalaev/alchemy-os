use "/inc/sys.eh"
use "/inc/io.eh"

def hash(a: Any): Int = 0

def readresource(path: String): IStream = readurl("res:"+path)

const clone = `Structure.clone`
