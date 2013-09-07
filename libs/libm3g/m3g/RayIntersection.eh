type RayIntersection < Any; 

def RayIntersection.new();
def RayIntersection.getIntersected(): Node;
def RayIntersection.getRay(ray: [Float]);
def RayIntersection.getDistance(): Float;
def RayIntersection.getSubmeshIndex(): Int;
def RayIntersection.getTextureS(index: Int): Float;
def RayIntersection.getTextureT(index: Int): Float;
def RayIntersection.getNormalX(): Float;
def RayIntersection.getNormalY(): Float;
def RayIntersection.getNormalZ(): Float;
