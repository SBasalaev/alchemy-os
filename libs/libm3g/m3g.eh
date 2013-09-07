use "m3g/AnimationController"
use "m3g/AnimationTrack"
use "m3g/Appearance"
use "m3g/Background"
use "m3g/Camera"
use "m3g/CompositingMode"
use "m3g/Fog"
use "m3g/Group"
use "m3g/Image2D"
use "m3g/IndexBuffer"
use "m3g/KeyframeSequence"
use "m3g/Light"
use "m3g/Material"
use "m3g/Mesh"
use "m3g/MorphingMesh"
use "m3g/Node"
use "m3g/Object3D"
use "m3g/PolygonMode"
use "m3g/RayIntersection"
use "m3g/SkinnedMesh"
use "m3g/Sprite3D"
use "m3g/Texture2D"
use "m3g/Transform"
use "m3g/Transformable"
use "m3g/TriangleStripArray"
use "m3g/VertexArray"
use "m3g/VertexBuffer"
use "m3g/World"
use "dict.eh"

const HINT_ANTIALIAS = 2;
const HINT_DITHER = 4;
const HINT_OVERWRITE = 16;
const HINT_TRUE_COLOR = 8; 

def m3g_isUsed(): Bool;
def m3g_getProperties(): Dict;
def m3g_loadData(data: [Byte], offset: Int): [Object3D];
def m3g_loadFile(name: String): [Object3D];

def m3g_bindTarget(target: Any, depthBuffer: Bool = true, hints: Int = 0);
def m3g_releaseTarget();
def m3g_getTarget(): Any;
def m3g_getHints(): Int;
def m3g_isDepthBufferEnabled(): Bool;
def m3g_setViewport(x: Int, y: Int, w: Int, h: Int);
def m3g_getViewportX(): Int;
def m3g_getViewportY(): Int;
def m3g_getViewportWidth(): Int;
def m3g_getViewportHeight(): Int;
def m3g_setDepthRange(near: Float, far: Float);
def m3g_getDepthRangeNear(): Float;
def m3g_getDepthRangeFar(): Float;
def m3g_clear(background: Background);
def m3g_renderWorld(world: World);
def m3g_renderNode(node: Node, transform: Transform);
def m3g_render(vertices: VertexBuffer, triangles: IndexBuffer, appearance: Appearance, transform: Transform = null, scope: Int = -1);
def m3g_setCamera(camera: Camera, transform: Transform);
def m3g_getCamera(transform: Transform = null): Camera;
def m3g_addLight(light: Light, transform: Transform = null): Int;
def m3g_setLight(index: Int, light: Light, transform: Transform);
def m3g_resetLights();
def m3g_getLightCount(): Int;
def m3g_getLight(index: Int, transform: Transform = null): Light;
