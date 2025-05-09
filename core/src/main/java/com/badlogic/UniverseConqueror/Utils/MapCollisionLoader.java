package com.badlogic.UniverseConqueror.Utils;

import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;

public class MapCollisionLoader {
    private final float tileWidth;
    private final float tileHeight;
    private final float mapHeight, mapWidth;
    private final float ppm; // Pixels per meter
    private final float mapWidthInTiles, mapHeightInTiles, mapCenterIsoX, mapCenterIsoY;
    private final Array<Rectangle> collisionRects;

    public MapCollisionLoader(TiledMap map, float ppm) {
        this.ppm = ppm;
        this.tileWidth = map.getProperties().get("tilewidth", Integer.class);
        this.tileHeight = map.getProperties().get("tileheight", Integer.class);
        this.mapHeight = map.getProperties().get("height", Integer.class);
        this.mapWidth = map.getProperties().get("width", Integer.class);

        mapWidthInTiles = map.getProperties().get("width", Integer.class);
        mapHeightInTiles = map.getProperties().get("height", Integer.class);
        mapCenterIsoX = (mapWidthInTiles - mapHeightInTiles) * tileWidth / 2f;
        mapCenterIsoY = (mapWidthInTiles + mapHeightInTiles) * tileHeight / 4f;
        collisionRects = new Array<>();
    }

    public void createCollisionBodies(World world, TiledMap map, String layerName) {
        MapLayer layer = map.getLayers().get(layerName);

        if (layer == null) {
            System.out.println("Layer '" + layerName + "' not found.");
            return;
        }

        for (MapObject object : layer.getObjects()) {
            if (!(object instanceof RectangleMapObject)) continue;

            Rectangle rect = ((RectangleMapObject) object).getRectangle();

            // Convert coordinates to isometric world space
            Rectangle isoPosition = convertToWorldCoordinates(rect);

            short categoryBits = getCategoryBitsForLayer(layerName);
            boolean isSensor = isSensorLayer(layerName);
            createIsometricDiamond(world, isoPosition, 256f, 128f, categoryBits, false, Constants.PPM);
        }
    }

    /**
     * Converts orthogonal map coordinates to isometric world coordinates.
     */
    private Rectangle convertToWorldCoordinates(Rectangle rect) {
        // Calculate the center of the rectangle (tile)
        float centerX = rect.x + rect.width / 2f;
        float centerY = rect.y + rect.height / 2f;

        // Convert to isometric coordinates
        float isoX = centerX - centerY;
        float isoY = (centerX + centerY) / 2f;

        // Adjust the map center to correctly align the tiles
        float finalX = (isoX + mapCenterIsoX) / ppm; // Centralize on the X axis
        float finalY = (isoY - mapCenterIsoY) / ppm; // Centralize on the Y axis

        // Print the calculated positions for debugging
        System.out.println("Tile (rect): " + rect.x + ", " + rect.y + " | Isometric position: " + finalX + ", " + finalY);

        // Create a rectangle in world space with the adjusted position
        return new Rectangle(finalX, finalY, rect.width / ppm, rect.height / ppm);
    }

    /**
     * Creates a static Box2D body from the position and dimensions.
     */
    public Body createIsometricDiamond(World world, Rectangle pixelPosition, float widthPx, float heightPx, short categoryBits, boolean isSensor, float ppm) {
        // Conversion to meters
        Vector2 position = new Vector2(pixelPosition.x+128, pixelPosition.y);

        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set(position.x, position.y);

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

    private short getCategoryBitsForLayer(String layerName) {
        switch (layerName.toLowerCase()) {
            case "collisions": return 0x0001;
            case "jumpable":   return 0x0002;
            default:           return 0x0004;
        }
    }

    private boolean isSensorLayer(String layerName) {
        return layerName.equalsIgnoreCase("jumpable");
    }

    public static Vector2 toIso(float x, float y) {
        return new Vector2(x - y, (x + y) / 2);
    }

    public static Vector2 toOrtho(float isoX, float isoY) {
        return new Vector2((2 * isoY + isoX) / 2, (2 * isoY - isoX) / 2);
    }
}
