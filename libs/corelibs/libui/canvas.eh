use "graphics.eh"

type Canvas < Screen;

// common key codes
const KEY_0 = '0'
const KEY_1 = '1'
const KEY_2 = '2'
const KEY_3 = '3'
const KEY_4 = '4'
const KEY_5 = '5'
const KEY_6 = '6'
const KEY_7 = '7'
const KEY_8 = '8'
const KEY_9 = '9'
const KEY_STAR = '*'
const KEY_HASH = '#'

// key actions
const UP    = 1
const DOWN  = 6
const LEFT  = 2
const RIGHT = 5
const FIRE  = 8
const ACT_A = 9
const ACT_B = 10
const ACT_C = 11
const ACT_D = 12

def new_canvas(full: Bool = false): Canvas;
const `Canvas.new` = new_canvas;
def Canvas.graphics(): Graphics;
def Canvas.read_key(): Int;
def Canvas.refresh();
def Canvas.action_code(key: Int): Int;
def Canvas.has_ptr_events(): Bool;
def Canvas.has_ptrdrag_event(): Bool;
def Canvas.has_hold_event(): Bool;