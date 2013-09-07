use "Object3D"

type Background < Object3D;

type Image2D;

const BG_BORDER = 32;
const BG_REPEAT = 33;

def Background.new();
def Background.setColorClearEnable(enable: Bool);
def Background.isColorClearEnabled(): Bool;
def Background.setDepthClearEnable(enable: Bool);
def Background.isDepthClearEnabled(): Bool;
def Background.setColor(argb: Int);
def Background.getColor(): Int;
def Background.setImage(image: Image2D);
def Background.getImage(): Image2D;
def Background.setImageMode(modeX: Int, modeY: Int);
def Background.getImageModeX(): Int;
def Background.getImageModeY(): Int;
def Background.setCrop(cropX: Int, cropY: Int, w: Int, h: Int);
def Background.getCropX(): Int;
def Background.getCropY(): Int;
def Background.getCropWidth(): Int;
def Background.getCropHeight(): Int;
