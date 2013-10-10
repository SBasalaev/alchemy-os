use "/inc/rnd.eh"
use "/inc/time.eh"

def rnd(max: Int): Int = new Random(systime()).next(max);
def rndint(): Int = new Random(systime()).nextInt();
def rndlong(): Long = new Random(systime()).nextLong();
def rndfloat(): Float = new Random(systime()).nextFloat();
def rnddouble(): Double = new Random(systime()).nextDouble();
