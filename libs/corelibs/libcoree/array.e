/* Array utilities. */

def `[Byte].contains`(this: [Byte], item: Byte): Bool {
  for (var b in this) {
    if (b == item) return true
  }
  return false
}

def `[Char].contains`(this: [Char], item: Char): Bool {
  for (var c in this) {
    if (c == item) return true
  }
  return false
}

def `[Short].contains`(this: [Short], item: Short): Bool {
  for (var s in this) {
    if (s == item) return true
  }
  return false
}

def `[Int].contains`(this: [Int], item: Int): Bool {
  for (var i in this) {
    if (i == item) return true
  }
  return false
}

def `[Long].contains`(this: [Long], item: Long): Bool {
  for (var l in this) {
    if (l == item) return true
  }
  return false
}

def `[Float].contains`(this: [Float], item: Float): Bool {
  for (var f in this) {
    if (f == item) return true
  }
  return false
}

def `[Double].contains`(this: [Double], item: Double): Bool {
  for (var d in this) {
    if (d == item) return true
  }
  return false
}

def `[Any].contains`(this: [Any], item: Any, eqfunc: (Any,Any): Bool): Bool {
  for (var a in this) {
    if (eqfunc == null || item == null || a == null) {
      if (a == item) return true
    } else {
      if (eqfunc(a, item)) return true
    }
  }
  return false
}
