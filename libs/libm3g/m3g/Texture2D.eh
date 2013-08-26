use "Transformable"

type Texture2D < Transformable;

const FILTER_BASE_LEVEL = 208;
const FILTER_LINEAR = 209;
const FILTER_NEAREST = 210;
const FUNC_ADD = 224;
const FUNC_BLEND = 225;
const FUNC_DECAL = 226;
const FUNC_MODULATE = 227;
const FUNC_REPLACE = 228;
const WRAP_CLAMP = 240;
const WRAP_REPEAT = 241;

type Image2D;

def Texture2D.new(image: Image2D);
def Texture2D.setImage(image: Image2D);
def Texture2D.getImage(): Image2D;
def Texture2D.setFiltering(levelFilter: Int, imageFilter: Int);
def Texture2D.getLevelFilter(): Int;
def Texture2D.getImageFilter(): Int;
def Texture2D.setWrapping(wrapS: Int, wrapT: Int);
def Texture2D.getWrappingS(): Int;
def Texture2D.getWrappingT(): Int;
def Texture2D.setBlending(func: Int);
def Texture2D.getBlending(): Int;
def Texture2D.setBlendColor(rgb: Int);
def Texture2D.getBlendColor(): Int;
