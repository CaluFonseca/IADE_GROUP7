package com.badlogic.UniverseConqueror.Attacks;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.graphics.OrthographicCamera;

public class Bullet {
    private Vector2 position;
    private Vector2 velocity;
    private static final float SPEED = 500f;
    private static Texture texture; // Usar textura estática partilhada para evitar criar várias vezes
    private Rectangle bounds;


    public Bullet(float x, float y, Vector2 target) {
        // Carrega a textura uma vez (boas práticas)
        if (texture == null) {
            texture = new Texture("bullet.png"); // Deve estar em assets
        }

        float scale = 0.1f;
        float bulletWidth = texture.getWidth() * scale;
        float bulletHeight = texture.getHeight() * scale;

        // Posiciona a bala para sair do centro (ajusta o offset pela metade do tamanho)
        this.position = new Vector2(x - bulletWidth / 2, y - bulletHeight / 2);

        this.bounds = new Rectangle(position.x, position.y, 2, 2);

        Vector2 direction = target.cpy().sub(position).nor(); // Direção normalizada
        this.velocity = direction.scl(SPEED); // Velocidade vetorial
    }

    public void update(float delta) {
        position.mulAdd(velocity, delta); // Atualiza posição com delta time
        bounds.setPosition(position);
    }

    public void render(SpriteBatch batch) {
        float scale = 0.1f;
        float width = texture.getWidth() * scale;
        float height = texture.getHeight() * scale;

        // Calcula o ângulo da bala em graus com base no vetor de velocidade
        float angle = velocity.angleDeg();

        // Aplica rotação, centraliza a origem da rotação
        batch.draw(texture,
            position.x, position.y,
            width / 2, height / 2, // origem para rotação
            width, height,
            1f, 1f, // escala
            angle,
            0, 0,
            texture.getWidth(), texture.getHeight(),
            false, false
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

    // Evita chamar diretamente em cada bala, pois é uma textura partilhada
    public static void disposeTexture() {
        if (texture != null) {
            texture.dispose();
            texture = null;
        }
    }
}
