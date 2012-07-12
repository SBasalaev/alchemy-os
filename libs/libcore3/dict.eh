/* Dictionary API */

type Dict < Any;

def new_dict(): Dict;

def Dict.size(): Int;
def Dict.get(key: Any): Any;
def Dict.set(key: Any, value: Any);
def Dict.remove(key: Any);
def Dict.clear();
def Dict.keys(): [Any];
