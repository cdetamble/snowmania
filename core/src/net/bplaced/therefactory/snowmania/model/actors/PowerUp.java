package net.bplaced.therefactory.snowmania.model.actors;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;

import net.bplaced.therefactory.snowmania.model.Board;
import net.bplaced.therefactory.snowmania.utils.MathUtils;

public class PowerUp {
	
	public float x;
	public float y;
	private TextureRegion region;
	private Board board;
	public Rectangle collisionArea;
	public boolean isGone = false;
	public PowerUpType type;
	public float lifetime; // in seconds
	private float scaleMin;
	private float scaleMax;
	private float period;
	private float rotate;
	private float spawntime;
	
	public enum PowerUpType {Coffee, Scarf, Wine, Pillow}
	
	public PowerUp(Board board, float x, float y, PowerUpType type, float lifetime) {
		this.board = board;
		this.type = type;
		this.lifetime = lifetime;
		region = board.game.atlas.findRegion(type.name().toLowerCase());
		this.x = (float) (x + board.cellsize/2.0 - region.getRegionWidth()/2.0);
		this.y =  (float) (y + board.cellsize/2.0 - region.getRegionHeight()/2.0);
		collisionArea = new Rectangle(this.x, this.y, region.getRegionWidth(), region.getRegionHeight());
		scaleMin = MathUtils.randomWithin(0.7f, 0.9f);
		scaleMax = MathUtils.randomWithin(0.9f, 1.1f);
		period = MathUtils.randomWithin(1.8f, 2.2f);
		rotate = MathUtils.randomWithin(4f, 6f);
		spawntime = board.game.elapsedTime;
	}

	public void update(float delta) {
		for (Snowman snowman : board.snowmen) {
			if (snowman.rectangleCollision.overlaps(collisionArea)) {
				// consumed by snowman
				snowman.consume(this);
				isGone = true;
				board.game.playSoundCollect();
			} else if (board.game.elapsedTime > spawntime + lifetime) {
				// lifetime exceeded
				isGone = true;
			}
		}
	}

	public void render(SpriteBatch batch) {
		batch.draw(region, x, y, 0, 0, region.getRegionWidth(), region.getRegionHeight(),
				MathUtils.oscilliate(board.game.elapsedTime, scaleMin, scaleMax, period),
				MathUtils.oscilliate(board.game.elapsedTime, scaleMin, scaleMax, -period),
				MathUtils.oscilliate(board.game.elapsedTime, -rotate, rotate, period));
	}

	public void debug(ShapeRenderer sr) {
		sr.rect(collisionArea.x, collisionArea.y, collisionArea.width, collisionArea.height);
	}

	
}
