package com.badlogic.UniverseConqueror.Utils;

import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;

public class MapCollisionLoader {
    private final float tileWidth;
    private final float tileHeight;
    private final float mapHeight;
    private final float ppm; // Pixels por metro

    public MapCollisionLoader(TiledMap map, float ppm) {
        this.ppm = ppm;
        this.tileWidth = map.getProperties().get("tilewidth", Integer.class);
        this.tileHeight = map.getProperties().get("tileheight", Integer.class);
        this.mapHeight = map.getProperties().get("height", Integer.class) ;
    }

    /**
     * Cria os corpos de colisão Box2D a partir de uma camada específica do mapa.
     */
    public void createCollisionBodies(World world, TiledMap map, String layerName) {
        MapLayer layer = map.getLayers().get(layerName);

        if (layer == null) {
            System.out.println("Camada '" + layerName + "' não encontrada.");
            return;
        }

        for (MapObject object : layer.getObjects()) {
            if (!(object instanceof RectangleMapObject)) continue;

            Rectangle rect = ((RectangleMapObject) object).getRectangle();

            // Usa coordenadas convertidas para isométricas
            Vector2 isoPosition = convertToWorldCoordinates(rect);

            short categoryBits = getCategoryBitsForLayer(layerName);
            boolean isSensor = isSensorLayer(layerName);
           /* System.out.println("[DEBUG] Corpo criado na posição: " + isoPosition +
                " | Tamanho: " + (width / ppm) + "x" + (height / ppm) +
                " | Categoria: " + categoryBits +
                " | Sensor: " + isSensor);
            createBox2DBody(world, isoPosition, rect.width, rect.height, categoryBits, isSensor);*/
        }
    }

    /**
     * Converte coordenadas ortogonais do mapa para mundo isométrico.
     */
    private Vector2 convertToWorldCoordinates(Rectangle rect) {
        float centerX = rect.x + rect.width / 2f;
        float centerY = rect.y + rect.height / 2f;

        // Conversão ortogonal → isométrico
        float isoX = (centerX - centerY) * (tileWidth / 2f);
        float isoY = (centerX + centerY) * (tileHeight / 2f);

        // Corrigir origem: subtrair meio da altura do mapa em iso
        isoY -= mapHeight * (tileHeight / 2f) / tileHeight;

        return new Vector2(isoX / ppm, isoY / ppm);
    }

    /**
     * Cria um corpo estático Box2D a partir da posição e dimensões fornecidas.
     */
    private void createBox2DBody(World world, Vector2 isoPosition, float width, float height, short categoryBits, boolean isSensor) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody; // Corrigido para estático
        bodyDef.position.set(isoPosition);

        Body body = world.createBody(bodyDef);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox((width / 2f) / ppm, (height / 2f) / ppm); // Corrigido: escala por PPM

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.friction = 0.8f;
        fixtureDef.isSensor = isSensor;

        fixtureDef.filter.categoryBits = categoryBits;
        fixtureDef.filter.maskBits = -1;

        body.createFixture(fixtureDef);

        body.setUserData(new Rectangle(
            isoPosition.x - (width / 2f) / ppm,
            isoPosition.y - (height / 2f) / ppm,
            width / ppm,
            height / ppm
        ));
        System.out.println("[DEBUG] Corpo criado na posição: " + isoPosition +
            " | Tamanho: " + (width / ppm) + "x" + (height / ppm) +
            " | Categoria: " + categoryBits +
            " | Sensor: " + isSensor);
        shape.dispose();
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
