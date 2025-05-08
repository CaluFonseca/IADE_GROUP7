package com.badlogic.UniverseConqueror.Utils;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.UniverseConqueror.characters.Character;

public class Minimap extends Actor {
    private Texture minimapTexture; // Texture for the minimap background
    private Texture characterMarker; // Marker for the character on the minimap
    private Vector2 minimapPosition; // Position of the minimap on the screen
    private float minimapWidth; // Width of the minimap
    private float minimapHeight; // Height of the minimap
    private Character player; // Reference to the player character
    private float worldWidth; // World width (used for scaling the character's position to the minimap)
    private float worldHeight; // World height (used for scaling the character's position to the minimap)

    // Constructor to initialize the minimap with textures, position, and size
    public Minimap(String minimapTexturePath, String characterMarkerPath, float x, float y, float width, float height, Character player) {
        this.minimapTexture = new Texture(minimapTexturePath);  // Load the minimap background texture
        this.characterMarker = new Texture(characterMarkerPath); // Load the character marker texture
        this.minimapPosition = new Vector2(x, y); // Set the position of the minimap
        this.minimapWidth = width; // Set the width of the minimap
        this.minimapHeight = height; // Set the height of the minimap
        this.player = player; // Set the player character
        setPosition(x, y); // Set the actor's position on the stage
        setSize(width, height); // Set the size of the minimap actor
    }

    // Set the world size (used for scaling the player's position to the minimap)
    public void setWorldSize(float worldWidth, float worldHeight) {
        this.worldWidth = worldWidth; // Set the world width
        this.worldHeight = worldHeight; // Set the world height
    }

    // This method is responsible for drawing the minimap on the screen
    @Override
    public void draw(Batch batch, float parentAlpha) {
        batch.draw(minimapTexture, getX(), getY(), getWidth(), getHeight()); // Draw the minimap background

        Vector2 characterPosition = getCharacterPosition(); // Get the player's position
        if (characterPosition != null && worldWidth > 0 && worldHeight > 0) {
            // Scale the character's position to the minimap coordinates
            float scale = 0.01f; // Adjust this value to scale the character correctly on the minimap
            float scaledX = getX() + (characterPosition.x * scale) - 10; // Calculate the scaled X position
            float scaledY = getY() + (characterPosition.y * scale) + 50; // Calculate the scaled Y position

            // Clamp the marker's position to stay within the minimap boundaries
            scaledX = MathUtils.clamp(scaledX, getX(), getX() + getWidth() - 10);
            scaledY = MathUtils.clamp(scaledY, getY(), getY() + getHeight() - 10);

            // Draw the character marker on the minimap
            batch.draw(characterMarker, scaledX, scaledY, 20, 20); // Adjust size of the marker if needed
        }
    }

    // Get the current position of the player on the minimap
    private Vector2 getCharacterPosition() {
        return player.getPosition(); // Return the player's position
    }

    // Dispose of the textures used by the minimap to free resources
    public void dispose() {
        minimapTexture.dispose();  // Dispose of the minimap background texture
        characterMarker.dispose(); // Dispose of the character marker texture
    }
}
