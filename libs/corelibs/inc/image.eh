use "graphics.eh"

type Image < Any

/* mutable image */
def Image.new(w: Int, h: Int)
def Image.graphics(): Graphics

/* immutable images */
type IStream
def imageFromARGB(argb: [Int], w: Int, h: Int, alpha: Bool): Image
def imageFromFile(file: String): Image
def imageFromStream(input: IStream): Image
def imageFromData(data: [Byte], ofs: Int = 0, len: Int = -1): Image
def imageFromImage(im: Image, x: Int, y: Int, w: Int, h: Int): Image

def Image.getARGB(argb: [Int], ofs: Int, scanlen: Int, x: Int, y: Int, w: Int, h: Int)
def Image.getWidth(): Int
def Image.getHeight(): Int
def Image.isMutable(): Bool
