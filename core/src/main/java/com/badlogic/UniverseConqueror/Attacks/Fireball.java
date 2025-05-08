package com.badlogic.UniverseConqueror.Attacks;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

public class Fireball {
    private Vector2 position;      // Center position of the fireball
    private Vector2 velocity;      // Velocity of the fireball
    private static final float SPEED = 800f;  // Speed of the fireball
    private static Texture texture; // Static texture for the fireball
    private Rectangle bounds;      // Bounds for collision detection

    private ParticleEffect particleEffect; // Particle effect for trail

    private static final float SCALE = 0.1f; // Scale of the fireball

    // Constructor
    public Fireball(float x, float y, Vector2 target) {
        if (texture == null) {
            texture = new Texture(Gdx.files.internal("fireball.png"));
            texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        }

        // Calculate width/height based on texture and scale
        float width = texture.getWidth() * SCALE;
        float height = texture.getHeight() * SCALE;

        // Set position centered
        this.position = new Vector2(x, y);

        // Set bounds with real scaled size
        this.bounds = new Rectangle(position.x - width / 2, position.y - height / 2, width, height);

        // Direction calculation
        Vector2 direction = target.cpy().sub(position).nor();
        this.velocity = direction.scl(SPEED);

        // Particle Effect for trail
        particleEffect = new ParticleEffect();
        particleEffect.load(Gdx.files.internal("effects/fire_trail.p"), Gdx.files.internal("effects"));
        particleEffect.start();
    }

    public void update(float delta) {
        // Update position
        position.mulAdd(velocity, delta);

        // Update bounds centered
        bounds.setPosition(position.x - bounds.width / 2, position.y - bounds.height / 2);

        // Update particles
        particleEffect.setPosition(position.x, position.y);
        particleEffect.update(delta);
    }

    public void render(SpriteBatch batch) {
        // Draw particle effect first (behind the fireball)
        particleEffect.draw(batch);

        float width = texture.getWidth() * SCALE;
        float height = texture.getHeight() * SCALE;
        float angle = velocity.angleDeg();

        // Draw the fireball centered
        batch.draw(texture,
            position.x - width / 2, position.y - height / 2,
            width / 2, height / 2, // origin at center
            width, height,
            1f, 1f,
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

    public void dispose() {

        if (particleEffect != null) {
            particleEffect.dispose();
        }
    }
}
