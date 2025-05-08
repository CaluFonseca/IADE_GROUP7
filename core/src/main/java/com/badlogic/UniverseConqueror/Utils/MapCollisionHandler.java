package com.badlogic.UniverseConqueror.Utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

public class MapCollisionHandler {
    private final Array<Rectangle> collisionRects;
    private final Array<Rectangle> jumpableRects;  // Adicionado para a camada "jumpable"
    private final float tileWidth;
    private final float tileHeight;
    private TiledMapTileLayer layer;
    private TiledMapTileLayer jumpableLayer;  // Camada "jumpable"

    // Constructor
    public MapCollisionHandler(TiledMap map, String layerName, String jumpableLayerName) {
        collisionRects = new Array<>();
        jumpableRects = new Array<>();

        // Obtendo as dimensões dos tiles
        this.tileWidth = map.getProperties().get("tilewidth", Integer.class);
        this.tileHeight = map.getProperties().get("tileheight", Integer.class);

        // Recupera as camadas do mapa
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

        // Offset para ajudar a posicionar os tiles corretamente em um espaço isométrico
        float tileOffsetX = tileWidth;
        float tileOffsetY = tileHeight;

        // Cálculo do ponto de origem baseado no tamanho do mapa e nas dimensões dos tiles
        float originX = (width + height) * tileWidth / 4f + tileWidth / 2f - tileOffsetX;
        float originY = - (tileHeight / 2f) * height + tileOffsetY;

        // Iterando por cada célula de tile na camada principal
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                TiledMapTileLayer.Cell cell = layer.getCell(x, y);
                TiledMapTileLayer.Cell jumpableCell = jumpableLayer.getCell(x, y);

                if (cell != null) {
                    // Lógica para a camada de colisão normal (não "jumpable")
                    int rotatedX = y;
                    int rotatedY = width - 1 - x;
                    float worldX = (rotatedX - rotatedY) * tileWidth / 2f + originX;
                    float worldY = (rotatedX + rotatedY) * tileHeight / 2f + originY;

                    Rectangle isoRect = new Rectangle(worldX, worldY - tileHeight / 2f, tileWidth, tileHeight);
                    collisionRects.add(isoRect);
                }

                // Lógica para a camada "jumpable"
                if (jumpableCell != null) {
                    int rotatedX = y;
                    int rotatedY = width - 1 - x;
                    float worldX = (rotatedX - rotatedY) * tileWidth / 2f + originX;
                    float worldY = (rotatedX + rotatedY) * tileHeight / 2f + originY;

                    // Gerando um retângulo para a camada "jumpable"
                    Rectangle isoRect = new Rectangle(worldX, worldY - tileHeight / 2f, tileWidth, tileHeight);
                    jumpableRects.add(isoRect);
                }
            }
        }
    }

    // Retorna os retângulos de colisão
    public Array<Rectangle> getCollisionRects() {
        return collisionRects;
    }

    // Retorna os retângulos da camada "jumpable"
    public Array<Rectangle> getJumpableRects() {
        return jumpableRects;
    }
}
