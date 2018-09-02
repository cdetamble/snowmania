package net.bplaced.therefactory.snowmania;

import com.badlogic.gdx.Game;

import net.bplaced.therefactory.snowmania.screens.GameScreen;

public class MyGdxGame extends Game {

	@Override
	public void create() {
		GameScreen screen = new GameScreen();
		setScreen(screen);
	}

}
