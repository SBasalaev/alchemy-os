/* Input/output routines. */

const EOF = -1;

type Connection < Any;

def Connection.close();

type IStream < Connection;

def IStream.read(): Int;
def IStream.readArray(buf: [Byte], ofs: Int = 0, len: Int = -1): Int;
def IStream.readFully(): [Byte];
def IStream.skip(num: Long): Long;
def IStream.available(): Int;
def IStream.reset();

type OStream < Connection;

def OStream.write(b: Int);
def OStream.writeArray(buf: [Byte], ofs: Int = 0, len: Int = -1);
def OStream.print(a: Any);
def OStream.println(a: Any);
def OStream.flush();
def OStream.printf(fmt: String, args: [Any]);
def OStream.writeAll(input: IStream);

type StreamConnection < Connection;

def StreamConnection.openInput(): IStream;
def StreamConnection.openOutput(): OStream;
