package com.badlogic.UniverseConqueror;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Item {
    private String name;
    private Vector2 position;
    private Texture texture;
    private String imagePath;
    private boolean isCollected; // Variável que indica se o item foi coletado

    // Construtor modificado para aceitar 4 parâmetros
    public Item(String name, float x, float y, String imagePath) {
        this.name = name;
        this.position = new Vector2(x, y);
        this.imagePath = imagePath;
        this.texture = new Texture(imagePath);
        this.isCollected = false; // Inicializa como não coletado
    }

    public void render(SpriteBatch batch) {
        if (!isCollected) {  // Só desenha o item se ele não tiver sido coletado
            batch.draw(texture, position.x, position.y);
        }
    }

    // Método que verifica se o item foi coletado
    public boolean isCollected() {
        return isCollected;
    }

    // Método para marcar o item como coletado
    public void collect() {
        isCollected = true;
    }

    public void update(Rectangle playerBounds) {
        // Aqui você pode adicionar a lógica para detectar colisões entre o personagem e o item
        if (!isCollected && playerBounds.overlaps(new Rectangle(position.x, position.y, texture.getWidth(), texture.getHeight()))) {
            collect(); // Marca o item como coletado quando o personagem colide com ele
        }
    }

    public Item copy() {
        return new Item(name, position.x, position.y, imagePath); // ou use uma propriedade texturePath se quiser mais flexibilidade
    }

    public String getName() {
        return name;
    }

    public void dispose() {
        if (texture != null) {
            texture.dispose();
        }
    }
}
