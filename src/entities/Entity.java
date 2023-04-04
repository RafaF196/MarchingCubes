package entities;

import models.RawModel;

import org.lwjgl.util.vector.Vector3f;

public class Entity {

	private RawModel model;
	private Vector3f position;
	private float rotY;
	private float scale;

	public Entity(RawModel model, Vector3f position, float rotY, float scale) {
		this.model = model;
		this.position = position;
		this.rotY = rotY;
		this.scale = scale;
	}

	public RawModel getModel() {
		return model;
	}

	public Vector3f getPosition() {
		return position;
	}

	public float getRotY() {
		return rotY;
	}

	public float getScale() {
		return scale;
	}

}
