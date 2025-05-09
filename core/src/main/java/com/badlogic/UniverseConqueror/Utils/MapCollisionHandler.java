package com.badlogic.UniverseConqueror.Utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;

public class MapCollisionHandler {
    private final Array<Rectangle> collisionRects;
    private final Array<Rectangle> jumpableRects;  // Added for the "jumpable" layer
    private final float tileWidth;
    private final float tileHeight;
    private TiledMapTileLayer layer;
    private TiledMapTileLayer jumpableLayer;  // "jumpable" layer

    // Constructor
    public MapCollisionHandler(TiledMap map, String layerName, String jumpableLayerName) {
        collisionRects = new Array<>();
        jumpableRects = new Array<>();

        // Getting the tile dimensions
        this.tileWidth = map.getProperties().get("tilewidth", Integer.class);
        this.tileHeight = map.getProperties().get("tileheight", Integer.class);

        // Retrieve the map layers
        MapLayer mapLayer = map.getLayers().get(layerName);
        MapLayer jumpableMapLayer = map.getLayers().get(jumpableLayerName);

        if (!(mapLayer instanceof TiledMapTileLayer)) {
            Gdx.app.error("Collision", "Layer not found or not a valid tile layer: " + layerName);
            return;
        }

        if (!(jumpableMapLayer instanceof TiledMapTileLayer)) {
            Gdx.app.error("Jumpable", "Jumpable Layer not found or not a valid tile layer: " + jumpableLayerName);
            return;
        }

        this.layer = (TiledMapTileLayer) mapLayer;
        this.jumpableLayer = (TiledMapTileLayer) jumpableMapLayer;

        int width = layer.getWidth();
        int height = layer.getHeight();

        // Offset to help position the tiles correctly in an isometric space
        float tileOffsetX = tileWidth;
        float tileOffsetY = tileHeight;

        // Calculate the origin point based on the map size and tile dimensions
        float originX = (width + height) * tileWidth / 4f + tileWidth / 2f - tileOffsetX;
        float originY = - (tileHeight / 2f) * height + tileOffsetY;

        // Iterating through each tile cell in the main layer
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                TiledMapTileLayer.Cell cell = layer.getCell(x, y);
                TiledMapTileLayer.Cell jumpableCell = jumpableLayer.getCell(x, y);

                if (cell != null) {
                    // Logic for the normal collision layer (not "jumpable")
                    int rotatedX = y;
                    int rotatedY = width - 1 - x;
                    float worldX = (rotatedX - rotatedY) * tileWidth / 2f + originX;
                    float worldY = (rotatedX + rotatedY) * tileHeight / 2f + originY;

                    Rectangle isoRect = new Rectangle(worldX, worldY - tileHeight / 2f, tileWidth, tileHeight);
                    collisionRects.add(isoRect);
                }

                // Logic for the "jumpable" layer
                if (jumpableCell != null) {
                    int rotatedX = y;
                    int rotatedY = width - 1 - x;
                    float worldX = (rotatedX - rotatedY) * tileWidth / 2f + originX;
                    float worldY = (rotatedX + rotatedY) * tileHeight / 2f + originY;

                    // Generating a rectangle for the "jumpable" layer
                    Rectangle isoRect = new Rectangle(worldX, worldY - tileHeight / 2f, tileWidth, tileHeight);
                    jumpableRects.add(isoRect);
                }
            }
        }
    }

    // Method to create Box2D bodies from the rectangles
    public void createBox2DBodies(World world) {
        // Creating Box2D bodies for collision layer
        for (Rectangle rect : collisionRects) {
          //  createBox2DBody(world, rect, (short)0x0001, false);  // Normal collision body
            createIsometricDiamond(world, rect, 256f, 128f, (short)0x0001, false, 4f);
        }

        // Creating Box2D bodies for jumpable layer
        for (Rectangle rect : jumpableRects) {
            createBox2DBody(world, rect, (short)0x0001, true);  // Jumpable layer body (as sensor)
        }
    }

    public Body createIsometricDiamond(World world, Rectangle pixelPosition, float widthPx, float heightPx, short categoryBits, boolean isSensor, float ppm) {
        // Conversion to meters
        Vector2 position = new Vector2(pixelPosition.x, pixelPosition.y);

        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set(position.x+128, position.y+64);

        Body body = world.createBody(bodyDef);

        float width = widthPx;
        float height = heightPx;

        PolygonShape shape = new PolygonShape();
        Vector2[] vertices = new Vector2[4];

        // Create the diamond (clockwise)
        vertices[0] = new Vector2(0, height / 2f);        // top
        vertices[1] = new Vector2(width / 2f, 0);         // right
        vertices[2] = new Vector2(0, -height / 2f);       // bottom
        vertices[3] = new Vector2(-width / 2f, 0);        // left

        shape.set(vertices);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.friction = 0.8f;
        fixtureDef.isSensor = isSensor;
        fixtureDef.filter.categoryBits = categoryBits;
        fixtureDef.filter.maskBits = -1;

        body.createFixture(fixtureDef);

        // Adjustment for rendering/debug purposes
        body.setUserData(new Rectangle(
            pixelPosition.x - widthPx / 2f,
            pixelPosition.y - heightPx / 2f,
            widthPx,
            heightPx
        ));

        shape.dispose();
        return body;
    }


    // Method to create a Box2D body from a Rectangle (Collision or Jumpable)
    private void createBox2DBody(World world, Rectangle rect, short categoryBits, boolean isSensor) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody; // Static body for map objects
        bodyDef.position.set(rect.x + rect.width / 2, rect.y + rect.height / 2);

        Body body = world.createBody(bodyDef);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(rect.width / 2, rect.height / 2); // Create a box shape with half the width/height

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.friction = 0.8f;
        fixtureDef.isSensor = isSensor;  // If it's jumpable, make it a sensor
        fixtureDef.filter.categoryBits = categoryBits;
        fixtureDef.filter.maskBits = -1;  // Collide with everything

        body.createFixture(fixtureDef);
        shape.dispose();
    }

    // Getter methods for collision and jumpable rects
    public Array<Rectangle> getCollisionRects() {
        return collisionRects;
    }

    public Array<Rectangle> getJumpableRects() {
        return jumpableRects;
    }
}
