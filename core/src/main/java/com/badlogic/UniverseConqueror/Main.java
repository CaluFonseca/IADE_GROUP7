package com.badlogic.UniverseConqueror;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main implements ApplicationListener {
    Texture backgroundTexture;
    Music music;
    SpriteBatch spriteBatch;
    FitViewport viewport;
    @Override
    public void create() {
        backgroundTexture = new Texture("space-galaxy-background.jpg");
        music = Gdx.audio.newMusic(Gdx.files.internal("space_intro_sound.mp3"));
        spriteBatch = new SpriteBatch();
        viewport = new FitViewport(8, 5);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true); // true centers the camera
    }

    @Override
    public void render() {
        input();
        logic();
        draw();
    }

    @Override
    public void pause() {
        // Invoked when your application is paused.
    }

    @Override
    public void resume() {
        // Invoked when your application is resumed after pause.
    }

    @Override
    public void dispose() {
        // Destroy application's resources here.
    }

    private void input() {

    }

    private void logic() {

    }

    private void draw() {
        ScreenUtils.clear(Color.BLACK);
        viewport.apply();
        spriteBatch.setProjectionMatrix(viewport.getCamera().combined);
        spriteBatch.begin();
        // store the worldWidth and worldHeight as local variables for brevity
        float worldWidth = viewport.getWorldWidth();
        float worldHeight = viewport.getWorldHeight();

        spriteBatch.draw(backgroundTexture, 0, 0, worldWidth, worldHeight); // draw the background
        spriteBatch.end();
    }

//    public static class Item {
//        private String name;
//        private Vector2 position;
//        private Texture texture;
//        private String imagePath;
//        private boolean isCollected;  // Indica se o item foi coletado
//        private boolean wasCounted;   // Indica se o item foi contabilizado (coletado) pelo personagem
//
//        // Construtor modificado para aceitar 4 parâmetros
//        public Item(String name, float x, float y, String imagePath) {
//            this.name = name;
//            this.position = new Vector2(x, y);
//            this.imagePath = imagePath;
//            this.texture = new Texture(imagePath);
//            this.isCollected = false; // Inicializa como não coletado
//            this.wasCounted = false;  // Inicializa como não contado
//        }
//
//        public void render(SpriteBatch batch) {
//            if (!isCollected) {  // Só desenha o item se ele não foi coletado
//                batch.draw(texture, position.x, position.y);
//            }
//        }
//
//        // Método que verifica se o item foi coletado
//        public boolean isCollected() {
//            return isCollected;
//        }
//
//        // Método para marcar o item como coletado
//        public void collect() {
//            if (!isCollected) {
//                isCollected = true;  // Marca o item como coletado
//            }
//        }
//
//        // Método para verificar se o item foi contado (coletado e processado)
//        public boolean wasCounted() {
//            return wasCounted;
//        }
//
//        // Método para marcar o item como contado (processado após coleta)
//        public void setWasCounted(boolean wasCounted) {
//            this.wasCounted = wasCounted;
//        }
//
//        public void update(Rectangle playerBounds) {
//            // Aqui você pode adicionar a lógica para detectar colisões entre o personagem e o item
//            if (!isCollected && playerBounds.overlaps(new Rectangle(position.x, position.y, texture.getWidth(), texture.getHeight()))) {
//                collect(); // Marca o item como coletado quando o personagem colide com ele
//            }
//        }
//
//        public void dispose() {
//            if (texture != null) {
//                texture.dispose();
//            }
//        }
//
//        // Métodos getter
//        public Vector2 getPosition() {
//            return position;
//        }
//
//        public Texture getTexture() {
//            return texture;
//        }
//
//        public String getName() {
//            return name;
//        }
//
//        public String getImagePath() {
//            return imagePath;
//        }
//    }
}
