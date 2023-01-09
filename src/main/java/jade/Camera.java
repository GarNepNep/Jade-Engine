package jade;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class Camera {
	private Matrix4f projectionMatrix, viewMatrix;
	private Vector2f position;
	public Camera(Vector2f position) {
		this.position = position;
		this.projectionMatrix = new org.joml.Matrix4f();
		this.viewMatrix = new org.joml.Matrix4f();
		adjustProjection();
	}
	
	public void adjustProjection() {
		projectionMatrix.identity(); // inits the identity matrix
		projectionMatrix.ortho(0f,32f * 40, 0f, 32f *21,0, 100f);
	}
	
	public Matrix4f getViewMatrix() {
		Vector3f cameraFront = new Vector3f(0f,0f,-1f);
		Vector3f cameraUp = new Vector3f(0f,1f,0f);
		this.viewMatrix.identity();
		viewMatrix.lookAt(new Vector3f(position.x, position.y, 20f),
				cameraFront.add(position.x, position.y, 0f),
				cameraUp);
		
		return this.viewMatrix;
	}
	
	public Matrix4f getProjectionMatrix(){
		return this.projectionMatrix;
	}
}
