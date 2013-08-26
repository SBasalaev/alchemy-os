use "Object3D" 

type PolygonMode < Object3D;

const CULL_BACK = 160;
const CULL_FRONT = 161;
const CULL_NONE = 162;
const SHADE_FLAT = 164;
const SHADE_SMOOTH = 165;
const WINDING_CCW = 168;
const WINDING_CW = 169;

def PolygonMode.new();
def PolygonMode.setCulling(mode: Int);
def PolygonMode.getCulling(): Int;
def PolygonMode.setWinding(mode: Int);
def PolygonMode.getWinding(): Int;
def PolygonMode.setShading(mode: Int);
def PolygonMode.getShading(): Int;
def PolygonMode.setTwoSidedLightingEnable(enable: Bool);
def PolygonMode.isTwoSidedLightingEnabled(): Bool;
def PolygonMode.setLocalCameraLightingEnable(enable: Bool);
def PolygonMode.isLocalCameraLightingEnabled(): Bool;
def PolygonMode.setPerspectiveCorrectionEnable(enable: Bool);
def PolygonMode.isPerspectiveCorrectionEnabled(): Bool;
