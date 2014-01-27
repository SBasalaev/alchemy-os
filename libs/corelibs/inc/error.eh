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
const ERR_TYPE_MISMATCH = 108
const ERR_DIV_BY_ZERO = 109
const ERR_INTERRUPT = 110
const ERR_MEDIA = 111

def Error.code(): Int;
def Error.msg(): String;
def Error.traceLen(): Int;
def Error.traceName(index: Int): String;
def Error.traceDbg(index: Int): String;

def `throw`(code: Int = FAIL, msg: String = null);
