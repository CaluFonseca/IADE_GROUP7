package com.badlogic.UniverseConqueror.Screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public abstract class BaseInfosScreen implements Screen {
    protected Game game;
    protected BitmapFont font;
    protected SpriteBatch batch;
    protected GlyphLayout layout;

    protected Stage stage;
    protected Skin skin;
    protected TextButton backButton;
    protected Sound buttonSound;

    protected String screenText;

    public BaseInfosScreen(Game game, String screenText) {
        this.game = game;
        this.screenText = screenText;
        initializeResources();
        initializeUI();
    }

    private void initializeResources() {
        font = new BitmapFont();
        batch = new SpriteBatch();
        layout = new GlyphLayout();
        stage = new Stage(new ScreenViewport());
        skin = new Skin(Gdx.files.internal("ui/uiskin.json"));
        buttonSound = Gdx.audio.newSound(Gdx.files.internal("audio/keyboardclick.mp3"));
    }

    private void initializeUI() {
        backButton = new TextButton("Back", skin);
        backButton.setPosition(20, 20);
        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                buttonSound.play();
                game.setScreen(new MainMenuScreen(game));
            }
        });
        stage.addActor(backButton);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        drawScreenText();

        stage.act(delta);
        stage.draw();
    }

    private void drawScreenText() {
        layout.setText(font, screenText);

        float x = (Gdx.graphics.getWidth() - layout.width) / 2;
        float y = (Gdx.graphics.getHeight() + layout.height) / 2;

        batch.begin();
        font.draw(batch, layout, x, y);
        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void hide() {
        dispose();
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void dispose() {
        font.dispose();
        batch.dispose();
        stage.dispose();
        skin.dispose();
        buttonSound.dispose();
    }
}
