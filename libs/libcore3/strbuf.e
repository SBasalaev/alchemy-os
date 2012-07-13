/* Implementation of StrBuf methods not found in j2me.
 * Copyright (c) 2012 Sergey Basalaev
 * Licensed under GPLv3 with linkage exception
 */

use "strbuf.eh"

def StrBuf.replace(from: Int, to: Int, by: Any): StrBuf {
  this.delete(from, to).insert(from, by)
}
