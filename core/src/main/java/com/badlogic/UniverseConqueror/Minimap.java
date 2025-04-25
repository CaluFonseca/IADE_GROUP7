package com.badlogic.UniverseConqueror;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.UniverseConqueror.characters.Character;


public class Minimap extends Actor {
    private Texture minimapTexture;
    private Texture characterMarker; // Marker for the character on the minimap
    private Vector2 minimapPosition;
    private float minimapWidth;
    private float minimapHeight;
    private Character player;
    private float worldWidth;  // Example, replace with actual world width
    private float worldHeight;

    public Minimap(String minimapTexturePath, String characterMarkerPath, float x, float y, float width, float height,Character player ) {
        this.minimapTexture = new Texture(minimapTexturePath);  // Load the minimap background
        this.characterMarker = new Texture(characterMarkerPath); // Load character marker
        this.minimapPosition = new Vector2(x, y);
        this.minimapWidth = width;
        this.minimapHeight = height;
        this.player=player;
        // Set the actor's position to the minimap's position
        setPosition(x, y);
        setSize(width, height);
    }

    public void setWorldSize(float worldWidth, float worldHeight) {
        this.worldWidth = worldWidth;
        this.worldHeight = worldHeight;
    }




    @Override
    public void draw(Batch batch, float parentAlpha) {
         batch.draw(minimapTexture, getX(), getY(), getWidth(), getHeight());

         Vector2 characterPosition = getCharacterPosition();
         if (characterPosition != null && worldWidth > 0 && worldHeight > 0) {
             // Ajuste de escala para reduzir a diferença de tamanho
             float scale = 0.01f; // Ajuste esse valor conforme necessário
             float scaledX = getX() + (characterPosition.x * scale)-10;
             float scaledY = getY() + (characterPosition.y * scale)+50;
             // Limita a posição do marcador dentro do minimap
             scaledX = MathUtils.clamp(scaledX, getX(), getX() + getWidth() - 10);
             scaledY = MathUtils.clamp(scaledY, getY(), getY() + getHeight() - 10);

       // Debug: Verificar valores das coordenadas
   //    System.out.println("Character Position: " + characterPosition.x + ", " + characterPosition.y);
     //  System.out.println("Scaled Position: " + scaledX + ", " + scaledY);
       //System.out.println("Minimap Dimensions: " + getWidth() + "x" + getHeight());
       //System.out.println("World Dimensions: " + worldWidth + "x" + worldHeight);
             batch.draw(characterMarker, scaledX , scaledY , 20, 20); // Tamanho ajustável
       }
    }


    // This method should be updated to get the actual character's position
    private Vector2 getCharacterPosition() {
        Vector2 position = player.getPosition();
       // System.out.println("Character Position: " + position.x + ", " + position.y);
        return position;
    }

    // Render method to be called from your game loop, passing the character position
//    public void render(SpriteBatch batch, Vector2 characterPosition, float worldWidth, float worldHeight) {
//        // Draw the minimap background
//        batch.draw(minimapTexture, minimapPosition.x, minimapPosition.y, minimapWidth, minimapHeight);
//        this.worldWidth = worldWidth;
//        this.worldHeight = worldHeight;
//        // Scale character position to minimap coordinates
//        if (worldWidth > 0 && worldHeight > 0) {
//            float scaledX = minimapPosition.x + (characterPosition.x / worldWidth) * minimapWidth;
//            float scaledY = minimapPosition.y + (characterPosition.y / worldHeight) * minimapHeight;
//
//            // Debug: Print scaled position to verify it's being calculated correctly
//            System.out.println("Scaled Character Position (Render): " + scaledX + ", " + scaledY);
//
//            // Draw the character's position on the minimap
//            batch.draw(characterMarker, scaledX - 5, scaledY - 5, 10, 10);  // Adjust size if needed
//        }
//    }

    // Dispose textures
    public void dispose() {
        minimapTexture.dispose();
        characterMarker.dispose();
    }
}
