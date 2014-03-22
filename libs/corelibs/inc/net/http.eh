/* HTTP connection API */

use "connection.eh"

type Http < StreamConnection

def Http.new(host: String): Http

const HEAD = "HEAD"
const GET = "GET"
const POST = "POST"

def Http.getReqMethod(): String
def Http.setReqMethod(method: String)

def Http.getReqProperty(key: String): String
def Http.setReqProperty(key: String, value: String)

const HTTP_OK = 200
const HTTP_CREATED = 201
const HTTP_ACCEPTED = 202
const HTTP_NOT_AUTHORITATIVE = 203
const HTTP_NO_CONTENT = 204
const HTTP_RESET = 205
const HTTP_PARTIAL = 206
const HTTP_MULT_CHOICE = 300
const HTTP_MOVED_PERM = 301
const HTTP_MOVED_TEMP = 302
const HTTP_SEE_OTHER = 303
const HTTP_NOT_MODIFIED = 304
const HTTP_USE_PROXY = 305
const HTTP_TEMP_REDIRECT = 307
const HTTP_BAD_REQUEST = 400
const HTTP_UNAUTHORIZED = 401
const HTTP_PAYMENT_REQUIRED = 402
const HTTP_FORBIDDEN = 403
const HTTP_NOT_FOUND = 404
const HTTP_BAD_METHOD = 405
const HTTP_NOT_ACCEPTABLE = 406
const HTTP_PROXY_AUTH = 407
const HTTP_CLIENT_TIMEOUT = 408
const HTTP_CONFLICT = 409
const HTTP_GONE = 410
const HTTP_LENGTH_REQUIRED = 411
const HTTP_PRECON_FAILED = 412
const HTTP_ENTITY_TOO_LARGE = 413
const HTTP_REQ_TOO_LONG = 414
const HTTP_UNSUPPORTED_TYPE = 415
const HTTP_UNSUPPORTED_RANGE = 416
const HTTP_EXPECT_FAILED = 417
const HTTP_INTERNAL_ERROR = 500
const HTTP_NOT_IMPLEMENTED = 501
const HTTP_BAD_GATEWAY = 502
const HTTP_UNAVAILABLE = 503
const HTTP_GATEWAY_TIMEOUT = 504
const HTTP_VERSION = 505

def Http.getType(): String
def Http.getEncoding(): String
def Http.getLength(): Long
def Http.getRespCode(): Int
def Http.getRespMsg(): String
def Http.getExpires(): Long
def Http.getDate(): Long
def Http.getModified(): Long

type Https < Http

def Https.new(host: String): Https

def Https.getPort(): Int

type SecInfo
def Https.getSecInfo(): SecInfo
