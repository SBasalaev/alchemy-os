use "Object3D"

type Image2D < Object3D;

const FORMAT_ALPHA = 96;
const FORMAT_LUMINANCE = 97;
const FORMAT_LUMINANCE_ALPHA = 98;
const FORMAT_RGB = 99;
const FORMAT_RGBA = 100;

type Image;

def createImage2D(format: Int, image: Image): Image2D;
def Image2D.new(format: Int, width: Int, height: Int);
def Image2D.set(x: Int, y: Int, width: Int, height: Int, image: [Byte]);
def Image2D.isMutable(): Bool;
def Image2D.getFormat(): Int;
def Image2D.getWidth(): Int;
def Image2D.getHeight(): Int;
