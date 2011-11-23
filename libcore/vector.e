/* Vector library
 * (C) 2011, Sergey Basalaev
 * Licensed under LGPL v3
 */

use "sys"
use "vector.eh"

type Vector {
  data: Array,
  size: Int
}

def new_vector(): Vector {
  var v = new Vector()
  v.size = 0
  v.data = new Array(8)
  v
}

def v_size(v: Vector): Int {
  v.size
}

def v_get(v: Vector, at: Int): Any {
  if (at < 0 || at >= v.size)
    null
  else
    v.data[at]
}

def v_set(v: Vector, at: Int, a: Any) {
  if (at < 0 || at >= v.size) {
    null
  } else {
    v.data[at] = a
  }
}

def v_remove(v: Vector, at: Int) {
  if (at < 0 || at >= v.size) {
    null
  } else {
    var n = v.size - at - 1
    if (n > 0) {
      acopy(v.data, at+1, v.data, at, n)
    }
    v.size = v.size - 1
    v.data[v.size] = null
  }
}

def _v_grow(v: Vector) {
  var newdata = new Array(v.data.len << 1)
  acopy(v.data, 0, newdata, 0, v.data.len)
  v.data = newdata
}

def v_add(v: Vector, a: Any) {
  if (v.size == v.data.len) {
    _v_grow(v)
  }
  v.data[v.size] = a
  v.size = v.size + 1
}

def v_insert(v: Vector, at: Int, a: Any) {
  if (v.size == v.data.len) {
    _v_grow(v)
  }
  acopy(v.data, at, v.data, at+1, v.size - at)
  v.size = v.size + 1
  v.data[at] = a
}

def v_indexof(v: Vector, a: Any): Int {
  var n = 0
  while (n < v.size && v.data[n] != a) {
    n = n + 1
  }
  if (n == v.size) -1 else n
}

def v_lindexof(v: Vector, a: Any): Int {
  var n = v.size - 1
  while (n >= 0 && v.data[n] != a) {
    n = n - 1
  }
  n
}
