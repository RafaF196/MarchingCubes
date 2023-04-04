package marchingCubes;

import org.lwjgl.util.vector.Vector3f;

public class Vertex {
	
	private Vector3f position;
	private Float value;
	private Boolean inThreshold;
	
	// Generate a vertex for the first time
	public Vertex(Float value, Float threshold) {
		this.value = value;
		this.inThreshold = (value > threshold);
	}
	
	// Actualize whether this vertex is above or below the threshold value
	public void recalculateThreshold(Float threshold) {
		this.inThreshold = (value > threshold);
	}
	
	public void setPosition(Vector3f position) {
		this.position = position;
	}
	
	public Vector3f getPosition() {
		return position;
	}
	
	public Float getValue() {
		return value;
	}
	
	public Boolean getInThreshold() {
		return inThreshold;
	}

}
