/* List API */

type List < Any

def List.new(a: Array = null)
def List.len(): Int
def List.get(at: Int): Any
def List.set(at: Int, val: Any)
def List.add(val: Any)
def List.addFrom(arr: Array, ofs: Int = 0, len: Int = -1)
def List.insert(at: Int, val: Any)
def List.insertFrom(at: Int, arr: Array, ofs: Int = 0, len: Int = -1)
def List.remove(at: Int)
def List.clear()
def List.range(from: Int, to: Int): List
def List.indexof(val: Any, from: Int = 0): Int
def List.lindexof(val: Any): Int
def List.filter(f: (Any):Bool): List
def List.filterself(f: (Any):Bool)
def List.map(f: (Any):Any): List
def List.mapself(f: (Any):Any)
def List.reduce(f: (Any,Any):Any): Any
def List.sort(f: (Any,Any):Int): List
def List.sortself(f: (Any,Any):Int)
def List.reverse(): List
def List.toarray(): [Any]
def List.copyInto(from: Int, buf: Array, ofs: Int = 0, len: Int = -1)
