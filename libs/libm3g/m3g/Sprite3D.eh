use "Node"

type Sprite3D < Node;

def Sprite3D.new(scaled: Bool, image: Image2D, appearance: Appearance);
def Sprite3D.isScaled(): Bool;
def Sprite3D.setAppearance(appearance: Appearance);
def Sprite3D.getAppearance(): Appearance;
def Sprite3D.setImage(image: Image2D);
def Sprite3D.getImage(): Image2D;
def Sprite3D.setCrop(cropX: Int, cropY: Int, width: Int, height: Int);
def Sprite3D.getCropX(): Int;
def Sprite3D.getCropY(): Int;
def Sprite3D.getCropWidth(): Int;
def Sprite3D.getCropHeight(): Int;
