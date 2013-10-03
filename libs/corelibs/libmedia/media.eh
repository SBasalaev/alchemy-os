
def play_tone(note: Int, duration: Int, volume: Int = 100);

def get_supported_ctypes(): [String];

type Player < Any;

type IStream;
def new_player(in: IStream, ctype: String): Player;
const `Player.new` = new_player;
def Player.get_ctype(): String;
def Player.set_loops(count: Int);
def Player.get_duration(): Long;
def Player.get_time(): Long;
def Player.set_time(time: Long);
def Player.start();
def Player.stop();
def Player.close();