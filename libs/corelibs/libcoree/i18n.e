/* Core library: internationalization functions
 * (C) 2013-2014 Sergey Basalaev
 * Licensed under GPL v3 with linkage exception
 */

use "dict.eh"
use "dataio.eh"
use "textio.eh"
use "sys.eh"

// magic for format v1 ('L' 10 'N' 1)
const MAGIC_1 = ('L' << 24) | (10 << 16) | ('N' << 8) | 1

type TextDomain {
  strings: Dict
}

var defdomain: TextDomain
var locale: String

def setlocale(lc: String = null) {
  // environment variable has the greatest priority
  if (lc == null || lc.len() == 0) {
    lc = getenv("LANGUAGE")
  }
  // try to load from config
  if ((lc == null || lc.len() == 0) && exists("/cfg/locale")) {
    var r = utfreader(fread("/cfg/locale"))
    lc = r.readLine().trim()
    r.close()
  }
  // use system properties
  if (lc == null || lc.len() == 0) {
    lc = sysProperty("microedition.locale");
    if (lc != null) lc = lc.replace('-', '_')
  }
  if (lc == null || lc.len() == 0) {
    lc = sysProperty("user.language");
    if (lc != null) lc = lc.replace('-', '_')
  }
  // use C as last resort
  if (lc == null || lc.len() == 0) {
    lc = "C"
  }
  locale = lc
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
    try {
      var inp = fread(domainpath)
      var ok = inp.readInt() == MAGIC_1
      var count = inp.readUShort()
      for (var i=0, i < count && ok, i+=1) {
        var key = inp.readUTF()
        var val = inp.readUTF()
        if (key == null || val == null) {
          ok = false
        } else {
          domain.strings[key] = val
        }
      }
      inp.close()
    } catch { }
  }
  return domain
}

def settextdomain(domain: String) {
  defdomain = loadtextdomain(domain)
}

def TextDomain._(msg: String): String {
  if (this == null) {
    return msg
  }
  var tr = this.strings[msg].cast(String)
  if (tr != null) return tr
  return msg
}

def _(msg: String): String = defdomain._(msg)
