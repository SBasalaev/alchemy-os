type Process < Any;

const PS_NEW = 0
const PS_RUNNING = 1
const PS_ENDED = 5

const MIN_PRIORITY = 1
const NORM_PRIORITY = 5
const MAX_PRIORITY = 10

def new_process(): Process;

def Process.get_state(): Int;

// methods for NEW state
def Process.getenv(key: String): String;
def Process.setenv(key: String, value: String);

def Process.get_in(): IStream;
def Process.get_out(): OStream;
def Process.get_err(): OStream;
def Process.set_in(in: IStream);
def Process.set_out(out: OStream);
def Process.set_err(err: OStream);
def Process.get_cwd(): String;
def Process.set_cwd(dir: String);

def Process.start(prog: String, args: [String]);
def Process.start_wait(prog: String, args: [String]): Int;

// methods for NEW and RUNNING state
def Process.get_priority(): Int;
def Process.set_priority(value: Int);

// methods for RUNNING state
def Process.get_name(): String;
def Process.interrupt();

// methods for ENDED state
//def Process.get_error(): Error;
def Process.get_exitcode(): Int;