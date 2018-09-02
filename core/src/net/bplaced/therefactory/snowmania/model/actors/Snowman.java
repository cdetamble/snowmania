package net.bplaced.therefactory.snowmania.model.actors;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Timer;

import net.bplaced.therefactory.snowmania.config.Constants;
import net.bplaced.therefactory.snowmania.config.Constants.PlayerColor;
import net.bplaced.therefactory.snowmania.config.Constants.PlayerDirection;
import net.bplaced.therefactory.snowmania.model.Board;
import net.bplaced.therefactory.snowmania.utils.MathUtils;
import net.bplaced.therefactory.snowmania.utils.Tuple;

public class Snowman {

    // config
    private final float frameDuration = 0.2f; // how fast the animation should be
    private float numSecondsToPlayCollisionAnimation = 2.0f;
    private float spawnAnimationTimeMultiplier = 1f;

    // multipliers
    private float walkingSpeedMultiplier = 1f;

    // animation
    private Animation[] animations;

    public float x, y;
    public PlayerColor color;
    public PlayerDirection direction;
    public PlayerDirection desiredDirection;
    private Board board;
    public Rectangle rectangleTouch;
    public Rectangle rectangleCollision;
    private float walkingSpeed;
    private float velocityX;
    private float velocityY;
    public State state;
    private float oldStateTime = 0f;
    public float spawnY;
    private Timer timer;
    private Timer.Task task;
    private float previousX;
    private float previousY;
    public boolean fridged; // whether this snowman has entered a fridge
    public int numCollisions;
    private TextureRegion textureMultiScarf;
    private boolean hasMultiScarf = false;

    public enum State {
        Spawning, Collided, Walking, Melt, Melted, EnterFridge, Sleeping
    }

    public enum AnimationType {
        Walking_Right, Walking_Down, Walking_Up, Collided, Sleeping, Melting
    }

    public Snowman(Board board, float x, float y, PlayerDirection direction, PlayerColor color, float walkingSpeed) {
        this.board = board;
        this.color = color;
        this.x = x;
        this.y = Constants.WINDOW_HEIGHT;
        this.spawnY = y;
        this.walkingSpeed = walkingSpeed;
        this.direction = direction;
        this.desiredDirection = direction;
        this.timer = new Timer();
        state = State.Spawning;
        this.textureMultiScarf = board.game.atlas.findRegion("scarf");

        rectangleTouch = new Rectangle(x, y, 22, 25);
        rectangleCollision = new Rectangle(x + 1, y + 1, 21, 15);

        changeVelocitiesAccordingTo(direction);
        loadAnimations();
        board.game.playSoundSpawn();
    }

    private void loadAnimations() {
        this.animations = new Animation[AnimationType.values().length];
        int i = 0;
        for (AnimationType type : AnimationType.values()) {
            String id = type.name().toLowerCase();
            if (!type.equals(AnimationType.Melting)) { // animation types that are colorless
                id = color.name().toLowerCase() + "_" + id;
            }
            animations[i++] = new Animation(frameDuration, board.game.atlas.findRegions(id), Animation.PlayMode.LOOP);
        }
    }

    public void update(float delta) {
        switch (state) {
            case Spawning:
                y -= (Constants.WINDOW_HEIGHT - spawnY) / (Constants.WINDOW_HEIGHT) * 2 * spawnAnimationTimeMultiplier;
                if (y <= spawnY) {
                    y = spawnY;
                    state = State.Walking;
                }
                break;
            case Melt:
                if (board.game.elapsedTime > oldStateTime + numSecondsToPlayCollisionAnimation) {
                    state = State.Melted;
                }
                break;
            case Collided:
                if (numCollisions > Constants.maxNumCollisions) {
                    melt();
                }
                if (board.game.elapsedTime > oldStateTime + numSecondsToPlayCollisionAnimation) {
                    state = State.Walking;
                }
                break;
            case EnterFridge:
            case Walking:
                if (!desiredDirection.equals(direction) && isCenteredOnCell()) {
                    applyDesiredDirection();
                }

                if (playerHasTouchedSnowman()) {
                    changeDesiredWalkingDirection();
                    board.game.playSoundClick();

                    // instantly change direction if orientation is the same between current and
                    // desired walking direction
                    if (walkingAndDesiredDirectionsHaveSameOrientation()) {
                        applyDesiredDirection();
                    }
                }

                previousX = x;
                previousY = y;
                x += velocityX * walkingSpeedMultiplier;
                y += velocityY * walkingSpeedMultiplier;
                snapToGrid();
                updateRectangles();

                checkForCollisions();
                if (!checkForGoal()) {
                    checkForOutOfBounds();
                }
                break;
            default:
                break;
        }
    }

    // ensure that snowman moves along grid, otherwise change of lanes is not working anymore
    private void snapToGrid() {
        if (isDirectionHorizontal(direction)) {
            y = board.originY + ((int) (y / board.cellsize)) * board.cellsize;

        } else {
            x = board.originX + ((int) (x / board.cellsize) - 1) * board.cellsize;
        }
    }

    private void updateRectangles() {
        rectangleTouch.setPosition(x, y);
        rectangleCollision.setPosition(x, y);
    }

    public void render(SpriteBatch batch) {
        TextureRegion currentFrame = null;

        if (hasMultiScarf) {
            batch.draw(textureMultiScarf, x + 6, y + 27, 0, 0, textureMultiScarf.getRegionWidth(), textureMultiScarf.getRegionHeight(),
                    MathUtils.oscilliate(board.game.elapsedTime, 0.8f, 1.2f, 2f),
                    MathUtils.oscilliate(board.game.elapsedTime, 0.8f, 1.2f, -2f),
                    MathUtils.oscilliate(board.game.elapsedTime, -5, 5, 2f));
        }

        switch (state) {
            case Melt:
                currentFrame = getCurrentFrameFor(AnimationType.Melting, true);
                break;
            case Sleeping:
                currentFrame = getCurrentFrameFor(AnimationType.Sleeping, true);
                break;
            case Collided:
                currentFrame = getCurrentFrameFor(AnimationType.Collided, true);
                break;
            case Spawning:
                currentFrame = getCurrentFrameFor(AnimationType.Walking_Down, true);
                break;
            case EnterFridge:
            case Walking:
                if (isDirectionHorizontal(desiredDirection)) {
                    currentFrame = getCurrentFrameFor(AnimationType.Walking_Right, true);
                    if (currentFrame != null) {
                        if (!currentFrame.isFlipX())
                            currentFrame.flip(desiredDirection.equals(PlayerDirection.Left), false);
                        else
                            currentFrame.flip(desiredDirection.equals(PlayerDirection.Right), false);
                    }
                } else if (desiredDirection.equals(PlayerDirection.Down)) {
                    currentFrame = getCurrentFrameFor(AnimationType.Walking_Down, true);
                } else if (desiredDirection.equals(PlayerDirection.Up)) {
                    currentFrame = getCurrentFrameFor(AnimationType.Walking_Up, true);
                }

                // make snowman disappear as son as fully within fridge
                if (state.equals(State.EnterFridge)) {
                    if (direction.equals(PlayerDirection.Left)) {
                        if ((board.originX - x) >= board.cellsize) {
                            state = State.Melted;
                        }
                    } else if (direction.equals(PlayerDirection.Up)) {
                        //System.out.println(y - board.endY );
                        if (y - board.endY >= 5) {
                            state = State.Melted;
                        }
                    } else if (direction.equals(PlayerDirection.Right)) {
                        if ((x - board.endX) > 0) {
                            state = State.Melted;
                        }
                    }
                }
                break;
            default:
                break;
        }

        if (currentFrame != null) {
            batch.draw(currentFrame, x, y);
        }
    }

    private TextureRegion getCurrentFrameFor(AnimationType type, boolean loop) {
        return (TextureRegion) animations[type.ordinal()].getKeyFrame(board.game.elapsedTime, loop);
    }

    private boolean checkForGoal() {
        for (Fridge fridge : board.fridges) {
            if (!fridge.isMoving && fridge.bounds.overlaps(rectangleCollision)) {
                if ((hasMultiScarf || fridge.color.equals(color))) {
                    board.game.playSoundFridged();
                    if (!state.equals(State.EnterFridge)) {
                        board.game.incrementGoals();
                    }
                    state = State.EnterFridge;
                    fridged = true;
                    fridge.isOpen = true;
                    fridge.enteringSnowman = this;
                } else {
                    board.game.playSoundExplosion();
                    melt();
                }
                return true;
            }
        }
        return false;
    }

    private void melt() {
        state = State.Melt;
        oldStateTime = board.game.elapsedTime;
    }

    private void checkForOutOfBounds() {
        if (rectangleCollision.x < board.originX - 5 || rectangleCollision.x + rectangleCollision.width - 5 > board.endX
                || rectangleCollision.y < board.originY - 5 || rectangleCollision.y + rectangleCollision.height - 5 > board.endY) {
            if (!state.equals(State.EnterFridge)) {
                board.game.playSoundExplosion();
            }
            melt();
        }
    }

    private void checkForCollisions() {
        // collision with other snowman
        for (Snowman snowman : board.snowmen) {
            if (!snowman.equals(this) // don't collide with yourself!
                    && rectangleCollision.overlaps(snowman.rectangleCollision)
                    && state.equals(State.Walking)
                    && snowman.state.equals(State.Walking)) {
                collideWith(snowman);
                snowman.collideWith(this);
            }
        }
    }

    public void collideWith(Snowman snowman) {
        revertEffectOfCurrentPowerUp();
        state = State.Collided;
        oldStateTime = board.game.elapsedTime;
        reverseWalkingDirection();
        board.game.playSoundExplosion();
        board.collisions.add(new Tuple(this, snowman));
        numCollisions++;
        hasMultiScarf = false;
    }

    private void reverseWalkingDirection() {
        switch (direction) {
            case Up:
                desiredDirection = PlayerDirection.Down;
                break;
            case Down:
                desiredDirection = PlayerDirection.Up;
                break;
            case Left:
                desiredDirection = PlayerDirection.Right;
                break;
            case Right:
                desiredDirection = PlayerDirection.Left;
                break;
        }
        applyDesiredDirection();
    }

    private void applyDesiredDirection() {
        direction = desiredDirection;
        changeVelocitiesAccordingTo(direction);
    }

    private boolean walkingAndDesiredDirectionsHaveSameOrientation() {
        return Constants.Orientations[desiredDirection.ordinal()]
                .equals(Constants.Orientations[direction.ordinal()]);
    }

    private void changeVelocitiesAccordingTo(PlayerDirection direction) {
        switch (direction) {
            case Up:
                velocityX = 0;
                velocityY = walkingSpeed;
                break;
            case Down:
                velocityX = 0;
                velocityY = -walkingSpeed;
                break;
            case Left:
                velocityX = -walkingSpeed;
                velocityY = 0;
                break;
            case Right:
                velocityX = walkingSpeed;
                velocityY = 0;
                break;
            default:
                break;
        }
    }

    private boolean isCenteredOnCell() {
        //return (x - board.originX) % board.cellsize == 0
        //		&& (y - board.originY) % board.cellsize == 0;
        for (int column = 0; column < board.numColumns; column++) {
            for (int row = 0; row < board.numRows; row++) {
                if (MathUtils.within(board.originX + column * board.cellsize, previousX < x ? previousX : x, previousX < x ? x : previousX)
                        && MathUtils.within(board.originY + row * board.cellsize, previousY < y ? previousY : y, previousY < y ? y : previousY)) {
                    return true;
                }
            }
        }
        return false;
    }

    // clock-wise
    private void changeDesiredWalkingDirection() {
        desiredDirection = PlayerDirection.values()[(desiredDirection.ordinal() + 1)
                % (PlayerDirection.values().length)];
    }

    private boolean playerHasTouchedSnowman() {
        Vector2 unprojected = board.game.screen.viewport.unproject(new Vector2(Gdx.input.getX(), Gdx.input.getY()));
        return Gdx.input.justTouched() && rectangleTouch.contains(unprojected.x, unprojected.y);
    }

    public boolean isDirectionHorizontal(PlayerDirection direction) {
        return Constants.Orientations[direction.ordinal()].equals(Constants.Orientation.Horizontal);
    }

    public void debug(ShapeRenderer sr) {
        //sr.polygon(rectangleTouch.getTransformedVertices());
        sr.setColor(Color.PURPLE);
        //sr.rect(rectangleTouch.x, rectangleTouch.y, rectangleTouch.width, rectangleTouch.height);
        sr.setColor(Color.GREEN);
        sr.rect(rectangleCollision.x, rectangleCollision.y, rectangleCollision.width, rectangleCollision.height);
    }

    public void consume(PowerUp powerUp) {

        // before consuming new powerup, revert the effect of the current one
        revertEffectOfCurrentPowerUp();

        switch (powerUp.type) {

            case Wine:
                walkingSpeedMultiplier = MathUtils.randomWithin(0.2f, 0.5f);
                applyFrameDurationToAnimations(frameDuration * 3f);
                task = new Timer.Task() {
                    @Override
                    public void run() {
                        walkingSpeedMultiplier = 1;
                        applyFrameDurationToAnimations(frameDuration);
                    }
                };
                timer.scheduleTask(task, MathUtils.randomWithin(8, 20) * 1000);
                break;

            case Coffee:
                walkingSpeedMultiplier = MathUtils.randomWithin(1.7f, 2.2f);
                applyFrameDurationToAnimations(frameDuration * 0.3f);
                task = new Timer.Task() {
                    @Override
                    public void run() {
                        walkingSpeedMultiplier = 1;
                        applyFrameDurationToAnimations(frameDuration);
                    }
                };
                timer.scheduleTask(task, MathUtils.randomWithin(8, 20) * 1000);
                break;

            case Pillow:
                final State oldState = state;
                state = State.Sleeping;
                task = new Timer.Task() {
                    @Override
                    public void run() {
                        state = oldState;
                    }
                };
                timer.scheduleTask(task, MathUtils.randomWithin(8, 20) * 1000);
                break;
            case Scarf:
                hasMultiScarf = true;
                break;
            default:
                break;
        }
    }

    private void applyFrameDurationToAnimations(float duration) {
        for (Animation a : animations) {
            a.setFrameDuration(duration);
        }
    }

    private void revertEffectOfCurrentPowerUp() {
        if (task != null) {
            task.run();
            timer.clear();
        }
    }

    // ensure that the collisions areas do not overlap anymore, after a collision has happened (so the snowmen dont get stuck in infinite collisions)
    public void resolveCollisionWith(Snowman snowman) {
        //System.out.println("resolve collision");
        if (isDirectionHorizontal(direction)) { // either move left or right
            if (x < snowman.x) { // i am left to the other snowman -> i move left
                float numPixelsToStepBack = (rectangleCollision.x + rectangleCollision.width) - snowman.rectangleCollision.x;
                if (numPixelsToStepBack > 0 && x > board.originX - numPixelsToStepBack) {
                    x -= numPixelsToStepBack;
                }
            } else if (x > snowman.x) {// i am right to the other snowman -> i move right
                float numPixelsToStepBack = (snowman.rectangleCollision.x + snowman.rectangleCollision.width) - snowman.rectangleCollision.x;
                if (numPixelsToStepBack > 0 && x < board.originX + numPixelsToStepBack) {
                    x += numPixelsToStepBack;
                }
            }
        } else { // either move up or down
            if (y < snowman.y) { // i am below the other snowman -> i move down
                float numPixelsToStepBack = (rectangleCollision.y + rectangleCollision.height) - snowman.rectangleCollision.y;
                //System.out.println(numPixelsToStepBack);
                if (numPixelsToStepBack > 0 && y > board.originY - numPixelsToStepBack) {
                    y -= numPixelsToStepBack;
                }
            } else if (y > snowman.y) {// i am above the other snowman -> i move up
                float numPixelsToStepBack = (snowman.rectangleCollision.y + snowman.rectangleCollision.height) - rectangleCollision.y;
                if (numPixelsToStepBack > 0 && y < board.endY + numPixelsToStepBack) {
                    y += numPixelsToStepBack;
                }
            }
        }
        updateRectangles();
    }
}
