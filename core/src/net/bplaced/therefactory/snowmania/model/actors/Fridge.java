package net.bplaced.therefactory.snowmania.model.actors;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;

import net.bplaced.therefactory.snowmania.model.Board;
import net.bplaced.therefactory.snowmania.config.Constants.PlayerColor;
import net.bplaced.therefactory.snowmania.config.Constants.PlayerDirection;

public class Fridge {

	public PlayerColor color;
	public float x;
	public float y;
	private Board board;
	public Rectangle bounds;
	public boolean isMoving = false; // snowmen cant enter while moving
	private TextureRegion texturePre;
	private TextureRegion texturePost;
	public PlayerDirection direction;
	public boolean isOpen;
	public Snowman enteringSnowman;


	public Fridge(Board board, float x, float y, PlayerColor color, PlayerDirection direction) {
		this.board = board;
		this.color = color;
		this.direction = direction;
		this.x = x;
		this.y = y;
		if (direction.equals(PlayerDirection.Left)) {
			//this.x -= 8;
		}
		
		this.bounds = new Rectangle(x, y, board.cellsize, board.cellsize);

		this.texturePre = board.game.atlas.findRegion(color.name().toLowerCase() + "_fridge_" + direction.name().toLowerCase() + "_pre");
		this.texturePost = board.game.atlas.findRegion(color.name().toLowerCase() + "_fridge_" + direction.name().toLowerCase() + "_post");
	}
	
	public void update(float delta) {
		if (isOpen) {
			if (enteringSnowman == null || enteringSnowman.state.equals(Snowman.State.Melted)) {
				isOpen = false;
				enteringSnowman = null;
			}
		}
	}

	public void debug(ShapeRenderer sr) {
		switch (color) {
		case Red:
			sr.setColor(Color.RED);
			break;
		case Blue:
			sr.setColor(Color.BLUE);
			break;
		case Green:
			sr.setColor(Color.GREEN);
			break;
		default:
			break;
		}
		sr.rect(x, y, board.cellsize, board.cellsize);
	}

	public void renderPreSnowman(SpriteBatch batch) {
		batch.draw(texturePre, x, y);
	}

	public void renderPostSnowman(SpriteBatch batch) {
		batch.draw(texturePost, x , y);
	}
	
	public void renderPreSnowman(ShapeRenderer sr) {
		switch (direction) {
		case Down:
			if (isOpen) {
				sr.setColor(Color.valueOf("A02E3F"));
				sr.rect(x - 23, y, 23, 26);
			} else {
				sr.setColor(Color.valueOf("BC364A"));
				sr.rect(x, y, 23, 26);
			}
			break;
		case Left:
			if (isOpen) {
				sr.setColor(Color.valueOf("3B54CE"));
				sr.triangle(x + 13, y - 13, x + 13, y, x + 23, y);
				sr.rect(x + 13, y , 10, 14);
				sr.triangle(x + 13, y + 14, x + 23, y + 14, x + 23, y + 26);
			} else {
				sr.setColor(Color.valueOf("2B3B96"));
				sr.triangle(x + 23, y, x + 23, y + 8, x +8 + 23, y +8);
			}
			break;
		case Right:
			if (isOpen) {
				// 3FA02E
				sr.setColor(Color.valueOf("3FA02E"));
				sr.triangle(x + 8, y + 34, x + 20, y + 34, x + 20, y + 43);
			} else {
			}
			break;
		}
	}
	
	public void renderPostSnowman(ShapeRenderer sr) {
		switch (direction) {
		case Left:
			if (isOpen) {
				sr.setColor(Color.valueOf("3B54CE"));
				sr.triangle(x + 13, y - 13, x + 13, y, x + 23, y);
				sr.rect(x + 13, y , 10, 14);
				sr.triangle(x + 13, y + 14, x + 23, y + 14, x + 23, y + 26);
			} else {
				sr.setColor(Color.valueOf("2B3B96"));
				sr.triangle(x + 23, y, x + 23, y + 8, x +8 + 23, y +8);
			}
			break;
		case Right:
			if (isOpen) {
				// 3FA02E
				sr.setColor(Color.valueOf("3FA02E"));
				sr.triangle(x + 8, y + 34, x + 20, y + 34, x + 20, y + 43);
			} else {
			}
			break;
		}
	}

}
