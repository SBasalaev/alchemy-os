/* Core library: Math functions
 * (C) 2011-2012, Sergey Basalaev
 * Licensed under GPL v3 with linkage exception
 */

use "math.eh"

def sgn(val: Double): Int = if (val < 0d) -1 else if (val > 0d) 1 else 0

def deg2rad(val: Double): Double = val*PI/180
def rad2deg(val: Double): Double = val*180/PI
