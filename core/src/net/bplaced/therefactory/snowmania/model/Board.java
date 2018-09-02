package net.bplaced.therefactory.snowmania.model;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;

import net.bplaced.therefactory.snowmania.config.Constants;
import net.bplaced.therefactory.snowmania.config.Constants.PlayerDirection;
import net.bplaced.therefactory.snowmania.model.actors.Fridge;
import net.bplaced.therefactory.snowmania.model.actors.PowerUp;
import net.bplaced.therefactory.snowmania.model.actors.PowerUp.PowerUpType;
import net.bplaced.therefactory.snowmania.model.actors.Snowman;
import net.bplaced.therefactory.snowmania.model.actors.Snowman.State;
import net.bplaced.therefactory.snowmania.utils.Comparators;
import net.bplaced.therefactory.snowmania.utils.MathUtils;
import net.bplaced.therefactory.snowmania.utils.Tuple;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static net.bplaced.therefactory.snowmania.config.Constants.PlayerDirection.Down;
import static net.bplaced.therefactory.snowmania.config.Constants.PlayerDirection.Left;
import static net.bplaced.therefactory.snowmania.config.Constants.PlayerDirection.Right;

public class Board {

    public int cellsize = 22; // in pixels
    public int numColumns = 14; // corresponds to x
    public int numRows = 5; // corresponds to y

    // origin is at lower left corner, all following values are absolute pixel
    // coordinates
    public float width = cellsize * numColumns;
    public float height = cellsize * numRows;
    public float originX = (Constants.WINDOW_WIDTH - width) / 2.0f;
    public float originY = (float) (cellsize / 2.0);
    public float endX = originX + width;
    public float endY = originY + height;
    public Rectangle bounds = new Rectangle(originX, originY, width, height);

    private SpriteBatch batch;
    private ShapeRenderer sr;
    private OrthographicCamera camera;

    public List<Tuple> collisions = new ArrayList<Tuple>();
    public List<Snowman> snowmen = new ArrayList<Snowman>();
    public List<Fridge> fridges = new ArrayList<Fridge>();
    public List<PowerUp> powerUps = new ArrayList<PowerUp>();
    public Game game;
    private List<Snowman> melted = new ArrayList<Snowman>();
    private Color gridColor = Color.valueOf("#A88875");

    public Board(Game game) {
        this.game = game;
        this.batch = game.screen.batch;
        this.sr = game.screen.sr;
        this.camera = game.screen.camera;
    }

    public void reset() {
        collisions.clear();
        snowmen.clear();
        fridges.clear();
        powerUps.clear();

        //spawnSnowmanIntoCell(1, 2, PlayerDirection.Left, PlayerColor.Blue, 0.5f);
        //spawnSnowmanIntoCell(7, 4, PlayerDirection.Right, PlayerColor.Red, 0.5f);
        //spawnSnowmanIntoCell(12, 2, PlayerDirection.Right, PlayerColor.Green, 0.5f);

        //spawnPowerUpInCell(7, 1, PowerUpType.Coffee, 1000);

        spawnFridgeToCell(7, numRows, Constants.PlayerColor.Red, Down); // top middle
        spawnFridgeToCell(-1, 2, Constants.PlayerColor.Blue, Left); // left middle
        spawnFridgeToCell(numColumns, 2, Constants.PlayerColor.Green, Right); // right middle
    }

    public void spawnPowerUpInCell(int x, int y, PowerUpType type, float lifetime) {
        powerUps.add(new PowerUp(this, originX + x * cellsize, originY + y * cellsize, type, lifetime));
    }

    public void spawnFridgeToCell(int x, int y, Constants.PlayerColor color, PlayerDirection direction) {
        Fridge e = new Fridge(this, originX + x * cellsize, originY + y * cellsize, color, direction);
        fridges.add(e);
        Collections.sort(fridges, Comparators.SortFridgesDescByYpos);
    }

    public void spawnSnowmanIntoCell(int x, int y, PlayerDirection direction, Constants.PlayerColor color, float walkingSpeed) {
        Snowman s = new Snowman(this, originX + x * cellsize, originY + y * cellsize, direction, color, walkingSpeed);
        snowmen.add(s);
    }

    public void update(float delta) {
        for (PowerUp powerUp : powerUps) {
            powerUp.update(delta);
        }
        for (int i = 0; i < powerUps.size(); i++) {
            if (powerUps.get(i).isGone) {
                powerUps.remove(i);
            }
        }

        for (Snowman snowman : snowmen) {
            snowman.update(delta);
        }
        while (!collisions.isEmpty()) {
            final Tuple collision = collisions.get(0);
            ((Snowman) collision.first).resolveCollisionWith((Snowman) collision.second);
            collisions.remove(collision);

            for (int i = 0; i < collisions.size(); i++) {
                Tuple otherCollision = collisions.get(i);
                if (collision.first.equals(otherCollision.second)
                        && collision.second.equals(otherCollision.first)) {
                    collisions.remove(otherCollision);
                }
            }
        }

        melted.clear();
        for (Snowman t : snowmen) {
            if (t.state.equals(Snowman.State.Melted)) {
                melted.add(t);
            }
        }

        if (!melted.isEmpty()) {
            snowmen.removeAll(melted);
            if (snowmen.isEmpty()) {

                // checkGameOverCondition
                boolean atLeastOneFridgedSnowman = false;
                for (Snowman o : melted) {
                    if (o.fridged) {
                        atLeastOneFridgedSnowman = true;
                        break;
                    }
                }
                if (!atLeastOneFridgedSnowman) {
                    game.state = Game.GameState.GameOver;
                }
            }
        }

        // sort by y for correct overlaying during rendering
        Collections.sort(snowmen, Comparators.SortSnowmanDescByYpos);

        for (Fridge fridge : fridges) {
            fridge.update(delta);
        }
    }

    public void render() {
        if (!game.state.equals(Game.GameState.Intro)) {
            sr.setProjectionMatrix(camera.combined);
            sr.begin(ShapeRenderer.ShapeType.Line);
            sr.setColor(gridColor);
            game.board.renderGrid();

            sr.setColor(Color.DARK_GRAY);
            sr.set(ShapeRenderer.ShapeType.Filled);
            for (Snowman snowman : snowmen) {
                if (snowman.state.equals(State.Spawning)) {
                    sr.ellipse(snowman.x + 2, snowman.spawnY , cellsize - 4, 4);
                }
            }

            sr.end();
        }

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        for (PowerUp fruit : powerUps) {
            fruit.render(batch);
        }
        for (Fridge exit : fridges) {
            exit.renderPreSnowman(batch);
        }
        batch.end();

        sr.setProjectionMatrix(camera.combined);
        sr.begin(ShapeRenderer.ShapeType.Filled);
        for (Fridge fridge : fridges) {
            fridge.renderPreSnowman(sr);
        }
        sr.end();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        for (Snowman snowman : snowmen) {
            if (!snowman.state.equals(State.Spawning))
                snowman.render(batch);
        }
        for (Fridge exit : fridges) {
            exit.renderPostSnowman(batch);
        }
        for (Snowman snowman : snowmen) {
            if (snowman.state.equals(State.Spawning))
                snowman.render(batch);
        }
        batch.end();

        sr.setProjectionMatrix(camera.combined);
        sr.begin(ShapeRenderer.ShapeType.Filled);
        for (Fridge fridge : fridges) {
            fridge.renderPostSnowman(sr);
        }
        sr.end();
    }

    private void debug() {
        sr.setProjectionMatrix(camera.combined);
        sr.begin(ShapeRenderer.ShapeType.Line);
        sr.setColor(Color.WHITE);

        renderGrid();

        // fridges
        sr.setColor(Color.PURPLE);
        for (Fridge exit : fridges) {
            exit.debug(sr);
        }

        // snowmen
        //for (Snowman snowman : snowmen) {
        //	snowman.debug(sr);
        //}

        //for (PowerUp fruit: powerUps) {
        //	fruit.debug(sr);
        //}

        sr.end();
    }

    public void renderGrid() {
        for (float x = originX; x < originX + width; x += cellsize) {
            for (float y = originY; y < originY + height; y += cellsize) {
                sr.rect(x, y, cellsize, cellsize);
            }
        }
    }

    public void repositionFridges() {
        for (Fridge fridge : fridges) {
            if (fridge.isOpen) {
                continue;
            }
            int random = MathUtils.randomWithin(0, 100);
            if (random > 50) {
                switch (fridge.direction) {
                    case Down:
                        fridge.x += cellsize;
                        if (fridge.x > (numColumns + 1) * cellsize) {
                            fridge.x = originX;
                        }
                        break;
                    case Right:
                        fridge.y -= cellsize;
                        if (fridge.y < 0) {
                            fridge.y = endY;
                        }
                        break;
                    case Left:
                        fridge.y += cellsize;
                        if (fridge.y > numRows * cellsize) {
                            fridge.y = originY;
                        }
                        break;
                    default:
                        break;
                }
                fridge.bounds.x = fridge.x;
                fridge.bounds.y = fridge.y;
            }
        }
    }

}
