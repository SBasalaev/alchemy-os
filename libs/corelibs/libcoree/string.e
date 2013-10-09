/* Implementations for built-in functions. */

def Bool.tostr(): String = if (this) "true" else "false"

def String.toint(): Int = String.tointbase(10)
def String.tolong(): Long = String.tolongbase(10)
def String.endswith(suffix: String): Bool = this.startswith(suffix, -suffix.len())
