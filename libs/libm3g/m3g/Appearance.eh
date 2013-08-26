use "Object3D"

type Appearance < Object3D;

type CompositingMode;
type Fog;
type Material;
type PolygonMode;
type Texture2D;

def Appearance.new();
def Appearance.setLayer(layer: Int);
def Appearance.getLayer(): Int;
def Appearance.setFog(fog: Fog);
def Appearance.getFog(): Fog;
def Appearance.setPolygonMode(mode: PolygonMode);
def Appearance.getPolygonMode(): PolygonMode;
def Appearance.setCompositingMode(mode: CompositingMode);
def Appearance.getCompositingMode(): CompositingMode;
def Appearance.setTexture(index: Int, texture: Texture2D);
def Appearance.getTexture(index: Int): Texture2D;
def Appearance.setMaterial(material: Material);
def Appearance.getMaterial(): Material;
