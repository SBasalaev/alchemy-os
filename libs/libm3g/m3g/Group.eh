use "Node" 

type Group < Node;

type Camera;
type RayIntersection;

def Group.new();
def Group.addChild(child: Node);
def Group.removeChild(child: Node);
def Group.getChildCount(): Int;
def Group.getChild(index: Int): Node;
def Group.pickByRay(scope: Int, ox: Float, oy: Float, oz: Float, dx: Float, dy: Float, dz: Float, ri: RayIntersection): Bool;
def Group.pickByCamera(scope: Int, x: Float, y: Float, camera: Camera, ri: RayIntersection): Bool;
