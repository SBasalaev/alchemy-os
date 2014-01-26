/* Core library: various system functions
 * Copyright (c) 2012-2014 Sergey Basalaev
 * Licensed under GPL v3 with linkage exception
 */

use "process"

def execWait(prog: String, args: [String]): Int = new Process(prog, args).start().waitFor()
def exec(prog: String, args: [String]) = new Process(prog, args).start()
