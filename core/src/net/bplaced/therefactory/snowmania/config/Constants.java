package net.bplaced.therefactory.snowmania.config;

public class Constants {
    public static final float increaseDifficultyEverySeconds = 10;
    public static final int repositionFridgesEverySeconds = 27;
    public static final int maxNumCollisions = 2;
    public static final int numSnowflakes = 200;
    public static int WINDOW_WIDTH = 384;
    public static int WINDOW_HEIGHT = 216;

    public static String VERSION = "1.1";

    public enum PlayerColor {
        Red, Blue, Green
    }

    public enum PlayerDirection {
        Up, Right, Down, Left
    }

    public enum Orientation {
        Horizontal, Vertical
    }

    public static Orientation[] Orientations = new Orientation[]{Orientation.Vertical, Orientation.Horizontal,
            Orientation.Vertical, Orientation.Horizontal};
}
