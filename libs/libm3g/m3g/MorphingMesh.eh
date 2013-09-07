use "Mesh"

type MorphingMesh < Mesh;

def MorphingMesh.new(base: VertexBuffer, targets: [VertexBuffer], submeshes: [IndexBuffer], appearances: [Appearance]);
def MorphingMesh.getMorphTarget(index: Int): VertexBuffer;
def MorphingMesh.getMorphTargetCount(): Int;
def MorphingMesh.setWeights(weights: [Float]);
def MorphingMesh.getWeights(weights: [Float]);
