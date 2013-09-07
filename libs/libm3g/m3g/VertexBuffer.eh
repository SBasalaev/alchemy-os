use "Object3D"

type VertexBuffer < Object3D;

type VertexArray;

def VertexBuffer.new();
def VertexBuffer.getVertexCount(): Int;
def VertexBuffer.setPositions(positions: VertexArray, scale: Float, bias: [Float]);
def VertexBuffer.setTexCoords(index: Int, texCoords: VertexArray, scale: Float, bias: [Float]);
def VertexBuffer.setNormals(normals: VertexArray);
def VertexBuffer.setColors(colors: VertexArray);
def VertexBuffer.getPositions(scaleBias: [Float]): VertexArray;
def VertexBuffer.getTexCoords(index: Int, scaleBias: [Float]): VertexArray;
def VertexBuffer.getNormals(): VertexArray;
def VertexBuffer.getColors(): VertexArray;
def VertexBuffer.setDefaultColor(argb: Int);
def VertexBuffer.getDefaultColor(): Int;
