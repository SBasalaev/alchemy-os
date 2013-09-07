use "Object3D"

type Transformable < Object3D;

type Transform;

def Transformable.setOrientation(angle: Float, ax: Float, ay: Float, az: Float);
def Transformable.preRotate(angle: Float, ax: Float, ay: Float, az: Float);
def Transformable.postRotate(angle: Float, ax: Float, ay: Float, az: Float);
def Transformable.getOrientation(angleAxis: [Float]);
def Transformable.setScale(sx: Float, sy: Float, sz: Float);
def Transformable.scale(sx: Float, sy: Float, sz: Float);
def Transformable.getScale(xyz: [Float]);
def Transformable.setTranslation(tx: Float, ty: Float, tz: Float);
def Transformable.translate(tx: Float, ty: Float, tz: Float);
def Transformable.getTranslation(xyz: [Float]);
def Transformable.setTransform(transform: Transform);
def Transformable.getTransform(transform: Transform);
def Transformable.getCompositeTransform(transform: Transform);
