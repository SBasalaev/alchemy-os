/* Core library: List implementation
 * (C) 2011-2013, Sergey Basalaev
 * Licensed under GPL v3 with linkage exception
 */

use "error.eh"
use "strbuf.eh"
use "string.eh"

type List {
  size: Int,
  data: [Any]
}

def List.grow(newlen: Int) {
  var newdata = new [Any](newlen)
  acopy(this.data, 0, newdata, 0, this.size)
  this.data = newdata
}

def new_list(): List = new List {size = 0, data = new [Any](10)}

def List.len(): Int = this.size

def List.get(at: Int): Any {
  if (at >= 0 && at < this.size) {
    this.data[at]
  } else {
    error(ERR_RANGE)
    null
  }
}

def List.set(at: Int, val: Any) {
  if (at >= 0 && at < this.size) {
    this.data[at] = val
  } else {
    error(ERR_RANGE)
  }
}

def List.insert(at: Int, val: Any) {
  var size = this.size
  if (at >= 0 && at <= size) {
    if (this.data.len == size)
      this.grow((size * 3) / 2 + 1)
    if (size-at > 0)
      acopy(this.data, at, this.data, at+1, size-at)
    this.data[at] = val
    this.size = size+1
  } else {
    error(ERR_RANGE)
  }
}

def List.insertall(at: Int, vals: [Any]) {
  var size = this.size
  if (at >= 0 && at <= size) {
    if (vals.len > 0) {
      if (this.data.len < size + vals.len)
        this.grow(this.data.len + vals.len)
      if (size-at > 0)
        acopy(this.data, at, this.data, at+vals.len, size-at)
      acopy(vals, 0, this.data, at, vals.len)
      this.size = size + vals.len
    }
  } else {
    error(ERR_RANGE)
  }
}

def List.add(val: Any) = this.insert(this.size, val)

def List.addall(vals: [Any]) = this.insertall(this.size, vals)

def List.remove(at: Int) {
  var size = this.size-1
  if (at >= 0 && at <= size) {
    if (size-at > 0)
      acopy(this.data, at+1, this.data, at, size-at)
    this.data[size] = null
    this.size = size
  } else {
    error(ERR_RANGE)
  }
}

def List.clear() {
  for (var i=this.size-1, i>=0, i-=1)
    this.data[i] = null
  this.size = 0
}

def List.range(from: Int, to: Int): List {
  if (from >= 0 && to <= this.size && from <= to) {
    var data = new [Any] (to-from)
    acopy(this.data, from, data, 0, data.len)
    new List(data.len, data)
  } else {
    error(ERR_RANGE)
    null
  }
}

def List.indexof(val: Any): Int {
  var i = 0
  while (i < this.size && this.data[i] != val) i=i+1
  if (i == this.size) -1
  else i
}

def List.lindexof(val: Any): Int {
  var i = this.size - 1
  while (i >= 0 && this.data[i] != val) i=i-1
  i
}

def List.filter(f: (Any):Bool): List {
  var l = new_list()
  var size = this.size
  for (var i=0, i<size, i=i+1) {
    var e = this.data[i]
    if (f(e)) l.add(e)
  }
  l
}

def List.filterself(f: (Any):Bool) {
  var size = this.size
  var data = this.data
  var count = 0
  for (var i=0, i<size, i=i+1) {
    var e = this.data[i]
    if (f(e)) {
      data[count] = e
      count = count+1
    }
  }
  for (var i=count+1, i<size, i=i+1) {
    data[i] = null
  }
  this.size = count
}

def List.map(f: (Any):Any): List {
  var size = this.size
  var data = this.data
  var newdata = new [Any](size)
  for (var i=0, i<size, i=i+1) {
    newdata[i] = f(data[i])
  }
  new List(size, newdata)
}

def List.mapself(f: (Any):Any) {
  var size = this.size
  var data = this.data
  for (var i=0, i<size, i=i+1) {
    data[i] = f(data[i])
  }
}

def _quicksort(a: [Any], low: Int, high: Int, f: (Any,Any):Int) {
  var i = low;
  var j = high;
  var x = a[(low+high)/2]
  while(i <= j) {
    while(f(a[i],x) < 0) i = i+1
    while(f(a[j],x) > 0) j = j-1
    if (i <= j) {
      var tmp = a[i]
      a[i] = a[j]
      a[j] = tmp
      i = i+1
      j = j-1
    }
  }
  if (low < j)  _quicksort(a, low, j, f)
  if (i < high) _quicksort(a, i, high, f)
}

def List.sort(f: (Any,Any):Int): List {
  var size = this.size
  var data = this.data
  var newdata = new [Any](size)
  acopy(data, 0, newdata, 0, size)
  _quicksort(newdata, 0, size-1, f)
  new List(size, newdata)
}

def List.sortself(f: (Any,Any):Int) {
  _quicksort(this.data, 0, this.size-1, f)
}

def List.reduce(f: (Any,Any):Any): Any {
  var size = this.size
  if (size == 0) {
    null
  } else {
    var data = this.data
    var e = data[0]
    for (var i=1, i<size, i=i+1) {
      e = f(e,data[i])
    }
    e
  }
}

def List.reverse(): List {
  var size = this.size
  var data = this.data
  var newdata = new [Any](size)
  for (var i=0, i<size, i=i+1) {
    newdata[size-i-1] = data[i]
  }
  new List(size, newdata)
}

def List.toarray(): [Any] {
  var arr = new [Any](this.size)
  acopy(this.data, 0, arr, 0, arr.len)
  arr
}

def List.copyinto(from: Int, buf: Array, ofs: Int, len: Int) {
  if (from + len > this.size) {
    len = this.size - from
  }
  if (len > 0) {
    acopy(this.data, from, buf, ofs, len)
  }
}

def List.tostr(): String {
  var sb = new_strbuf()
  sb.addch('[')
  for (var i=0, i<this.size, i=i+1) {
    if (i != 0) sb.append(", ")
    sb.append(this.data[i])
  }
  sb.addch(']').tostr()
}

def List.eq(other: List): Bool {
  var result = other != null && this.size == other.size
  for (var i=0, result && i < this.size, i+=1) {
    result = this.data[i] == other.data[i]
  }
  result
}