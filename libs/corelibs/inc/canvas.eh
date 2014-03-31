use "graphics.eh"
use "ui.eh"

type Canvas < Screen

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

def Canvas.new(full: Bool = false): Canvas
def Canvas.graphics(): Graphics
def Canvas.repaint(x: Int, y: Int, w: Int, h: Int)
def Canvas.refresh()
def Canvas.actionCode(key: Int): Int
def Canvas.keyName(key: Int): String
def Canvas.hasPtrEvents(): Bool
def Canvas.hasPtrDragEvent(): Bool
def Canvas.hasHoldEvent(): Bool
