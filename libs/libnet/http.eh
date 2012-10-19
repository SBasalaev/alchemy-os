/* HTTP connection API */

use "connection.eh"

type Http < StreamConnection;

def new_http(host: String): Http;

const HEAD = "HEAD"
const GET = "GET"
const POST = "POST"

def Http.get_req_method(): String;
def Http.set_req_method(method: String);

def Http.get_req_property(key: String): String;
def Http.set_req_property(key: String, value: String);

const HTTP_OK                = 200
const HTTP_CREATED           = 201
const HTTP_ACCEPTED          = 202
const HTTP_NOT_AUTHORITATIVE = 203
const HTTP_NO_CONTENT        = 204
const HTTP_RESET             = 205
const HTTP_PARTIAL           = 206
const HTTP_MULT_CHOICE       = 300
const HTTP_MOVED_PERM        = 301
const HTTP_MOVED_TEMP        = 302
const HTTP_SEE_OTHER         = 303
const HTTP_NOT_MODIFIED      = 304
const HTTP_USE_PROXY         = 305
const HTTP_TEMP_REDIRECT     = 307
const HTTP_BAD_REQUEST       = 400
const HTTP_UNAUTHORIZED      = 401
const HTTP_PAYMENT_REQUIRED  = 402
const HTTP_FORBIDDEN         = 403
const HTTP_NOT_FOUND         = 404
const HTTP_BAD_METHOD        = 405
const HTTP_NOT_ACCEPTABLE    = 406
const HTTP_PROXY_AUTH        = 407
const HTTP_CLIENT_TIMEOUT    = 408
const HTTP_CONFLICT          = 409
const HTTP_GONE              = 410
const HTTP_LENGTH_REQUIRED   = 411
const HTTP_PRECON_FAILED     = 412
const HTTP_ENTITY_TOO_LARGE  = 413
const HTTP_REQ_TOO_LONG      = 414
const HTTP_UNSUPPORTED_TYPE  = 415
const HTTP_UNSUPPORTED_RANGE = 416
const HTTP_EXPECT_FAILED     = 417
const HTTP_INTERNAL_ERROR    = 500
const HTTP_NOT_IMPLEMENTED   = 501
const HTTP_BAD_GATEWAY       = 502
const HTTP_UNAVAILABLE       = 503
const HTTP_GATEWAY_TIMEOUT   = 504
const HTTP_VERSION           = 505

def Http.get_type(): String;
def Http.get_encoding(): String;
def Http.get_length(): Long;
def Http.get_resp_code(): Int;
def Http.get_resp_msg(): String;
def Http.get_expires(): Long;
def Http.get_date(): Long;
def Http.get_modified(): Long;

type Https < Http;

def new_https(host: String): Https;

def Https.get_port(): Int;

type SecInfo;
def Https.get_secinfo(): SecInfo;
