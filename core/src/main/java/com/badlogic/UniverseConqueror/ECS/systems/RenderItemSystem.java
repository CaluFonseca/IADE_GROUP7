package com.badlogic.UniverseConqueror.ECS.systems;

import com.badlogic.UniverseConqueror.ECS.components.ItemComponent;
import com.badlogic.UniverseConqueror.ECS.components.TextureComponent;
import com.badlogic.UniverseConqueror.ECS.components.TransformComponent;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class RenderItemSystem extends EntitySystem {
    private SpriteBatch batch;
    private ComponentMapper<TextureComponent> tm = ComponentMapper.getFor(TextureComponent.class);
    private ComponentMapper<TransformComponent> trm = ComponentMapper.getFor(TransformComponent.class);
    private ComponentMapper<ItemComponent> im = ComponentMapper.getFor(ItemComponent.class);
    private OrthographicCamera camera;

    private Family renderableItems = Family.all(TextureComponent.class, TransformComponent.class, ItemComponent.class).get();

    public RenderItemSystem(SpriteBatch batch, OrthographicCamera camera) {
        this.batch = batch;
        this.camera = camera;
    }

    @Override
    public void update(float deltaTime) {

        batch.setProjectionMatrix(camera.combined);
        System.out.println("Iniciando renderização...");

        // Inicia a renderização
        batch.begin();

        // Itera sobre as entidades e renderiza as texturas
        int count = 0;
        for (Entity entity : getEngine().getEntitiesFor(Family.all(TextureComponent.class, TransformComponent.class, ItemComponent.class).get())) {
            TextureComponent texture = tm.get(entity);
            TransformComponent transform = trm.get(entity);

            if (texture.texture != null) {
                batch.draw(texture.texture, transform.position.x, transform.position.y);
            }
        }
        batch.end();
    }
}
