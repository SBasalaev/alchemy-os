use "ui_types.eh"
use "font.eh"

type Graphics;

// color mask: 0x00RRGGBB
def get_color(g: Graphics): Int;
def set_color(g: Graphics, rgb: Int);

def draw_line(g: Graphics, x1: Int, y1: Int, x2: Int, y2: Int);
def draw_rect(g: Graphics, x: Int, y: Int, w: Int, h: Int);
def fill_rect(g: Graphics, x: Int, y: Int, w: Int, h: Int);
def draw_roundrect(g: Graphics, x: Int, y: Int, w: Int, h: Int, arcw: Int, arch: Int);
def fill_roundrect(g: Graphics, x: Int, y: Int, w: Int, h: Int, arcw: Int, arch: Int);
def draw_arc(g: Graphics, x: Int, y: Int, w: Int, h: Int, sta: Int, a: Int);
def fill_arc(g: Graphics, x: Int, y: Int, w: Int, h: Int, sta: Int, a: Int);
def fill_triangle(g: Graphics, x1: Int, y1: Int, x2: Int, y2: Int, x3: Int, y3: Int);
def draw_string(g: Graphics, str: String, x: Int, y: Int);
def draw_image(g: Graphics, im: Image, x: Int, y: Int);
def copy_area(g: Graphics, xsrc: Int, ysrc: Int, w: Int, h: Int, xdst: Int, ydst: Int);
