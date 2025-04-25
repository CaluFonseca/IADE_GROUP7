package com.badlogic.UniverseConqueror.Attacks;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.graphics.OrthographicCamera;

public class Fireball {
    private Vector2 position;
    private Vector2 velocity;
    private static final float SPEED = 600f; // Velocidade do fireball
    private static Texture texture; // Usar textura estática partilhada
    private static TextureRegion textureRegion; // TextureRegion para renderização
    private Rectangle bounds;

    public Fireball(float x, float y, Vector2 target) {
        // Carrega a textura uma vez (boas práticas)
        if (texture == null) {
            texture = new Texture("fireball.png"); // Deve estar em assets
            textureRegion = new TextureRegion(texture); // Cria o TextureRegion
        }

        float scale = 0.2f; // Tamanho ajustado do fireball
        float fireballWidth = textureRegion.getRegionWidth() * scale;
        float fireballHeight = textureRegion.getRegionHeight() * scale;

        // Posiciona o fireball para sair do centro
        this.position = new Vector2(x - fireballWidth / 2, y - fireballHeight / 2);

        this.bounds = new Rectangle(position.x, position.y, fireballWidth, fireballHeight);

        // Define a direção e velocidade do fireball
        Vector2 direction = target.cpy().sub(position).nor();
        this.velocity = direction.scl(SPEED);
    }

    public void update(float delta) {
        position.mulAdd(velocity, delta); // Atualiza a posição com delta time
        bounds.setPosition(position);
    }

    public void render(SpriteBatch batch) {
        float scale = 0.2f;
        float width = textureRegion.getRegionWidth() * scale;
        float height = textureRegion.getRegionHeight() * scale;

        // Calcula o ângulo do fireball com base na direção
        float angle = velocity.angleDeg();

        // Aplica rotação, centraliza a origem da rotação
        batch.draw(textureRegion,
            position.x, position.y,
            width / 2, height / 2, // origem para rotação
            width, height,
            1f, 1f, // escala
            angle
        );
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public boolean isOutOfBounds(OrthographicCamera camera) {
        float margin = 100f;
        return position.x < camera.position.x - camera.viewportWidth / 2 - margin ||
            position.x > camera.position.x + camera.viewportWidth / 2 + margin ||
            position.y < camera.position.y - camera.viewportHeight / 2 - margin ||
            position.y > camera.position.y + camera.viewportHeight / 2 + margin;
    }

    public static void disposeTexture() {
        if (texture != null) {
            texture.dispose();
            texture = null;
            textureRegion = null; // Limpa o TextureRegion
        }
    }
}
