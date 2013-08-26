use "Object3D" 

type Material < Object3D;

const MATERIAL_AMBIENT = 1024;
const MATERIAL_DIFFUSE = 2048;
const MATERIAL_EMISSIVE = 4096;
const MATERIAL_SPECULAR = 8192;

def Material.new();
def Material.setColor(target: Int, argb: Int);
def Material.getColor(target: Int): Int;
def Material.setShininess(shininess: Float);
def Material.getShininess(): Float;
def Material.setVertexColorTrackingEnable(enable: Bool);
def Material.isVertexColorTrackingEnabled(): Bool;
