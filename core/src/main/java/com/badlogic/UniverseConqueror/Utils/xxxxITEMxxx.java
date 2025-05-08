package com.badlogic.UniverseConqueror.Utils;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class xxxxITEMxxx {
    private String name; // Name of the item
    private Vector2 position; // Position of the item in the game world
    private Texture texture; // Texture for the item (image to be rendered)
    private String imagePath; // Path to the item's texture image
    private boolean isCollected; // Flag to track if the item has been collected

    // Constructor to initialize the item with name, position, and image path
    public xxxxITEMxxx(String name, float x, float y, String imagePath) {
        this.name = name; // Set item name
        this.position = new Vector2(x, y); // Set position of the item
        this.imagePath = imagePath; // Set image path for the texture
        this.texture = new Texture(imagePath); // Load the texture
        this.isCollected = false; // Set the initial state as not collected
    }

    // Method to render the item on the screen if it has not been collected
    public void render(SpriteBatch batch) {
        if (!isCollected) {  // Only draw the item if it has not been collected
            batch.draw(texture, position.x, position.y); // Draw the item texture at the specified position
        }
    }

    // Method to check if the item has been collected
    public boolean isCollected() {
        return isCollected; // Return the collection status of the item
    }

    // Method to mark the item as collected
    public void collect() {
        isCollected = true; // Set the item as collected
    }

    // Method to update the item state based on the player's collision
    public void update(Rectangle playerBounds) {
        // Check for collision between the item and the player's bounding box
        if (!isCollected && playerBounds.overlaps(new Rectangle(position.x, position.y, texture.getWidth(), texture.getHeight()))) {
            collect(); // Mark the item as collected if the player collides with it
        }
    }

    // Method to create a copy of the item
    public xxxxITEMxxx copy() {
        return new xxxxITEMxxx(name, position.x, position.y, imagePath); // Create a new instance with the same properties
    }

    // Getter for the item's name
    public String getName() {
        return name; // Return the name of the item
    }

    // Dispose of the texture to free up resources
    public void dispose() {
        if (texture != null) {
            texture.dispose(); // Dispose the texture to free up memory
        }
    }
}
