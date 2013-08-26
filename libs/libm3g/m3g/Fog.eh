use "Object3D"

type Fog < Object3D;

const FOG_EXPONENTIAL = 80;
const FOG_LINEAR = 81;

def Fog.new();
def Fog.setMode(mode: Int);
def Fog.getMode(): Int;
def Fog.setLinear(near: Float, far: Float);
def Fog.getNearDistance(): Float;
def Fog.getFarDistance(): Float;
def Fog.setDensity(density: Float);
def Fog.getDensity(): Float;
def Fog.setColor(rgb: Int);
def Fog.getColor(): Int;
