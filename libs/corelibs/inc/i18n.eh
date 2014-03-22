def setlocale(lc: String)
def settextdomain(domain: String)

def _(msg: String): String

type TextDomain < Any

def loadtextdomain(domain: String): TextDomain
def TextDomain._(msg: String): String
