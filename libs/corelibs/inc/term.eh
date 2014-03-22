use "io"

type TermIStream < IStream

def isTerm(stream: IStream): Bool

def TermIStream.clear()
def TermIStream.getPrompt(): String
def TermIStream.setPrompt(prompt: String)
