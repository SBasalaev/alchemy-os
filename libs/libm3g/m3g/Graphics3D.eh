use "dict.eh"

const HINT_ANTIALIAS = 2;
const HINT_DITHER = 4;
const HINT_OVERWRITE = 16;
const HINT_TRUE_COLOR = 8; 

type Graphics3D < Any;

type Appearance;
type Background;
type Camera;
type IndexBuffer;
type Light;
type Node;
type Transform;
type VertexBuffer;
type World;

def graphics3D(): Graphics3D;

def Graphics3D.bindTarget(target: Any, depthBuffer: Bool = true, hints: Int = 0);
def Graphics3D.releaseTarget();
def Graphics3D.getTarget(): Any;
def Graphics3D.getHints(): Int;
def Graphics3D.isDepthBufferEnabled(): Bool;
def Graphics3D.setViewport(x: Int, y: Int, w: Int, h: Int);
def Graphics3D.getViewportX(): Int;
def Graphics3D.getViewportY(): Int;
def Graphics3D.getViewportWidth(): Int;
def Graphics3D.getViewportHeight(): Int;
def Graphics3D.setDepthRange(near: Float, far: Float);
def Graphics3D.getDepthRangeNear(): Float;
def Graphics3D.getDepthRangeFar(): Float;
def Graphics3D.clear(background: Background);
def Graphics3D.renderWorld(world: World);
def Graphics3D.renderNode(node: Node, transform: Transform);
def Graphics3D.render(vertices: VertexBuffer, triangles: IndexBuffer, appearance: Appearance, transform: Transform = null, scope: Int = -1);
def Graphics3D.setCamera(camera: Camera, transform: Transform);
def Graphics3D.getCamera(transform: Transform = null): Camera;
def Graphics3D.addLight(light: Light, transform: Transform = null): Int;
def Graphics3D.setLight(index: Int, light: Light, transform: Transform);
def Graphics3D.resetLights();
def Graphics3D.getLightCount(): Int;
def Graphics3D.getLight(index: Int, transform: Transform): Light;
def Graphics3D.getProperties(): Dict;
