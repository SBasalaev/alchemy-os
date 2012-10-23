const SUCCESS = 0
const FAIL = 1
const ERR_SYSTEM = 100
const ERR_NULL = 101
const ERR_IO = 102
const ERR_RANGE = 103
const ERR_NEG_ALEN = 104
const ERR_ILL_ARG = 105
const ERR_ILL_STATE = 106
const ERR_SECURITY = 107
const ERR_TYPE_MIS = 108
const ERR_DIV_BY_0 = 109
const ERR_INTERRUPT = 110

def Error.code(): Int;
def Error.msg(): String;
def error(code: Int, msg: String);