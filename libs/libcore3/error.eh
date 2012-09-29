const SUCCESS = 0
const ERR_NULL = 100
const ERR_IO = 101
const ERR_RANGE = 102
const ERR_ILLARG = 103
const ERR_ILLSTATE = 104
const ERR_SECURITY = 105
const ERR_SYSTEM = 199

def Error.code(): Int;
def Error.msg(): String;
def error(code: Int, msg: String);