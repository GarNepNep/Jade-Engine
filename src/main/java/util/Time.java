package util;

public class Time {
    public static float timeStarted = System.nanoTime();

    // Returns the time since the applications' started
    public static float getTime() {
        return (float) ((System.nanoTime() - timeStarted) * 1E-9);
    }
}
