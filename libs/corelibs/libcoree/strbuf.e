/* Core library: implementation of StrBuf functions not found in j2me
 * (C) 2011-2014 Sergey Basalaev
 * Licensed under GPL v3 with linkage exception
 */

use "strbuf.eh"

def StrBuf.replace(from: Int, to: Int, by: String): StrBuf {
  return this.delete(from, to).insert(from, by)
}
