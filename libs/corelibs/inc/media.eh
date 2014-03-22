use "io"

def playTone(note: Int, duration: Int, volume: Int = 100)

def getSupportedCtypes(): [String]

type Player < Any

def streamPlayer(input: String, ctype: Int): Player
def filePlayer(file: String, ctype: Int): Player

def Player.getCtype(): String
def Player.setLoops(count: Int)
def Player.getDuration(): Long
def Player.getTime(): Long
def Player.setTime(time: Long)
def Player.start()
def Player.stop()
def Player.close()
