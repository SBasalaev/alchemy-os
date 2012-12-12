/* List API */

type List < Any;

def new_list(): List;
const `List.new` = new_list;

def List.len(): Int;
def List.get(at: Int): Any;
def List.set(at: Int, val: Any);
def List.add(val: Any);
def List.addall(vals: [Any]);
def List.insert(at: Int, val: Any);
def List.insertall(at: Int, vals: [Any]);
def List.remove(at: Int);
def List.clear();
def List.range(from: Int, to: Int): List;
def List.indexof(val: Any): Int;
def List.lindexof(val: Any): Int;
def List.filter(f: (Any):Bool): List;
def List.filterself(f: (Any):Bool);
def List.map(f: (Any):Any): List;
def List.mapself(f: (Any):Any);
def List.reduce(f: (Any,Any):Any): Any;
def List.sort(f: (Any,Any):Int): List;
def List.sortself(f: (Any,Any):Int);
def List.reverse(): List;
def List.toarray(): [Any];
def List.tostr(): String;
