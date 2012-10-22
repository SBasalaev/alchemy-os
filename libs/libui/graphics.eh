use "ui_types.eh"

type Graphics < Any;

// color mask: 0x00RRGGBB
def Graphics.get_color(): Int;
def Graphics.set_color(rgb: Int);

const SOLID = 0
const DOTTED = 1

def Graphics.get_stroke(): Int;
def Graphics.set_stroke(stroke: Int);

def Graphics.get_font(): Int;
def Graphics.set_font(font: Int);

def Graphics.draw_line(x1: Int, y1: Int, x2: Int, y2: Int);
def Graphics.draw_rect(x: Int, y: Int, w: Int, h: Int);
def Graphics.fill_rect(x: Int, y: Int, w: Int, h: Int);
def Graphics.draw_roundrect(x: Int, y: Int, w: Int, h: Int, arcw: Int, arch: Int);
def Graphics.fill_roundrect(x: Int, y: Int, w: Int, h: Int, arcw: Int, arch: Int);
def Graphics.draw_arc(x: Int, y: Int, w: Int, h: Int, sta: Int, a: Int);
def Graphics.fill_arc(x: Int, y: Int, w: Int, h: Int, sta: Int, a: Int);
def Graphics.fill_triangle(x1: Int, y1: Int, x2: Int, y2: Int, x3: Int, y3: Int);
def Graphics.draw_string(str: String, x: Int, y: Int);
def Graphics.draw_image(im: Image, x: Int, y: Int);
def Graphics.draw_rgb(rgb: [Int], ofs: Int, scanlen: Int, x: Int, y: Int, w: Int, h: Int, alpha: Bool);
def Graphics.copy_area(xsrc: Int, ysrc: Int, w: Int, h: Int, xdst: Int, ydst: Int);

const TR_NONE = 0
const TR_ROT90 = 5
const TR_ROT180 = 3
const TR_ROT270 = 6
const TR_HMIRROR = 2
const TR_HMIRROR_ROT90 = 7
const TR_VMIRROR = 1
const TR_VMIRROR_ROT90 = 4

def Graphics.draw_region(im: Image, xsrc: Int, ysrc: Int, w: Int, h: Int, trans: Int, xdst: Int, ydst: Int);
