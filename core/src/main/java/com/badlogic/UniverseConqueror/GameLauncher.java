package com.badlogic.UniverseConqueror;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.UniverseConqueror.Screens.GameScreen;
import com.badlogic.UniverseConqueror.Screens.MainMenuScreen;

public class GameLauncher extends Game {
    public SpriteBatch batch;
    private GameScreen gameScreen;
    @Override
    public void create() {
        batch = new SpriteBatch();
        gameScreen = new GameScreen(this);
        setScreen(new MainMenuScreen(this));
    }

    public void startGame() {
        setScreen(new GameScreen(this));
    }
    public GameScreen getGameScreen() {
        return gameScreen;
    }

}
