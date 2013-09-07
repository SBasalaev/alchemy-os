use "dict.eh"
use "dataio.eh"
use "textio.eh"
use "string.eh"
use "sys.eh"

// magic for format v1 ('L' 10 'N' 1)
const MAGIC_1 = ('L' << 24) | (10 << 16) | ('N' << 8) | 1

type TextDomain {
  strings: Dict
}

var defdomain: TextDomain;
var locale: String;

def setlocale(lc: String) {
  // environment variable has the greatest priority
  if (lc == null || lc.len() == 0) {
    lc = getenv("LANGUAGE")
  }
  // try to load from config
  if ((lc == null || lc.len() == 0) && exists("/cfg/locale")) {
    var r = utfreader(fopen_r("/cfg/locale"))
    lc = r.readline().trim()
    r.close()
  }
  // use system property
  if (lc == null || lc.len() == 0) {
    lc = sys_property("microedition.locale");
    if (lc != null) lc = lc.replace('-', '_')
  }
  // use C as last resort
  if (lc == null || lc.len() == 0) {
    lc = "C"
  }
  locale = lc;
}

def loadtextdomain(name: String): TextDomain {
  if (locale == null || locale.len() == 0) {
    setlocale(null)
  }
  var domain = new TextDomain(new Dict())
  var domainpath = "/res/locale/" + locale + '/' + name + ".lc"
  if (!exists(domainpath) && locale.indexof('_') > 0) {
    locale = locale[: locale.indexof('_')]
    domainpath = "/res/locale/" + locale + '/' + name + ".lc"
  }
  if (exists(domainpath)) {
    var in = fopen_r(domainpath)
    var ok = in.readint() == MAGIC_1
    var count = in.readushort()
    if (count != null)
    for (var i=0, i < count && ok, i+=1) {
      var key = in.readutf()
      var val = in.readutf()
      if (key == null || val == null) {
        ok = false
      } else {
        domain.strings[key] = val
      }
    }
    in.close()
  }
  domain
}

def settextdomain(domain: String) {
  defdomain = loadtextdomain(domain)
}

def TextDomain._(msg: String): String {
  if (this == null) {
    msg
  } else {
    var tr = this.strings[msg]
    if (tr != null) tr
    else msg
  }
}

def _(msg: String): String = defdomain._(msg)
