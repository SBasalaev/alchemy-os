use "io"

type Process < Any;

const PS_NEW = 0
const PS_RUNNING = 1
const PS_ENDED = 5

const MIN_PRIORITY = 1
const NORM_PRIORITY = 5
const MAX_PRIORITY = 10

def currentProcess(): Process;

def Process.new(cmd: String, args: [String]);
def Process.getState(): Int;

// methods for NEW and RUNNING state
def Process.getPriority(): Int;
def Process.setPriority(value: Int);
def Process.getName(): String;
def Process.getArgs(): [String];

// methods for NEW state
def Process.setEnv(key: String, value: String);
def Process.setIn(in: IStream);
def Process.setOut(out: OStream);
def Process.setErr(err: OStream);
def Process.setCwd(dir: String);

def Process.start(): Process;
def Process.waitFor(): Int;

// methods for RUNNING state
def Process.kill();

// methods for ENDED state
def Process.getExitCode(): Int;
def Process.getError(): Error;
