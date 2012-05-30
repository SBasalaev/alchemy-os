use "ui_types.eh"

/* mutable image */
def new_image(w: Int, h: Int): Image;
def image_graphics(im: Image): Graphics;

/* immutable images */
type IStream;
def image_from_argb(argb: Array, w: Int, h: Int, alpha: Bool): Image;
def image_from_stream(in: IStream): Image;
def image_from_data(data: BArray): Image;
def image_from_image(im: Image, x: Int, y: Int, w: Int, h: Int): Image;

def get_image_argb(im: Image, argb: Array, ofs: Int, scanlen: Int, x: Int, y: Int, w: Int, h: Int);
