package net.bplaced.therefactory.snowmania.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.Viewport;

import net.bplaced.therefactory.snowmania.utils.ICallback;
import net.bplaced.therefactory.snowmania.utils.MathUtils;

/**
 * Created by Christian on 27.02.2018.
 */

public class RoundedButton {

    public float x;
    public float y;
    public float width;
    private float height;
    private float radius;
    private final String label;
    private Color color, colorShadow, colorPressed;
    public boolean isVisible = true;

    public boolean isPressed;
    private final Viewport viewport;
    private final ICallback callback;

    public RoundedButton(String label, float x, float y, float width, float height, float radius, ICallback callback, Viewport viewport) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.radius = radius;
        this.label = label;
        color = Color.valueOf("#cccccc");
        colorPressed = Color.valueOf("#bbbbbb");
        colorShadow = Color.valueOf("#aaaaaa");
        this.viewport = viewport;
        this.callback = callback;
    }

    public void render(ShapeRenderer sr) {
        if (isVisible) {
            render(sr, 4, colorShadow);
            render(sr, isPressed ? 5 : 0, isPressed ? colorPressed : color);
        }
    }

    public void touchUp() {
        if (isPressed && isVisible) {
            callback.execute();
        }
        isPressed = false;
    }

    public boolean touchDown(Vector2 unprojected) {
        if (isVisible) {
            isPressed = contains(unprojected);
        }
        return isPressed;
    }

    public boolean contains(Vector2 unprojected) {
        return MathUtils.within(unprojected.x, x, x + width)
                && MathUtils.within(unprojected.y, y - 5, y + height);
    }

    public void render(SpriteBatch batch, BitmapFont font) {
        if (isVisible) {
            font.setColor(Color.DARK_GRAY);
            font.draw(batch, label, x + 5, y + 15 - (isPressed ? 5 : 0));
        }
    }

    private void render(ShapeRenderer sr, int yOffset, Color color) {
        if (isVisible) {
            sr.setColor(color);

            y -= yOffset;
            // Central rectangle
            sr.rect(x + radius, y + radius, width - 2 * radius, height - 2 * radius);

            // Four side rectangles, in clockwise order
            sr.rect(x + radius, y, width - 2 * radius, radius);
            sr.rect(x + width - radius, y + radius, radius, height - 2 * radius);
            sr.rect(x + radius, y + height - radius, width - 2 * radius, radius);
            sr.rect(x, y + radius, radius, height - 2 * radius);

            // Four arches, clockwise too
            sr.arc(x + radius, y + radius, radius, 180f, 90f);
            sr.arc(x + width - radius, y + radius, radius, 270f, 90f);
            sr.arc(x + width - radius, y + height - radius, radius, 0f, 90f);
            sr.arc(x + radius, y + height - radius, radius, 90f, 90f);
            y += yOffset;
        }
    }

}
