package com.badlogic.UniverseConqueror.Attacks;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.graphics.OrthographicCamera;

public class Bullet {
    private Vector2 position;      // Position of the bullet
    private Vector2 velocity;      // Velocity of the bullet
    private static final float SPEED = 500f; // Speed of the bullet
    private static Texture texture; // Static texture shared for all bullets to save memory
    private Rectangle bounds;      // Bounds for collision detection


    // Constructor to initialize the bullet with position and target
    public Bullet(float x, float y, Vector2 target) {
        // Load the texture once (good practice to avoid reloading the texture each time a bullet is created)
        if (texture == null) {
            texture = new Texture("bullet.png"); // Load texture from the assets folder
        }

        // Scale factor to adjust bullet size
        float scale = 0.1f;
        float bulletWidth = texture.getWidth() * scale;
        float bulletHeight = texture.getHeight() * scale;

        // Position the bullet so it starts from the center (adjusted by half the size)
        this.position = new Vector2(x - bulletWidth / 2, y - bulletHeight / 2);

        // Define the bounding rectangle for collision detection
        this.bounds = new Rectangle(position.x, position.y, 2, 2);

        // Calculate the direction vector from the bullet to the target and normalize it
        Vector2 direction = target.cpy().sub(position).nor();
        // Multiply direction by speed to get the velocity
        this.velocity = direction.scl(SPEED);
    }

    // Update the bullet position based on velocity and delta time
    public void update(float delta) {
        // Update the position using the velocity and delta time
        position.mulAdd(velocity, delta);
        // Update the bounding rectangle position to match the bullet's position
        bounds.setPosition(position);
    }

    // Render the bullet on the screen
    public void render(SpriteBatch batch) {
        float scale = 0.1f;
        float width = texture.getWidth() * scale;
        float height = texture.getHeight() * scale;

        // Calculate the angle of the bullet based on its velocity
        float angle = velocity.angleDeg();

        // Draw the bullet, applying rotation and scaling
        batch.draw(texture,
            position.x, position.y,
            width / 2, height / 2, // Set origin for rotation (center of the bullet)
            width, height, // Set width and height of the bullet
            1f, 1f, // Scaling
            angle, // Apply rotation
            0, 0, // Source rectangle in the texture (top left corner)
            texture.getWidth(), texture.getHeight(),
            false, false // Don't flip the texture
        );
    }

    // Get the bounding box for the bullet (used for collision detection)
    public Rectangle getBounds() {
        return bounds;
    }

    // Check if the bullet is out of the camera's view (with a margin)
    public boolean isOutOfBounds(OrthographicCamera camera) {
        float margin = 100f; // Margin to check if the bullet is out of the camera's view
        return position.x < camera.position.x - camera.viewportWidth / 2 - margin ||
            position.x > camera.position.x + camera.viewportWidth / 2 + margin ||
            position.y < camera.position.y - camera.viewportHeight / 2 - margin ||
            position.y > camera.position.y + camera.viewportHeight / 2 + margin;
    }

    // Dispose method to clean up the texture (called statically)
    // This avoids having to dispose the texture in every bullet instance since it's shared
    public static void disposeTexture() {
        if (texture != null) {
            texture.dispose();  // Dispose of the texture to free memory
            texture = null;     // Set the texture reference to null
        }
    }

    // Dispose method to clean up the resources when the bullet is no longer needed
    public void dispose() {
        if (texture != null) {
            texture.dispose();  // Dispose of the texture when the bullet is disposed
        }
    }
}
