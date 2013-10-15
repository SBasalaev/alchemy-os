type Thread < Any;

def currentThread(): Thread;

def Thread.new(run: ());
def Thread.start();
def Thread.isAlive(): Bool;
def Thread.interrupt();
def Thread.isInterrupted(): Bool;
def Thread.join();

def Lock < Any;

def Lock.new();
def Lock.lock();
def Lock.tryLock(): Bool;
def Lock.unlock();
