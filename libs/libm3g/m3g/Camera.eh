use "Node"

type Camera < Node;

type Transform;

const CAMERA_GENERIC = 48;
const CAMERA_PARALLEL = 49;
const CAMERA_PERSPECTIVE = 50;

def Camera.new();
def Camera.setParallel(fovy: Float, aspectRatio: Float, near: Float, far: Float);
def Camera.setPerspective(fovy: Float, aspectRatio: Float, near: Float, far: Float);
def Camera.setGeneric(transform: Transform);
def Camera.getProjection(transform: Transform): Int;
