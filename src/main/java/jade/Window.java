package jade;

import org.lwjgl.Version;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import util.Time;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Window {
    private final int width;
    private final int height;
    private final String title;
    private static Window window = null;
    private long glfwWindow;
    private static Scene currentScene;
    // The background colours.
    public float r,g,b,a;


    private Window(){
       this.width = 1920;
       this.height = 1080;
       this.r = 1;
       this.g = 1;
       this.b = 1;
       this.a = 0;
       this.title = "Mario";
    }

    public static void changeScene(int newScene){
        switch (newScene){
            case 0:
                currentScene = new LevelEditorScene();
                currentScene.init();
                break;
            case 1:
                currentScene = new LevelScene();
                currentScene.init();
                break;
            default:
                assert false : "Unknown scene '" + newScene + "'";
        }
    }
    public static Window get(){
        if(Window.window == null){
            Window.window = new Window();
        }
        return Window.window;
    }
    public void run() {
        System.out.println("LWJGL is " + Version.getVersion());
        init();
        loop();

        // Free the memory
        glfwFreeCallbacks(glfwWindow);
        glfwDestroyWindow(glfwWindow);

        // Terminate GLFW and free error call back
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }
    public void init(){
        GLFWErrorCallback.createPrint(System.err).set();
        if(!glfwInit()){
            throw new IllegalStateException("unable to init GLFW");
        }

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        glfwWindowHint(GLFW_MAXIMIZED, GLFW_TRUE);

        //Create the window
        glfwWindow = glfwCreateWindow(this.width, this.height, this.title, NULL, NULL);
        if(glfwWindow == NULL){
            throw new IllegalStateException("failed to create glfwWindow");
        }

        glfwSetCursorPosCallback(glfwWindow, MouseListener::mousePosCallback);
        glfwSetMouseButtonCallback(glfwWindow, MouseListener::mouseButtonCallback);
        glfwSetScrollCallback(glfwWindow, MouseListener::mouseScrollCallback);
        glfwSetKeyCallback(glfwWindow, KeyListener::keyCallback);

        // Make the OpenGL context Current
        glfwMakeContextCurrent(glfwWindow);

        // Enable V-sync
        glfwSwapInterval(1);

        // Make the window visible
        glfwShowWindow(glfwWindow);

        // This line is critical for LWJGL's interoperation with GLFW's
        // OpenGL context, or any context that is managed externally.
        // LWJGL detects the context that is current in the current thread,
        // creates the GLCapabilities instance and makes the OpenGL
        // bindings available for use.
        GL.createCapabilities();
        Window.changeScene(0);
    }

    public void loop(){
        float beginTime = Time.getTime();

        float dt = -1f;
        float endTime;
        while(!glfwWindowShouldClose(glfwWindow)){
            glfwPollEvents();

            glClearColor(r, g, b, a);
            glClear(GL_COLOR_BUFFER_BIT); // flush the colour to the screen
            if(dt >= 0){
                currentScene.update(dt);
            }
            glfwSwapBuffers(glfwWindow);

            // System.out.println(dt);
            //System.out.println(1/dt);
            endTime = Time.getTime();
            dt = endTime - beginTime;
            beginTime = endTime;
        }
    }

}
