type Graphics;

// color mask: 0x00RRGGBB
def get_color(g: Graphics): Int;
def set_color(g: Graphics, rgb: Int);

// font masks
const FACE_SYSTEM = 0;
const FACE_MONO = 32;
const FACE_PROP = 64;
const STYLE_PLAIN = 0;
const STYLE_BOLD = 1;
const STYLE_ITALIC = 2;
const STYLE_ULINE = 4;
const SIZE_SMALL = 8;
const SIZE_MED = 0;
const SIZE_LARGE = 16;

def get_font(g: Graphics): Int;
def set_font(g: Graphics, font: Int);
def str_width(font: Int, str: String): Int;
def font_height(font: Int): Int;

def draw_line(g: Graphics, x1: Int, y1: Int, x2: Int, y2: Int);
def draw_rect(g: Graphics, x: Int, y: Int, w: Int, h: Int);
def fill_rect(g: Graphics, x: Int, y: Int, w: Int, h: Int);
def draw_roundrect(g: Graphics, x: Int, y: Int, w: Int, h: Int, arcw: Int, arch: Int);
def fill_roundrect(g: Graphics, x: Int, y: Int, w: Int, h: Int, arcw: Int, arch: Int);
def draw_arc(g: Graphics, x: Int, y: Int, w: Int, h: Int, sta: Int, a: Int);
def fill_arc(g: Graphics, x: Int, y: Int, w: Int, h: Int, sta: Int, a: Int);
def fill_triangle(g: Graphics, x1: Int, y1: Int, x2: Int, y2: Int, x3: Int, y3: Int);
def draw_string(g: Graphics, str: String, x: Int, y: Int);
type Image;
def draw_image(g: Graphics, im: Image, x: Int, y: Int);
def copy_area(g: Graphics, xsrc: Int, ysrc: Int, w: Int, h: Int, xdst: Int, ydst: Int);
