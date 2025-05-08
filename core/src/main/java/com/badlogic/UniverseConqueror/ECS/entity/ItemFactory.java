package com.badlogic.UniverseConqueror.ECS.entity;

import com.badlogic.UniverseConqueror.ECS.components.ItemComponent;
import com.badlogic.UniverseConqueror.ECS.components.TextureComponent;
import com.badlogic.UniverseConqueror.ECS.components.TransformComponent;
import com.badlogic.UniverseConqueror.ECS.components.BodyComponent;
import com.badlogic.UniverseConqueror.ECS.components.PositionComponent;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.physics.box2d.*;

public class ItemFactory {
    private Texture itemTexture;
    private String name;
    private float x, y;
    private String texturePath;
    private Entity entity;

    public ItemFactory(String name, float x, float y, String texturePath) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.texturePath = texturePath;
        itemTexture = new Texture(Gdx.files.internal(texturePath));
    }

    public Entity createEntity(PooledEngine engine, World world) {
        // Create a new entity
        entity = engine.createEntity();
        System.out.println("Criando entidade para o item: " + name);
        // Create and add position component
        PositionComponent position = engine.createComponent(PositionComponent.class);
        position.position.set(x, y);
        entity.add(position);

        TransformComponent transformComponent = engine.createComponent(TransformComponent.class);
        transformComponent.position.set(x, y,0);  // Defina a posição para o item
        entity.add(transformComponent);
        // Create and add Texture component (for visual representation)
        TextureComponent textureComponent = engine.createComponent(TextureComponent.class);
        textureComponent.texture = itemTexture;  // Atribui a textura do item.
        entity.add(textureComponent);  // Adiciona o componente Texture à entidade

        // Create and add a collision component (Box2D)
        BodyComponent bodyComponent = engine.createComponent(BodyComponent.class);
        bodyComponent.body = createBody(world);
        entity.add(bodyComponent);

        ItemComponent itemComponent = engine.createComponent(ItemComponent.class);
        itemComponent.name = name;
        entity.add(itemComponent);
//        // Create and add a tag for item (optional)
//        TagComponent tag = engine.createComponent(TagComponent.class);
//        tag.tag = "Item";
//        entity.add(tag);

        return entity;
    }

    private Body createBody(World world) {
//        // Define the Box2D body
//        BodyDef bodyDef = new BodyDef();
//        bodyDef.type = BodyDef.BodyType.StaticBody;
//        bodyDef.position.set(x, y);
//
//        Body body = world.createBody(bodyDef);
//
//        // Create a rectangle shape for the body
//        FixtureDef fixtureDef = new FixtureDef();
//        PolygonShape shape = new PolygonShape();
//        shape.setAsBox(48 / 2f, 48 / 2f);  // Defina a largura e a altura do retângulo
//        fixtureDef.shape = shape;
//        fixtureDef.density = 1f;
//
//        body.createFixture(fixtureDef);
//        body.setUserData(this);  // Optionally link the item object to the body
//
//        return body;
        // Define o corpo Box2D como estático
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set(x, y);

        Body body = world.createBody(bodyDef);

        // Cria uma forma retangular
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(itemTexture.getWidth() / 2f, itemTexture.getHeight() / 2f);  // metade do tamanho

        // Define o fixture como sensor (detecção sem colisão)
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.isSensor = true;  // ← MUITO IMPORTANTE

        body.createFixture(fixtureDef);
        body.setUserData(entity);  // Associa a entidade (ou this) ao corpo

        shape.dispose();  // Libera recursos da forma

        return body;
    }
}
