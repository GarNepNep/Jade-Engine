package jade;

import org.joml.Vector2f;
import org.lwjgl.BufferUtils;
import renderer.Shader;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.ARBVertexArrayObject.glBindVertexArray;
import static org.lwjgl.opengl.GL20.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL20.GL_ELEMENT_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL20.GL_FLOAT;
import static org.lwjgl.opengl.GL20.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL20.GL_TRIANGLES;
import static org.lwjgl.opengl.GL20.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL20.glBindBuffer;
import static org.lwjgl.opengl.GL20.glBufferData;
import static org.lwjgl.opengl.GL20.glDisableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glDrawElements;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glGenBuffers;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

public class LevelEditorScene extends Scene {
	private final float[] vertexArray = {
			// position         //colour
			100.5f, 0.5f, 0f, 1f, 0f, 0f, 1f, // bottom right    0
			0.5f, 100.5f, 0f, 0f, 1f, 0f, 1f,    // top left     1
			100.5f, 100.5f, 0f, 0f, 0f, 1f, 1f,    // top right    2
			0f, 0f, 0f, 1f, 1f, 0f, 1f,    // bottom left  3
	};
	
	// IMP: must be in anti-clockwise order
	private final int[] elementArray = {
			/**
			 *  x2   x1
			 *
			 *       x3
			 *  x1
			 *
			 *  x2    x3
			*/
//
			2, 1, 0,
			0, 1, 3
	};
	private int vertexID, fragmentID, shaderProgram;
	private int vaoID, vboID, eboID;
	
	private Shader defaultShader;
	
	public LevelEditorScene() {
	}
	
	// compile and link shaders
	@Override
	public void init() {
		this.camera = new Camera(new Vector2f(0f));
		defaultShader = new Shader("assets/shaders/default.glsl");
		defaultShader.compile();
		
		// Generate VAO VBO EBO buffer objects and send to GPU
		vaoID = glGenVertexArrays();
		glBindVertexArray(vaoID);
		
		// create a float buffer of vertices
		FloatBuffer vertexBuffer =
				BufferUtils.createFloatBuffer(vertexArray.length);
		vertexBuffer.put(vertexArray).flip();
		
		vboID = glGenBuffers();
		
		glBindBuffer(GL_ARRAY_BUFFER, vboID);
		glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);
		
		// Create the indices and upload
		IntBuffer elementBuffer =
				BufferUtils.createIntBuffer(elementArray.length);
		elementBuffer.put(elementArray).flip();
		
		eboID = glGenBuffers();
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, eboID);
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, elementBuffer, GL_STATIC_DRAW);
		
		// add vertex attribute pointers
		int positionsSize = 3;
		int colourSize = 4;
		int floatSizeBytes = 4;
		int vertexSizeBytes = (positionsSize + colourSize) * floatSizeBytes;
		glVertexAttribPointer(0, positionsSize, GL_FLOAT,
				false, vertexSizeBytes, 0);
		glEnableVertexAttribArray(0);
		
		glVertexAttribPointer(1, colourSize, GL_FLOAT,
				false, vertexSizeBytes, positionsSize * colourSize);
		glEnableVertexAttribArray(1);
		
	}
	
	@Override
	public void update(float dt) {
		// Bind the shader program
		defaultShader.use();
		defaultShader.uploadMat4f("uProjection", camera.getProjectionMatrix());
		defaultShader.uploadMat4f("uView", camera.getViewMatrix());
		
		
		//bind the VAO
		glBindVertexArray(vaoID);
		
		// Enable the vertex attribute
		glEnableVertexAttribArray(0);
		glEnableVertexAttribArray(1);
		
		glDrawElements(GL_TRIANGLES, elementArray.length, GL_UNSIGNED_INT, 0);
		
		// unbind
		// Enable the vertex attribute
		glDisableVertexAttribArray(0);
		glDisableVertexAttribArray(1);
		
		glBindVertexArray(0);
		
		defaultShader.detatch();
	}
}
