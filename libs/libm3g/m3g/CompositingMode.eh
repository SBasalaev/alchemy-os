use "Object3D"

type CompositingMode < Object3D;

const CM_ALPHA = 64;
const CM_ALPHA_ADD = 65;
const CM_MODULATE = 66;
const CM_MODULATE_X2 = 67;
const CM_REPLACE = 68;

def CompositingMode.new();
def CompositingMode.setBlending(mode: Int);
def CompositingMode.getBlending(): Int;
def CompositingMode.setAlphaThreshold(threshold: Float);
def CompositingMode.getAlphaThreshold(): Float;
def CompositingMode.setAlphaWriteEnable(enable: Bool);
def CompositingMode.isAlphaWriteEnabled(): Bool;
def CompositingMode.setColorWriteEnable(enable: Bool);
def CompositingMode.isColorWriteEnabled(): Bool;
def CompositingMode.setDepthWriteEnable(enable: Bool);
def CompositingMode.isDepthWriteEnabled(): Bool;
def CompositingMode.setDepthTestEnable(enable: Bool);
def CompositingMode.isDepthTestEnabled(): Bool;
def CompositingMode.setDepthOffset(factor: Float, units: Float);
def CompositingMode.getDepthOffsetFactor(): Float;
def CompositingMode.getDepthOffsetUnits(): Float;
