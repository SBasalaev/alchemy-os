use "process.eh"

def exec_wait(prog: String, args: [String]): Int = new_process().start_wait(prog, args)
def exec(prog: String, args: [String]): Int = new_process().start(prog, args)
