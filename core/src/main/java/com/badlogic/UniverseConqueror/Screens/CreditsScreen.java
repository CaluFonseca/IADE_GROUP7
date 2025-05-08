package com.badlogic.UniverseConqueror.Screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class CreditsScreen implements Screen {
    private BitmapFont font;
    private Game game;
    private SpriteBatch batch;

    public CreditsScreen(Game game) {
        this.game = game;
        font = new BitmapFont();
        batch = new SpriteBatch();
    }

    @Override
    public void show() {
        // Logic to show controls on the screen
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);  // Clear the screen

        batch.begin();  // Start the batch for drawing
        font.draw(batch, " Cláudio Fonseca -   \n Fernando Simões - \n Paulo Ferreira - \n Vítor Hugo Freitas - 20241067", 100, 300);
        batch.end();  // End the batch
    }

    @Override
    public void resize(int width, int height) {}

    @Override
    public void hide() {}

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void dispose() {
        font.dispose();
        batch.dispose();
    }
}
