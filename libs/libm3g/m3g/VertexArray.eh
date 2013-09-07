use "Object3D"

type VertexArray < Object3D;

def VertexArray.new(numVertices: Int, numComponents: Int, componentSize: Int);
def VertexArray.set(firstVertex: Int, numVertices: Int, values: Array);
def VertexArray.getVertexCount(): Int;
def VertexArray.getComponentCount(): Int;
def VertexArray.getComponentType(): Int;
def VertexArray.get(firstVertex: Int, numVertices: Int, values: Array);
