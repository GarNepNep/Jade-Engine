package renderer;

import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.lwjgl.opengl.GL20.glUniform1f;
import static org.lwjgl.opengl.GL20.glUniform1i;
import static org.lwjgl.opengl.GL20.glUniform2f;
import static org.lwjgl.opengl.GL20.glUniform3f;
import static org.lwjgl.opengl.GL20.glUniform4f;
import static org.lwjgl.opengl.GL20.glUniformMatrix3fv;
import static org.lwjgl.opengl.GL20C.GL_COMPILE_STATUS;
import static org.lwjgl.opengl.GL20C.GL_FALSE;
import static org.lwjgl.opengl.GL20C.GL_FRAGMENT_SHADER;
import static org.lwjgl.opengl.GL20C.GL_INFO_LOG_LENGTH;
import static org.lwjgl.opengl.GL20C.GL_LINK_STATUS;
import static org.lwjgl.opengl.GL20C.GL_VERTEX_SHADER;
import static org.lwjgl.opengl.GL20C.glAttachShader;
import static org.lwjgl.opengl.GL20C.glCompileShader;
import static org.lwjgl.opengl.GL20C.glCreateProgram;
import static org.lwjgl.opengl.GL20C.glCreateShader;
import static org.lwjgl.opengl.GL20C.glGetProgramInfoLog;
import static org.lwjgl.opengl.GL20C.glGetProgrami;
import static org.lwjgl.opengl.GL20C.glGetShaderInfoLog;
import static org.lwjgl.opengl.GL20C.glGetShaderi;
import static org.lwjgl.opengl.GL20C.glGetUniformLocation;
import static org.lwjgl.opengl.GL20C.glLinkProgram;
import static org.lwjgl.opengl.GL20C.glShaderSource;
import static org.lwjgl.opengl.GL20C.glUniformMatrix4fv;
import static org.lwjgl.opengl.GL20C.glUseProgram;

public class Shader {
	
	private int shaderProgramID;
	private boolean beingUsed = false;
	private String vertexSource;
	private String fragmentSource;
	private final String filepath;
	
	public Shader(String filepath) {
		this.filepath = filepath;
		
		try {
			String source = new String(Files.readAllBytes(Paths.get(filepath)));
			
			String[] splitString = source.split("(#type)( )+([a-zA-Z]+)");
			
			int index = source.indexOf("#type") + 6;
			int eol = source.indexOf("\r\n", index);
			
			String firstPattern = source.substring(index, eol).trim();
			
			index = source.indexOf("#type", eol) + 6;
			eol = source.indexOf("\r\n", index);
			
			String secondPattern = source.substring(index, eol).trim();
			
			if (firstPattern.equals("vertex")) {
				vertexSource = splitString[1];
			} else if (firstPattern.equals("fragment")) {
				fragmentSource = splitString[1];
			} else {
				throw new IOException("Unexpected token -" + firstPattern + "- in ");
			}
			
			if (secondPattern.equals("vertex")) {
				vertexSource = splitString[2];
			} else if (secondPattern.equals("fragment")) {
				fragmentSource = splitString[2];
			} else {
				throw new IOException("Unexpected token -" + firstPattern + "- in ");
			}
			System.out.printf("%s%n%s%n", fragmentSource, vertexSource);
		} catch (IOException e) {
			e.printStackTrace();
			assert false : "Error: Could not open file for shader '" + filepath + "'";
		}
	}
	
	public void compile() {
		// ####
		//  compile and link shaders
		// ####
		int vertexID, fragmentID;
		
		// load and compile vertex shader;
		
		vertexID = glCreateShader(GL_VERTEX_SHADER);
		
		//pass shader src code to GPU
		glShaderSource(vertexID, vertexSource);
		glCompileShader(vertexID);
		
		//check for errors in complication
		int success = glGetShaderi(vertexID, GL_COMPILE_STATUS);
		
		if (success == GL_FALSE) {
			int len = glGetShaderi(vertexID, GL_INFO_LOG_LENGTH);
			System.out.printf("Error: '%s'\n\tVertex shader compilation failed.", filepath);
			System.out.println(glGetShaderInfoLog(vertexID, len));
			assert false : "";
		}
		fragmentID = glCreateShader(GL_FRAGMENT_SHADER);
		
		//pass shader src code to GPU
		glShaderSource(fragmentID, fragmentSource);
		glCompileShader(fragmentID);
		
		//check for errors in complication
		success = glGetShaderi(vertexID, GL_COMPILE_STATUS);
		
		if (success == GL_FALSE) {
			int len = glGetShaderi(fragmentID, GL_INFO_LOG_LENGTH);
			System.out.printf("Error: '%s'\n\tFragment compilation failed.", filepath);
			System.out.println(glGetShaderInfoLog(fragmentID, len));
			assert false : "";
		}
		
		// link shaders and check for errors
		shaderProgramID = glCreateProgram();
		glAttachShader(shaderProgramID, vertexID);
		glAttachShader(shaderProgramID, fragmentID);
		glLinkProgram(shaderProgramID);
		
		success = glGetProgrami(shaderProgramID, GL_LINK_STATUS);
		
		if (success == GL_FALSE) {
			int len = glGetShaderi(shaderProgramID, GL_INFO_LOG_LENGTH);
			System.out.printf("Error: '%s'\n\tLinking shader compilation failed.", filepath);
			System.out.println(glGetProgramInfoLog(shaderProgramID, len));
			assert false : "";
		}
		
	}
	
	public void use() {
		if (!beingUsed) {
			glUseProgram(shaderProgramID);
			beingUsed = true;
		}
	}
	
	public void detatch() {
		glUseProgram(0);
		beingUsed = false;
	}
	
	public void uploadMat4f(String varName, Matrix4f mat4) {
		int varLocation = glGetUniformLocation(shaderProgramID, varName);
		use();
		FloatBuffer matBuffer = BufferUtils.createFloatBuffer(16);
		mat4.get(matBuffer); // uploads buffer
		glUniformMatrix4fv(varLocation, false, matBuffer);
	}
	
	public void uploadVec4(String varName, Vector4f vec) {
		int varLocation = glGetUniformLocation(shaderProgramID, varName);
		use();
		glUniform4f(varLocation, vec.x, vec.y, vec.z, vec.w);
	}
	
	public void uploadFloat(String varName, float val) {
		int varLocation = glGetUniformLocation(shaderProgramID, varName);
		use();
		glUniform1f(varLocation, val);
	}
	public void uploadInt(String varName, int val) {
		int varLocation = glGetUniformLocation(shaderProgramID, varName);
		use();
		glUniform1i(varLocation, val);
	}
	public void uploadMat3f(String varName, Matrix3f mat) {
		int varLocation = glGetUniformLocation(shaderProgramID, varName);
		use();
		FloatBuffer fb = BufferUtils.createFloatBuffer(9);
		mat.get(fb);
		glUniformMatrix3fv(varLocation, false, fb);
	}
	
	public void uploadVec2f(String varName, Vector2f val) {
		int varLocation = glGetUniformLocation(shaderProgramID, varName);
		use();
		glUniform2f(varLocation, val.x, val.y);
	}
	public void uploadVec3f(String varName, Vector3f val) {
		int varLocation = glGetUniformLocation(shaderProgramID, varName);
		use();
		glUniform3f(varLocation, val.x, val.y, val.z);
	}
}
