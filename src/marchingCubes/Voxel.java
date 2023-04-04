package marchingCubes;

import org.lwjgl.util.vector.Vector3f;

public class Voxel {
	
	private static final Float scale = 0.1f;
	
	private Vector3f midpoint;
	private Integer mask = 0;
	private Vertex[] vertices = new Vertex[8];
	
	private Boolean hasNewVerts = false;
	private Vector3f[] newVerts = new Vector3f[12];
	
	// Generate a voxel from the first time
	public Voxel(Integer i, Integer j, Integer k, Float threshold, Integer GridSize, Vertex[] vertices) {
		Float xPos = (float) -((i - GridSize/2 + 1)/(GridSize*scale)); // for some reason the xPos is reversed
		Float yPos = (float) ((j - GridSize/2 + 1)/(GridSize*scale)); // (probably because I use a different
		Float zPos = (float) ((k - GridSize/2 + 1)/(GridSize*scale)); // coordinate system than intended)
		this.midpoint = new Vector3f(xPos, yPos, zPos);
		this.vertices = vertices;
		setVerticesPositions(GridSize);
		calculateMask();
		computeNewVerts(threshold);
	}
	
	// Recompute the values of the voxel with a new threshold
	public void recomputeNewThreshold(Float threshold) {
		hasNewVerts = false;
		for (int i = 0; i < 8; i++) {
			vertices[i].recalculateThreshold(threshold);
		}
		calculateMask();
		computeNewVerts(threshold);
	}
	
	// Assigns an (x,y,z) position to each vertex based on the middle point of the voxel
	public void setVerticesPositions(Integer GridSize) {
		Float offset = 0.5f/(GridSize*scale);
		vertices[0].setPosition(new Vector3f(midpoint.x - offset, midpoint.y - offset, midpoint.z - offset));
		vertices[1].setPosition(new Vector3f(midpoint.x - offset, midpoint.y - offset, midpoint.z + offset));
		vertices[2].setPosition(new Vector3f(midpoint.x - offset, midpoint.y + offset, midpoint.z - offset));
		vertices[3].setPosition(new Vector3f(midpoint.x - offset, midpoint.y + offset, midpoint.z + offset));
		vertices[4].setPosition(new Vector3f(midpoint.x + offset, midpoint.y - offset, midpoint.z - offset));
		vertices[5].setPosition(new Vector3f(midpoint.x + offset, midpoint.y - offset, midpoint.z + offset));
		vertices[6].setPosition(new Vector3f(midpoint.x + offset, midpoint.y + offset, midpoint.z - offset));
		vertices[7].setPosition(new Vector3f(midpoint.x + offset, midpoint.y + offset, midpoint.z + offset));
	}
	
	// Calculates the mask of the voxel
	public void calculateMask() {
		Integer maskValue = 0, powValue;
		for (int i = 0; i < 8; i++) {
			if (vertices[i].getInThreshold()) {
				powValue = 1;
				for (int j = 0; j < i; j++) powValue*=2;
				maskValue += powValue;
			}
		}
		this.mask = maskValue;
	}
	
	// Computes a vertex for the model if the conditions are fulfilled (the vertices of an edge
	// must have values above and below the threshold)
	public void computeNewVerts(Float threshold) {
		if (vertices[0].getInThreshold() != vertices[4].getInThreshold()) addNewVertex(0, threshold, 0, 4);
		if (vertices[4].getInThreshold() != vertices[5].getInThreshold()) addNewVertex(1, threshold, 4, 5);
		if (vertices[5].getInThreshold() != vertices[1].getInThreshold()) addNewVertex(2, threshold, 5, 1);
		if (vertices[1].getInThreshold() != vertices[0].getInThreshold()) addNewVertex(3, threshold, 1, 0);
		
		if (vertices[2].getInThreshold() != vertices[6].getInThreshold()) addNewVertex(4, threshold, 2, 6);
		if (vertices[6].getInThreshold() != vertices[7].getInThreshold()) addNewVertex(5, threshold, 6, 7);
		if (vertices[7].getInThreshold() != vertices[3].getInThreshold()) addNewVertex(6, threshold, 7, 3);
		if (vertices[3].getInThreshold() != vertices[2].getInThreshold()) addNewVertex(7, threshold, 3, 2);
		
		if (vertices[4].getInThreshold() != vertices[6].getInThreshold()) addNewVertex(8, threshold, 4, 6);
		if (vertices[5].getInThreshold() != vertices[7].getInThreshold()) addNewVertex(9, threshold, 5, 7);
		if (vertices[0].getInThreshold() != vertices[2].getInThreshold()) addNewVertex(10, threshold, 0, 2);
		if (vertices[1].getInThreshold() != vertices[3].getInThreshold()) addNewVertex(11, threshold, 1, 3);
	}
	
	// Adds a new vertex to given edge based on the values and positions of both vertices
	public void addNewVertex(Integer edge, Float threshold, Integer a, Integer b) {
		Vector3f posA = vertices[a].getPosition();
		Vector3f posB = vertices[b].getPosition();
		Float valueA = vertices[a].getValue();
		Float valueB = vertices[b].getValue();
		// Interpolation
		Float difference = Math.abs(valueA - valueB);
		Float factA = 1 - (Math.abs(valueA - threshold) / difference);
		Float factB = 1 - (Math.abs(valueB - threshold) / difference);
		Vector3f newVertex = new Vector3f(factA*posA.x + factB*posB.x, factA*posA.y + factB*posB.y,
				factA*posA.z + factB*posB.z);		
		newVerts[edge] = newVertex;
		hasNewVerts = true; // This voxel will affect the resulting model
	}
	
	public Integer getMask() {
		return mask;
	}
	
	public Boolean hasNewVertices() {
		return hasNewVerts;
	}
	
	public Vector3f[] getNewVertices() {
		return newVerts;
	}

}
