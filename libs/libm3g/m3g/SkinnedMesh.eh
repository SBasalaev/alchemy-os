use "Mesh"

type SkinnedMesh < Mesh;

def SkinnedMesh.new(vertices: VertexBuffer, submeshes: [IndexBuffer], appearances: [Appearance], skeleton: Group);
def SkinnedMesh.getSkeleton(): Group;
def SkinnedMesh.addTransform(bone: Node, weight: Int, firstVertex: Int, numVertices: Int);
def SkinnedMesh.getBoneTransform(bone: Node, transform: Transform);
def SkinnedMesh.getBoneVertices(bone: Node, indices: [Int], weights: [Float]): Int;
