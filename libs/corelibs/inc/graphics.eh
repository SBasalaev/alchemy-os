use "font"

type Image
type Graphics < Any

// color mask: 0x00RRGGBB
def Graphics.getColor(): Int
def Graphics.setColor(rgb: Int)

const SOLID = 0
const DOTTED = 1

def Graphics.getStroke(): Int
def Graphics.setStroke(stroke: Int)

def Graphics.getFont(): Int
def Graphics.setFont(font: Int)

def Graphics.drawLine(x1: Int, y1: Int, x2: Int, y2: Int)
def Graphics.drawRect(x: Int, y: Int, w: Int, h: Int)
def Graphics.fillRect(x: Int, y: Int, w: Int, h: Int)
def Graphics.drawRoundrect(x: Int, y: Int, w: Int, h: Int, arcw: Int, arch: Int)
def Graphics.fillRoundrect(x: Int, y: Int, w: Int, h: Int, arcw: Int, arch: Int)
def Graphics.drawArc(x: Int, y: Int, w: Int, h: Int, sta: Int, a: Int)
def Graphics.fillArc(x: Int, y: Int, w: Int, h: Int, sta: Int, a: Int)
def Graphics.fillTriangle(x1: Int, y1: Int, x2: Int, y2: Int, x3: Int, y3: Int)
def Graphics.drawString(str: String, x: Int, y: Int)
def Graphics.drawImage(im: Image, x: Int, y: Int)
def Graphics.drawRGB(rgb: [Int], ofs: Int, scanlen: Int, x: Int, y: Int, w: Int, h: Int, alpha: Bool = false)
def Graphics.copyArea(xsrc: Int, ysrc: Int, w: Int, h: Int, xdst: Int, ydst: Int)

const TR_NONE = 0
const TR_ROT90 = 5
const TR_ROT180 = 3
const TR_ROT270 = 6
const TR_HMIRROR = 2
const TR_HMIRROR_ROT90 = 7
const TR_VMIRROR = 1
const TR_VMIRROR_ROT90 = 4

def Graphics.drawRegion(im: Image, xsrc: Int, ysrc: Int, w: Int, h: Int, trans: Int, xdst: Int, ydst: Int)
