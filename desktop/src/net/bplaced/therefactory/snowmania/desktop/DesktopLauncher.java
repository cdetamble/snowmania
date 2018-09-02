package net.bplaced.therefactory.snowmania.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

import net.bplaced.therefactory.snowmania.config.Constants;
import net.bplaced.therefactory.snowmania.MyGdxGame;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.x = 1920;
		float scale = 3f;
		config.width = (int) (Constants.WINDOW_WIDTH * scale);
		config.height = (int) (Constants.WINDOW_HEIGHT * scale);
		config.vSyncEnabled = true;
		config.title = "Snowmania - Ludum Dare 40 Compo";
		//config.fullscreen= true;
		new LwjglApplication(new MyGdxGame(), config);
	}
}
