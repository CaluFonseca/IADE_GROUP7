package com.badlogic.UniverseConqueror.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.UniverseConqueror.GameLauncher;

public class PauseScreen implements Screen {

    private GameLauncher game;
    private GameScreen gameScreen;
    private Stage stage;
    private Skin skin;
    private Table table;
    private SpriteBatch batch;
    private Texture background;
    private Music music;
    private Sound hoverSound;
    private Sound clickSound;

    private boolean isAudioOn = true;

    private float elapsedTime = 0; // Tempo decorrido em pausa
    private Label timerLabel; // Exibe o cronômetro

    public PauseScreen(GameLauncher game, GameScreen gameScreen) {
        this.game = game;
        this.gameScreen = gameScreen;
    }

    @Override
    public void show() {
        stage = new Stage(new FitViewport(1920, 1080));
        Gdx.input.setInputProcessor(stage);

        batch = new SpriteBatch();
        skin = new Skin(Gdx.files.internal("ui/uiskin.json"), new TextureAtlas(Gdx.files.internal("ui/uiskin.atlas")));
        background = new Texture(Gdx.files.internal("background_pause.jpg"));
        music = Gdx.audio.newMusic(Gdx.files.internal("audio/space_intro_sound.mp3"));
        hoverSound = Gdx.audio.newSound(Gdx.files.internal("audio/alert0.mp3"));
        clickSound = Gdx.audio.newSound(Gdx.files.internal("audio/keyboardclick.mp3"));

        music.setLooping(true);
        music.play();

        table = new Table();
        table.center();
        table.setFillParent(true);

        Label pauseLabel = new Label("PAUSA", skin, "title");
        pauseLabel.setFontScale(1.5f); // Ajuste conforme desejar
        table.add(pauseLabel).padBottom(50).row();
        // Botões
        TextButton resumeButton = new TextButton("Continuar", skin);
        TextButton mainMenuButton = new TextButton("Menu Principal", skin);
        TextButton exitButton = new TextButton("Sair", skin);
        TextButton audioToggleButton = new TextButton("Som: On", skin);

        // Listeners
        resumeButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                clickSound.play();
                gameScreen.resumeGame();
                game.setScreen(gameScreen);
            }
        });

        mainMenuButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                clickSound.play();
                music.stop();
                game.setScreen(new MainMenuScreen(game));
            }
        });

        exitButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                clickSound.play();
                Gdx.app.exit();
            }
        });

        audioToggleButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                clickSound.play();
                isAudioOn = !isAudioOn;
                if (isAudioOn) {
                    music.play();
                    audioToggleButton.setText("Som: On");
                } else {
                    music.pause();
                    audioToggleButton.setText("Som: Off");
                }
            }
        });

        // Hover Sound
        addHoverSound(resumeButton);
        addHoverSound(mainMenuButton);
        addHoverSound(exitButton);
        addHoverSound(audioToggleButton);

        // Cronômetro
        timerLabel = new Label("00:00:00", skin);
        timerLabel.setFontScale(2f);
        table.add(timerLabel).padBottom(30).row();

        float buttonWidth = 400f;
        float buttonHeight = 80f;

        table.add(resumeButton).size(buttonWidth, buttonHeight).pad(10).row();
        table.add(mainMenuButton).size(buttonWidth, buttonHeight).pad(10).row();
        table.add(exitButton).size(buttonWidth, buttonHeight).pad(10).row();
        table.add(audioToggleButton).size(buttonWidth, buttonHeight).pad(10).row();

        stage.addActor(table);
    }

    private void updateTimer(float deltaTime) {
        elapsedTime += deltaTime;
        int hours = (int) (elapsedTime / 3600);
        int minutes = (int) ((elapsedTime % 3600) / 60);
        int seconds = (int) (elapsedTime % 60);

        String timeFormatted = String.format("%02d:%02d:%02d", hours, minutes, seconds);
        timerLabel.setText(timeFormatted);
    }

    private void addHoverSound(TextButton button) {
        button.addListener(new ClickListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                hoverSound.play();
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                hoverSound.stop();
            }
        });
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();
        batch.draw(background, 0, 0, stage.getViewport().getWorldWidth(), stage.getViewport().getWorldHeight());
        batch.end();

        updateTimer(delta);

        stage.act(Math.min(delta, 1 / 30f));
        stage.draw();
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
        stage.dispose();
        skin.dispose();
        batch.dispose();
        background.dispose();
        music.dispose();
        hoverSound.dispose();
        clickSound.dispose();
    }
}
