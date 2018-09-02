package net.bplaced.therefactory.client;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.gwt.GwtApplication;
import com.badlogic.gdx.backends.gwt.GwtApplicationConfiguration;

import net.bplaced.therefactory.snowmania.config.Constants;
import net.bplaced.therefactory.snowmania.MyGdxGame;

public class HtmlLauncher extends GwtApplication {

        @Override
        public GwtApplicationConfiguration getConfig () {
                float scale = 2.5f;
                return new GwtApplicationConfiguration((int)(Constants.WINDOW_WIDTH * scale),
                        (int)(Constants.WINDOW_HEIGHT * scale));
        }

        @Override
        public ApplicationListener createApplicationListener () {
                return new MyGdxGame();
        }
}