// array operations

def new_ba(len: Int): BArray;
def new_ca(len: Int): CArray;
def newarray(len: Int): Array;

def baload(ba: BArray, at: Int): Int;
def caload(ca: CArray, at: Int): Int;
def aload(ar: Array, at: Int): Any;

def bastore(ba: BArray, at: Int, val: Int);
def castore(ca: CArray, at: Int, val: Int);
def astore(ar: Array, at: Int, val: Any);

def balen(ba: BArray): Int;
def calen(ca: CArray): Int;
def alen(ar: Array): Int;

def bacopy(src: BArray, sofs: Int, dst: BArray, dofs: Int, len: Int);
def cacopy(src: CArray, sofs: Int, dst: CArray, dofs: Int, len: Int);
def acopy(src: Array, sofs: Int, dst: Array, dofs: Int, len: Int);

