use "io"

type BufferIStream < IStream

def BufferIStream.new(buf: [Byte])

type BufferOStream < OStream

def BufferOStream.new()
def BufferOStream.len(): Int
def BufferOStream.getBytes(): [Byte]
def BufferOStream.reset()
