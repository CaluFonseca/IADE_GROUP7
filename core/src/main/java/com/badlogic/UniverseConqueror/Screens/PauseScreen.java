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
import com.badlogic.UniverseConqueror.Utils.Timer;

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

    private Timer pauseTimer; // Timer to track how long the game has been paused
    private Label timerLabel;

    // Constructor to initialize the game and gameScreen references
    public PauseScreen(GameLauncher game, GameScreen gameScreen) {
        this.game = game;
        this.gameScreen = gameScreen;
    }

    @Override
    public void show() {
        // Setup the stage and viewport
        stage = new Stage(new FitViewport(1920, 1080));
        Gdx.input.setInputProcessor(stage);

        batch = new SpriteBatch();
        skin = new Skin(Gdx.files.internal("ui/uiskin.json"), new TextureAtlas(Gdx.files.internal("ui/uiskin.atlas")));
        background = new Texture(Gdx.files.internal("background_pause.jpg"));  // Background image for pause screen
        music = Gdx.audio.newMusic(Gdx.files.internal("audio/space_intro_sound.mp3")); // Background music for pause screen
        hoverSound = Gdx.audio.newSound(Gdx.files.internal("audio/alert0.mp3")); // Sound for hover effect
        clickSound = Gdx.audio.newSound(Gdx.files.internal("audio/keyboardclick.mp3")); // Sound for button click

        // Set music to loop and start playing
        music.setLooping(true);
        music.play();

        // Timer to track pause duration
        pauseTimer = new Timer(Float.MAX_VALUE); // Infinite timer to just count time
        pauseTimer.start();

        // Create and configure the table layout for buttons
        table = new Table();
        table.center();
        table.setFillParent(true);

        // Add a label for the pause screen title
        Label pauseLabel = new Label("PAUSE", skin, "title");
        pauseLabel.setFontScale(1.5f);
        table.add(pauseLabel).padBottom(50).row();

        // Create buttons for the pause screen
        TextButton resumeButton = new TextButton("Resume", skin);
        TextButton mainMenuButton = new TextButton("Main Menu", skin);
        TextButton exitButton = new TextButton("Exit", skin);
        TextButton audioToggleButton = new TextButton("Sound: On", skin);

        // Add listeners for the buttons
        resumeButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                clickSound.play(); // Play click sound
                gameScreen.resumeGame(); // Resume the game
                game.setScreen(gameScreen); // Switch back to the game screen
            }
        });

        mainMenuButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                clickSound.play(); // Play click sound
                music.stop(); // Stop the pause screen music
                game.setScreen(new MainMenuScreen(game)); // Switch to the main menu screen
            }
        });

        exitButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                clickSound.play(); // Play click sound
                Gdx.app.exit(); // Exit the game application
            }
        });

        audioToggleButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                clickSound.play(); // Play click sound
                isAudioOn = !isAudioOn; // Toggle the audio state
                if (isAudioOn) {
                    music.play(); // Resume music
                    audioToggleButton.setText("Sound: On"); // Update button text
                } else {
                    music.pause(); // Pause music
                    audioToggleButton.setText("Sound: Off"); // Update button text
                }
            }
        });

        // Add hover sound to buttons
        addHoverSound(resumeButton);
        addHoverSound(mainMenuButton);
        addHoverSound(exitButton);
        addHoverSound(audioToggleButton);

        // Add a timer label to show the time since the game was paused
        timerLabel = new Label("00:00:00", skin);
        timerLabel.setFontScale(2f);
        table.add(timerLabel).padBottom(30).row();

        // Set button size
        float buttonWidth = 400f;
        float buttonHeight = 80f;

        // Add buttons to the table
        table.add(resumeButton).size(buttonWidth, buttonHeight).pad(10).row();
        table.add(mainMenuButton).size(buttonWidth, buttonHeight).pad(10).row();
        table.add(exitButton).size(buttonWidth, buttonHeight).pad(10).row();
        table.add(audioToggleButton).size(buttonWidth, buttonHeight).pad(10).row();

        // Add the table to the stage
        stage.addActor(table);
    }

    // Method to add hover sound effect to buttons
    private void addHoverSound(TextButton button) {
        button.addListener(new ClickListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                hoverSound.play(); // Play hover sound when mouse enters button
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                hoverSound.stop(); // Stop hover sound when mouse exits button
            }
        });
    }

    // Update the timer label to display the elapsed time
    private void updateTimer() {
        float elapsed = pauseTimer.getTime(); // Get elapsed time
        int hours = (int) (elapsed / 3600); // Calculate hours
        int minutes = (int) ((elapsed % 3600) / 60); // Calculate minutes
        int seconds = (int) (elapsed % 60); // Calculate seconds

        // Format and set the timer label text
        String timeFormatted = String.format("%02d:%02d:%02d", hours, minutes, seconds);
        timerLabel.setText(timeFormatted); // Update timer label text
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1); // Clear the screen with black color
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT); // Clear the screen

        batch.begin();
        batch.draw(background, 0, 0, stage.getViewport().getWorldWidth(), stage.getViewport().getWorldHeight()); // Draw the background
        batch.end();

        pauseTimer.update(delta); // Update the pause timer
        updateTimer(); // Update the timer label

        stage.act(Math.min(delta, 1 / 30f)); // Update the stage actions
        stage.draw(); // Draw the stage (buttons and UI)
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true); // Adjust the stage viewport when the screen is resized
    }

    @Override
    public void hide() {
        dispose(); // Dispose of resources when the screen is hidden
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    // Dispose of all resources
    @Override
    public void dispose() {
        stage.dispose(); // Dispose of the stage
        skin.dispose(); // Dispose of the skin (UI theme)
        batch.dispose(); // Dispose of the sprite batch
        background.dispose(); // Dispose of the background texture
        music.dispose(); // Dispose of the background music
        hoverSound.dispose(); // Dispose of the hover sound
        clickSound.dispose(); // Dispose of the click sound
    }
}
