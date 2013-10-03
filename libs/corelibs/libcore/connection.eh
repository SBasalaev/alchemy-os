/* Connection API */

type IStream;
type OStream;

type Connection < Any;

def Connection.close();

type StreamConnection < Connection;

def StreamConnection.open_input(): IStream;
def StreamConnection.open_output(): OStream;

const `StreamConnection.openInput` = `StreamConnection.open_input`
const `StreamConnection.openOutput` = `StreamConnection.open_output`
