package models;

public class RawModel {

	private int vaoID; // ID of the VAO
	private int vertexCount; // number of vertex of the model
	
	public RawModel(int vaoID, int vertexCount) {
		this.vaoID = vaoID;
		this.vertexCount = vertexCount;
	}
	
	public int getVaoID() {
		return vaoID;
	}

	public int getVertexCount() {
		return vertexCount;
	}
	
}
