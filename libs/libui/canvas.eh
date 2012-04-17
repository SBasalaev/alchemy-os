use "graphics";

type Screen;

def new_canvas(full: Bool): Screen;
def canvas_graphics(cnv: Screen): Graphics;
def canvas_read_key(cnv: Screen): Int;
def canvas_refresh(cnv: Screen);
