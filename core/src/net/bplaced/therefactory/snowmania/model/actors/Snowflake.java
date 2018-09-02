package net.bplaced.therefactory.snowmania.model.actors;

import net.bplaced.therefactory.snowmania.config.Constants;
import net.bplaced.therefactory.snowmania.utils.MathUtils;
import net.bplaced.therefactory.snowmania.screens.GameScreen;

public class Snowflake {

	private float originX, velocityY;
	private GameScreen screen;

	public Snowflake(int x, int y,float velocityY, GameScreen screen) {
		this.x =x;
		this.y=y;
		this.screen = screen;
		this.originX = x;
		this.velocityY = velocityY;
	}

	public float x, y;

	public void update(float delta) {
		x = MathUtils.oscilliate(screen.game.elapsedTime, originX-2f, originX+2f, 10f);
		y += velocityY;
		if (y < -10) {
			y = Constants.WINDOW_HEIGHT;
		}
	}
	
	
}
