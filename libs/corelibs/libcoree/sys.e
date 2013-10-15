/* Core library: various system functions
 * Copyright (c) 2012-2013 Sergey Basalaev
 * Licensed under GPL v3 with linkage exception
 */

use "process"

def execWait(prog: String, args: [String]): Int = new Process().start(prog, args).waitFor()
def exec(prog: String, args: [String]) = new Process().start(prog, args)
