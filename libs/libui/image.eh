use "ui_types.eh"

type Image < Any;

/* mutable image */
def new_image(w: Int, h: Int): Image;
def Image.graphics(): Graphics;

/* immutable images */
type IStream;
def image_from_argb(argb: [Int], w: Int, h: Int, alpha: Bool): Image;
def image_from_file(file: String): Image;
def image_from_stream(in: IStream): Image;
def image_from_data(data: [Byte]): Image;
def image_from_image(im: Image, x: Int, y: Int, w: Int, h: Int): Image;

def Image.get_argb(argb: [Int], ofs: Int, scanlen: Int, x: Int, y: Int, w: Int, h: Int);
