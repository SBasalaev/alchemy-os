/* Core library: String functions
 * Copyright (c) 2013 Sergey Basalaev
 * Licensed under GPL v3 with linkage exception
 */

use "string.eh"

def String.endswith(suffix: String): Bool = this.startswith(suffix, this.len()-suffix.len())

def Bool.tostr(): String = if (this) "true" else "false"
