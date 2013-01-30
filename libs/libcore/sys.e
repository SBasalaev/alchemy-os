use "process.eh"

def exec_wait(prog: String, args: [String]): Int = new_process().start_wait(prog, args)
def exec(prog: String, args: [String]) = new_process().start(prog, args)

def Bool.tostr(): String = if (this == true) "true" else "false"