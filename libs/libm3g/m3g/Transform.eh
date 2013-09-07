type Transform < Any;

type VertexArray;

def Transform.new();
def Transform.clone(): Transform;
def Transform.setIdentity();
def Transform.set(matrix: [Float]);
def Transform.get(matrix: [Float]);
def Transform.invert();
def Transform.transpose();
def Transform.postMultiply(transform: Transform);
def Transform.postScale(sx: Float, sy: Float, sz: Float);
def Transform.postRotate(angle: Float, ax: Float, ay: Float, az: Float);
def Transform.postRotateQuat(qx: Float, qy: Float, qz: Float, qw: Float);
def Transform.postTranslate(tx: Float, ty: Float, tz: Float);
def Transform.transform(in: VertexArray, out: [Float], w: Bool);
