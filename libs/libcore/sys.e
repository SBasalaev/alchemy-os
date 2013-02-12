/* Core library: various system functions
 * Copyright (c) 2012-2013 Sergey Basalaev
 * Licensed under GPL v3 with linkage exception
 */

use "process.eh"

def exec_wait(prog: String, args: [String]): Int = new_process().start_wait(prog, args)
def exec(prog: String, args: [String]) = new_process().start(prog, args)
