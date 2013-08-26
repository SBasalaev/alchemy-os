use "Node"

type Mesh < Node;

type Appearance;
type IndexBuffer;
type VertexBuffer;

def Mesh.new(vertices: VertexBuffer, submeshes: [IndexBuffer], appearances: [Appearance]);
def Mesh.setAppearance(index: Int, appearance: Appearance);
def Mesh.getAppearance(index: Int): Appearance;
def Mesh.getIndexBuffer(index: Int): IndexBuffer;
def Mesh.getVertexBuffer(): VertexBuffer;
def Mesh.getSubmeshCount(): Int;
