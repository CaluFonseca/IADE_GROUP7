//package com.badlogic.UniverseConqueror;
//
//import com.badlogic.gdx.ApplicationListener;
//import com.badlogic.gdx.Gdx;
//import com.badlogic.gdx.audio.Music;
//import com.badlogic.gdx.graphics.Color;
//import com.badlogic.gdx.graphics.Texture;
//import com.badlogic.gdx.graphics.g2d.SpriteBatch;
//import com.badlogic.gdx.utils.ScreenUtils;
//import com.badlogic.gdx.utils.viewport.FitViewport;
//
///** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
//public class Main implements ApplicationListener {
//    Texture backgroundTexture;
//    Music music;
//    SpriteBatch spriteBatch;
//    FitViewport viewport;
//    @Override
//    public void create() {
//        backgroundTexture = new Texture("space-galaxy-background.jpg");
//        music = Gdx.audio.newMusic(Gdx.files.internal("space_intro_sound.mp3"));
//        spriteBatch = new SpriteBatch();
//        viewport = new FitViewport(8, 5);
//    }
//
//    @Override
//    public void resize(int width, int height) {
//        viewport.update(width, height, true); // true centers the camera
//    }
//
//    @Override
//    public void render() {
//        input();
//        logic();
//        draw();
//    }
//
//    @Override
//    public void pause() {
//        // Invoked when your application is paused.
//    }
//
//    @Override
//    public void resume() {
//        // Invoked when your application is resumed after pause.
//    }
//
//    @Override
//    public void dispose() {
//        // Destroy application's resources here.
//    }
//
//    private void input() {
//
//    }
//
//    private void logic() {
//
//    }
//
//    private void draw() {
//        ScreenUtils.clear(Color.BLACK);
//        viewport.apply();
//        spriteBatch.setProjectionMatrix(viewport.getCamera().combined);
//        spriteBatch.begin();
//        // store the worldWidth and worldHeight as local variables for brevity
//        float worldWidth = viewport.getWorldWidth();
//        float worldHeight = viewport.getWorldHeight();
//
//        spriteBatch.draw(backgroundTexture, 0, 0, worldWidth, worldHeight); // draw the background
//        spriteBatch.end();
//    }
//
//}
