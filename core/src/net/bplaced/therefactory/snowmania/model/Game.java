package net.bplaced.therefactory.snowmania.model;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Music.OnCompletionListener;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;

import net.bplaced.therefactory.snowmania.config.Constants;
import net.bplaced.therefactory.snowmania.model.actors.PowerUp.PowerUpType;
import net.bplaced.therefactory.snowmania.screens.GameScreen;
import net.bplaced.therefactory.snowmania.utils.MathUtils;

public class Game {

    public Board board;
    public GameScreen screen;
    public TextureAtlas atlas = new TextureAtlas("pack.atlas");
    public AssetManager assets = new AssetManager();
    private Sound soundCollect;
    private Sound soundClick;
    private Sound soundSelect;
    private Music soundSpawn;
    private Music soundExplosion;
    private Music soundFridged;
    public float elapsedTime;
    private int timeNextSpawnSnowman;
    private int timeNextSpawnPowerUp;
    private int timeNextRepositionFridges;
    public int numGoals;
    private Music[] songs;
    private int songIndex = -1;
    private int spawnTimeSnowman;
    private float timeNextIncreaseDifficulty;
    private boolean down = true;
    public GameState state = GameState.Intro;

    public enum GameState {
        Running, GameOver, Intro, Paused
    }

    public Game(GameScreen screen) {
        loadAssets();
        this.screen = screen;
        board = new Board(this);
        playNextSong();
    }

    private void playNextSong() {
        songIndex = (songIndex + 1) % songs.length;
        songs[songIndex].play();
    }

    public void reset() {
        state = GameState.Running;
        numGoals = 0;
        screen.scoreString = numGoals + "";
        elapsedTime = 0f;
        timeNextSpawnSnowman = 8;
        timeNextSpawnPowerUp = 3;
        timeNextRepositionFridges = Constants.repositionFridgesEverySeconds;
        timeNextIncreaseDifficulty = Constants.increaseDifficultyEverySeconds;
        spawnTimeSnowman = 8;
        board.reset();
        board.spawnSnowmanIntoCell(
                //0, 1,
                board.numColumns / 2,
                board.numRows / 2,
                //PlayerDirection.Right,
                Constants.PlayerDirection.values()[MathUtils.randomWithin(0, Constants.PlayerDirection.values().length - 1)],
                Constants.PlayerColor.values()[MathUtils.randomWithin(0, Constants.PlayerColor.values().length - 1)],
                MathUtils.randomWithin(0.4f, 0.6f));
    }

    public void update(float delta) {
        elapsedTime += delta;

        if (state.equals(GameState.Running)) {

            // move closed fridges
            if (elapsedTime >= timeNextRepositionFridges) {
                timeNextRepositionFridges += Constants.repositionFridgesEverySeconds;
                board.repositionFridges();
            }

            // difficulty
            if (elapsedTime >= timeNextIncreaseDifficulty) {
                timeNextIncreaseDifficulty += Constants.increaseDifficultyEverySeconds;
                if (down) {
                    spawnTimeSnowman--;
                    if (spawnTimeSnowman <= 1) {
                        down = false;
                    }
                } else {
                    spawnTimeSnowman++;
                    if (spawnTimeSnowman >= 8) {
                        down = true;
                    }
                }
            }

            // snowmen
            if (elapsedTime >= timeNextSpawnSnowman) {
                timeNextSpawnSnowman += spawnTimeSnowman;
                if (board.snowmen.size() < 20) {
                    board.spawnSnowmanIntoCell(
                            MathUtils.randomWithin(1, board.numColumns - 2),
                            MathUtils.randomWithin(1, board.numRows - 2),
                            Constants.PlayerDirection.values()[MathUtils.randomWithin(0, Constants.PlayerDirection.values().length - 1)],
                            Constants.PlayerColor.values()[MathUtils.randomWithin(0, Constants.PlayerColor.values().length - 1)],
                            0.4f + (1 - (spawnTimeSnowman / 8f)) * 0.9f + MathUtils.randomWithin(-0.1f, 0.1f));
                }
            }

            // power ups
            if (elapsedTime >= timeNextSpawnPowerUp) {
                timeNextSpawnPowerUp += MathUtils.randomWithin(3, 4);
                board.spawnPowerUpInCell(MathUtils.randomWithin(1, board.numColumns - 2),
                        MathUtils.randomWithin(1, board.numRows - 2),
                        PowerUpType.values()[MathUtils.randomWithin(0, PowerUpType.values().length - 1)],
                        MathUtils.randomWithin(15, 25));
            }

            board.update(delta);
        }
    }

    public void render() {
        board.render();
    }

    private void loadAssets() {
        assets.load("sfx/mouse_click.ogg", Sound.class);
        assets.load("sfx/collect.ogg", Sound.class);
        assets.load("sfx/spawn.ogg", Music.class);
        assets.load("sfx/select.ogg", Sound.class);
        assets.load("sfx/explosion.ogg", Music.class);
        assets.load("sfx/fridged.ogg", Music.class);
        assets.load("music/song1.ogg", Music.class);
        assets.load("music/song2.ogg", Music.class);
        assets.load("music/song3.ogg", Music.class);
        assets.finishLoading();
        soundCollect = assets.get("sfx/collect.ogg", Sound.class);
        soundSpawn = assets.get("sfx/spawn.ogg", Music.class);
        soundSelect = assets.get("sfx/select.ogg", Sound.class);
        soundExplosion = assets.get("sfx/explosion.ogg", Music.class);
        soundFridged = assets.get("sfx/fridged.ogg", Music.class);
        soundClick = assets.get("sfx/mouse_click.ogg", Sound.class);
        songs = new Music[3];
        for (int i = 0; i < 3; i++) {
            songs[i] = assets.get("music/song" + (i + 1) + ".ogg", Music.class);
            songs[i].setLooping(false);
            songs[i].setOnCompletionListener(new OnCompletionListener() {

                @Override
                public void onCompletion(Music music) {
                    playNextSong();
                }
            });
        }
    }

    public void playSoundFridged() {
        if (!soundFridged.isPlaying()) {
            soundFridged.play();
        }
    }

    public void playSoundCollect() {
        soundCollect.play();
    }

    public void playSoundSelect() {
        soundSelect.play();
    }

    public void playSoundExplosion() {
        if (!soundExplosion.isPlaying()) {
            soundExplosion.play();
        }
    }

    public void playSoundSpawn() {
        if (!soundSpawn.isPlaying()) {
            soundSpawn.play();
        }
    }

    public void playSoundClick() {
        soundClick.play();
    }

    public void incrementGoals() {
        numGoals++;
        screen.scoreString = numGoals + "";
    }

}
