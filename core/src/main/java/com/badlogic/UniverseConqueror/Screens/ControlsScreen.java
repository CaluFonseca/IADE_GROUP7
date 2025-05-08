package com.badlogic.UniverseConqueror.Screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class ControlsScreen implements Screen {
    private BitmapFont font;
    private Game game;
    private SpriteBatch batch;

    public ControlsScreen(Game game) {
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
        font.draw(batch, "Controlos do Jogo: \n- W: Mover para cima\n- S: Mover para baixo\n- A: Mover para esquerda\n- C: Ativar Câmera \n- RightMouse: Mover Câmera\n-Shift + WASD: Mover mais rápido\n- leftMouse: Tiro \n-E + leftMouse: SuperAtaque \n-Espaço: Salto \n- TAB: Defender \n - ESC: Pausa ", 100, 300);
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
