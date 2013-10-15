use "/inc/process.eh"

const this_process = currentProcess;
const new_process = `Process.new`;
const `Process.get_state` = `Process.getState`;
const `Process.getenv` = `Process.getEnv`;
const `Process.setenv` = `Process.setEnv`;
const `Process.set_in` = `Process.setIn`;
const `Process.set_out` = `Process.setOut`;
const `Process.set_err` = `Process.setErr`;
const `Process.set_cwd` = `Process.setCwd`;
const `Process.set_priority` = `Process.setPriority`;
const `Process.get_priority` = `Process.getPriority`;
const `Process.get_name` = `Process.getName`;
const `Process.interrupt` = `Process.kill`;
const `Process.get_exitcode` = `Process.getExitCode`;

def Process.start_wait() = this.start().waitFor()
