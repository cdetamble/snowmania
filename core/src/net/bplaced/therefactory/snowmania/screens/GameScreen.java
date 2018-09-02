package net.bplaced.therefactory.snowmania.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;

import net.bplaced.therefactory.snowmania.config.Constants;
import net.bplaced.therefactory.snowmania.model.Board;
import net.bplaced.therefactory.snowmania.model.Game;
import net.bplaced.therefactory.snowmania.model.actors.PowerUp;
import net.bplaced.therefactory.snowmania.model.actors.PowerUp.PowerUpType;
import net.bplaced.therefactory.snowmania.model.actors.Snowflake;
import net.bplaced.therefactory.snowmania.model.actors.Snowman;
import net.bplaced.therefactory.snowmania.model.actors.Snowman.State;
import net.bplaced.therefactory.snowmania.utils.ICallback;
import net.bplaced.therefactory.snowmania.utils.MathUtils;

import static net.bplaced.therefactory.snowmania.config.Constants.WINDOW_HEIGHT;
import static net.bplaced.therefactory.snowmania.config.Constants.WINDOW_WIDTH;

public class GameScreen extends ScreenAdapter implements InputProcessor {

    private final Sprite spriteStar;
    private final Sprite spriteDownload;
    private final Sound soundClick;
    private Snowman snowman;
    public OrthographicCamera camera;
    public FitViewport viewport;
    public SpriteBatch batch;
    public ShapeRenderer sr;

    public Game game;
    private BitmapFont font;
    public String scoreString;
    private Sprite landscape;
    private Sprite sky;
    private Texture lakeReflection;
    private Sprite vignette;
    private float lakeX;

    public Array<Snowflake> snowflakes = new Array<Snowflake>();
    private float fadeAlpha = 1;
    private Sprite frame;
    private Sprite title;
    boolean soundtrackScreen = false;
    private PowerUp powerup;
    private Array<RoundedButton> buttons = new Array<RoundedButton>();
    private float elapsedTime;
    private Vector2 unprojected = new Vector2();
    private RoundedButton pressedButton;
    private CharSequence versionString = "Snowmania Version " + Constants.VERSION;

    public GameScreen() {
        camera = new OrthographicCamera();
        viewport = new FitViewport(WINDOW_WIDTH, WINDOW_HEIGHT, camera);
        camera.position.set(WINDOW_WIDTH / 2, WINDOW_HEIGHT / 2, 0);
        camera.update();

        batch = new SpriteBatch();
        sr = new ShapeRenderer();
        sr.setAutoShapeType(true);

        font = new BitmapFont(Gdx.files.internal("fonts/amiga4everpro2.fnt"));

        game = new Game(this);
        scoreString = game.numGoals + "";

        frame = new Sprite(new Texture(Gdx.files.internal("frame.png")));
        title = new Sprite(new Texture(Gdx.files.internal("title.png")));
        title.setX(WINDOW_WIDTH / 2 - title.getWidth() / 2);
        title.setY(-title.getHeight());
        sky = new Sprite(new Texture(Gdx.files.internal("sky.png")));
        landscape = new Sprite(new Texture(Gdx.files.internal("landscape.png")));
        lakeReflection = new Texture(Gdx.files.internal("lake_reflection.png"));

        vignette = new Sprite(new Texture(Gdx.files.internal("vignette.png")));

        for (int i = 0; i < Constants.numSnowflakes; i++) {
            snowflakes.add(new Snowflake(
                    MathUtils.randomWithin(0, WINDOW_WIDTH),
                    MathUtils.randomWithin(0, WINDOW_HEIGHT),
                    MathUtils.randomWithin(-0.1f, -0.6f),
                    this));
        }

        if (soundtrackScreen) {
            Board board = new Board(game);
            snowman = new Snowman(board, 60, 40, Constants.PlayerDirection.Down, Constants.PlayerColor.Red, 0);
            snowman.state = State.Sleeping;
            powerup = new PowerUp(board, 85, 43, PowerUpType.Coffee, 99999);
        }

        buttons.add(new RoundedButton("Rate", 20, WINDOW_HEIGHT - 30, 35, 20, 4, new ICallback() {
            @Override
            public void execute() {
                Gdx.net.openURI("https://play.google.com/store/apps/details?id=net.bplaced.therefactory.snowmania");
            }
        }, viewport));
        buttons.add(new RoundedButton("Soundtrack", 80, WINDOW_HEIGHT - 30, 76, 20, 4, new ICallback() {
            @Override
            public void execute() {
                Gdx.net.openURI("https://youtu.be/qKxkrvXVTk8");
            }
        }, viewport));
        buttons.add(new RoundedButton("Play", WINDOW_WIDTH / 2f - 35f / 2f, WINDOW_HEIGHT / 2 - 50, 35, 20, 4, new ICallback() {
            @Override
            public void execute() {
                if (game.state.equals(Game.GameState.Paused)) {
                    game.state = Game.GameState.Running;
                } else {
                    game.reset();
                }
            }
        }, viewport));
        buttons.add(new RoundedButton("x", WINDOW_WIDTH - 30, WINDOW_HEIGHT - 30, 18, 18, 4, new ICallback() {
            @Override
            public void execute() {
                if (game.state.equals(Game.GameState.Paused)) {
                    game.state = Game.GameState.Running;
                } else {
                    game.state = Game.GameState.Paused;
                }
            }
        }, viewport));

        spriteStar = new Sprite(new Texture("star.png"));
        spriteDownload = new Sprite(new Texture("download.png"));

        spriteStar.setPosition(buttons.get(0).x + buttons.get(0).width - 4, buttons.get(0).y - 15);
        spriteDownload.setPosition(buttons.get(1).x + buttons.get(1).width - 4, buttons.get(1).y - 15);

        soundClick = game.assets.get("sfx/mouse_click.ogg", Sound.class);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        update(delta);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        sky.draw(batch);
        //lakeReflection.draw(batch);
        batch.draw(lakeReflection, lakeX, 0);
        batch.draw(lakeReflection, lakeX + lakeReflection.getWidth(), 0, 0, 0, lakeReflection.getWidth(), lakeReflection.getHeight(),
                1, 1, 0, 0, 0, lakeReflection.getWidth(), lakeReflection.getHeight(), true, false);

        landscape.draw(batch);

        vignette.draw(batch);
        if (soundtrackScreen) {
            snowman.x = 60;
            snowman.y = 40;
            snowman.render(batch);
            powerup.render(batch);
        }
        vignette.draw(batch);
        batch.end();

        game.render();

        sr.setProjectionMatrix(camera.combined);
        sr.begin(ShapeType.Filled);
        sr.setColor(Color.WHITE);
        for (Snowflake snowflake : snowflakes) {
            sr.point(snowflake.x, snowflake.y, 0);
        }
        if (!game.state.equals(Game.GameState.Paused) && !game.state.equals(Game.GameState.GameOver)) {
            for (RoundedButton button : buttons) {
                button.render(sr);
            }
        }
        sr.end();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        if (game.state.equals(Game.GameState.GameOver) || game.state.equals(Game.GameState.Paused)) {
            vignette.draw(batch);
            vignette.draw(batch);
            vignette.draw(batch);

            font.setColor(Color.WHITE);
            if (game.state.equals(Game.GameState.GameOver)) {
                font.draw(batch, "You fridged " + game.numGoals + " snowmen!", WINDOW_WIDTH / 2 - 70, 115);
            }
            font.draw(batch, versionString, 30, 28);
        } else if (game.state.equals(Game.GameState.Running)) {
            font.setColor(Color.WHITE);
            font.draw(batch, scoreString, 40, WINDOW_HEIGHT - 8);

        }
        if (game.state.equals(Game.GameState.Intro)) {
            title.draw(batch);
            renderIcons();
        }
        if (!game.state.equals(Game.GameState.Paused) && !game.state.equals(Game.GameState.GameOver)) {
            for (RoundedButton button : buttons) {
                button.render(batch, font);
            }
        }
        frame.draw(batch);
        batch.end();

        if (fadeAlpha > 0) {
            Gdx.gl.glEnable(GL20.GL_BLEND);
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            sr.setProjectionMatrix(camera.combined);
            sr.begin(ShapeType.Filled);
            sr.setColor(new Color(0, 0, 0, fadeAlpha));
            sr.rect(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);
            sr.end();
            Gdx.gl.glDisable(GL20.GL_BLEND);
        }

        if (game.state.equals(Game.GameState.Paused) || game.state.equals(Game.GameState.GameOver)) {
            sr.setProjectionMatrix(camera.combined);
            sr.begin(ShapeRenderer.ShapeType.Filled);
            sr.setColor(Color.WHITE);
            for (RoundedButton button : buttons) {
                button.render(sr);
            }
            sr.end();

            batch.setProjectionMatrix(camera.combined);
            batch.begin();
            for (RoundedButton button : buttons) {
                button.render(batch, font);
            }
            renderIcons();
            batch.end();
        }
    }

    private void renderIcons() {
        batch.draw(spriteDownload, spriteDownload.getX(), spriteDownload.getY(), 0, 0,
                spriteDownload.getWidth() / 2.5f, spriteDownload.getHeight() / 2.5f,
                MathUtils.oscilliate(elapsedTime, 0.9f, 1.1f, 1.8f),
                MathUtils.oscilliate(elapsedTime, 0.9f, 1.1f, -1.8f),
                MathUtils.oscilliate(elapsedTime, -5f, 5f, 1.8f));
        batch.draw(spriteStar, spriteStar.getX(), spriteStar.getY(), 0, 0,
                spriteStar.getWidth() / 2.5f, spriteStar.getHeight() / 2.5f,
                MathUtils.oscilliate(elapsedTime, 0.9f, 1.1f, 1.8f),
                MathUtils.oscilliate(elapsedTime, 0.9f, 1.1f, -1.8f),
                MathUtils.oscilliate(elapsedTime, -5f, 5f, 1.8f));
    }

    private void update(float delta) {
        elapsedTime += delta;

        sky.setY(MathUtils.oscilliate(game.elapsedTime, 0, 120, 30f));
        lakeX = -290 * sky.getY() / 120;

        for (Snowflake snowflake : snowflakes) {
            snowflake.update(delta);
        }

        if (fadeAlpha > 0) {
            fadeAlpha -= 0.005f;
            title.setY(Math.min(90, title.getY() + 1f));
        }

        if (!game.state.equals(Game.GameState.Paused)) {
            game.update(delta);
        }

        buttons.get(0).isVisible = !game.state.equals(Game.GameState.Running); // rate
        buttons.get(1).isVisible = !game.state.equals(Game.GameState.Running); // soundtrack
        buttons.get(2).isVisible = !game.state.equals(Game.GameState.Running) && title.getY() >= 80; // play
        buttons.get(3).isVisible = !game.state.equals(Game.GameState.Intro)
                && !game.state.equals(Game.GameState.GameOver); // x
    }

    @Override
    public void show() {
        super.show();
        Gdx.input.setInputProcessor(this);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    @Override
    public boolean keyDown(int keycode) {
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        unprojected.set(Gdx.input.getX(), Gdx.input.getY());
        unprojected = viewport.unproject(unprojected);
        for (RoundedButton roundedButton : buttons) {
            if (roundedButton.isVisible) {
                if (roundedButton.touchDown(unprojected)) {
                    pressedButton = roundedButton;
                    soundClick.play();
                    break;
                }
            }
        }
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        unprojected.set(Gdx.input.getX(), Gdx.input.getY());
        unprojected = viewport.unproject(unprojected);

        if (pressedButton != null) {
            for (RoundedButton roundedButton : buttons) {
                if (roundedButton.isVisible
                        && roundedButton.contains(unprojected)
                        && pressedButton == roundedButton
                        && pressedButton.isPressed) {
                    pressedButton.touchUp();
                }
            }
        }
        for (RoundedButton roundedButton : buttons) {
            roundedButton.isPressed = false;
        }
        pressedButton = null;
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        return false;
    }
}
