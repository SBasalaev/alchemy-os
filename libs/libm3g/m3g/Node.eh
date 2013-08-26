use "Transformable"

type Node < Transformable;

const ALIGN_NONE = 144;
const ALIGN_ORIGIN = 145;
const ALIGN_X_AXIS = 146;
const ALIGN_Y_AXIS = 147;
const ALIGN_Z_AXIS = 148;

def Node.setRenderingEnable(enable: Bool);
def Node.isRenderingEnabled(): Bool;
def Node.setPickingEnable(enable: Bool);
def Node.isPickingEnabled(): Bool;
def Node.setScope(scope: Int);
def Node.getScope(): Int;
def Node.setAlphaFactor(alphaFactor: Float);
def Node.getAlphaFactor(): Float;
def Node.getParent(): Node;
def Node.getTransformTo(target: Node, transform: Transform): Bool;
def Node.align(reference: Node);
def Node.setAlignment(zRef: Node, zTarget: Int, yRef: Node, yTarget: Int);
def Node.getAlignmentTarget(axis: Int): Int;
def Node.getAlignmentReference(axis: Int): Int;
