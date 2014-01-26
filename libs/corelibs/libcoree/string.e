/* Implementations for built-in functions. */

def Bool.tostr(): String = if (this) "true" else "false"

def String.toint(): Int = this.tointbase(10)
def String.tolong(): Long = this.tolongbase(10)
def String.endsWith(suffix: String): Bool = this.startsWith(suffix, -suffix.len())
