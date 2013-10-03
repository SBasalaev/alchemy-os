use "connection.eh"

type Pipe < StreamConnection;

def new_pipe(): Pipe;
const `Pipe.new` = new_pipe;