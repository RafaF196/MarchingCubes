package entityRenderer;

import java.util.List;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.util.vector.Matrix4f;

import entities.Camera;
import entities.Entity;
import models.RawModel;
import toolbox.MatrixOps;

public class EntityRenderer {

	private EntityShader entityShader;

	public EntityRenderer(Matrix4f projectionMatrix) {
		entityShader = new EntityShader();
		entityShader.start();
		entityShader.loadProjectionMatrix(projectionMatrix);
		entityShader.connectTextureUnits();
		entityShader.stop();
	}

	public void render(List<Entity> entities, Camera camera) {
		entityShader.start();
		entityShader.loadViewMatrix(camera);
		for (Entity entity : entities) {
			RawModel model = entity.getModel();
			bindModelVao(model);
			loadModelMatrix(entity);
			GL11.glDrawElements(GL11.GL_TRIANGLES, model.getVertexCount(), GL11.GL_UNSIGNED_INT, 0);
			unbindVao();
		}
		entityShader.stop();
	}

	// binds the Vertex Array Object of a model and enables the vertex attributes stored in positions
	// 0 (vertex positions) and 2 (normal vectors)
	private void bindModelVao(RawModel rawModel) {
		GL30.glBindVertexArray(rawModel.getVaoID());
		GL20.glEnableVertexAttribArray(0);
		GL20.glEnableVertexAttribArray(2);
	}

	// unbinds the Vertex Array Object
	private void unbindVao() {
		GL20.glDisableVertexAttribArray(0);
		GL20.glDisableVertexAttribArray(2);
		GL30.glBindVertexArray(0);
	}

	private void loadModelMatrix(Entity entity) {
		Matrix4f transformationMatrix = MatrixOps.createTransformationMatrix(entity.getPosition(), 0, entity.getRotY(), 0,
				entity.getScale());
		entityShader.loadTransformationMatrix(transformationMatrix);
	}
	
	public void cleanUp() {
		entityShader.cleanUp();
	}

}
