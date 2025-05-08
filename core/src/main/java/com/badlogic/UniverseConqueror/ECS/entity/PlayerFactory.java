package com.badlogic.UniverseConqueror.ECS.entity;

import com.badlogic.UniverseConqueror.ECS.components.*;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.ObjectMap;

public class PlayerFactory {


    public static Entity createPlayer(PooledEngine engine,
                                      Vector2 position,
                                      ObjectMap<StateComponent.State, Animation<TextureRegion>> animations,
                                      ObjectMap<String, Sound> sounds,
                                      World world) {
        Entity entity = engine.createEntity();

        // Criar e adicionar componentes relacionados à posição e transformações
        TransformComponent transform = engine.createComponent(TransformComponent.class);
        transform.position.set(position.x, position.y, 0);

//        PositionComponent positionComponent = engine.createComponent(PositionComponent.class);
//        positionComponent.position.set(position);

        // Criar componentes relacionados à física e Box2D
        PhysicsComponent physicsComponent = engine.createComponent(PhysicsComponent.class);
        BodyComponent bodyComponent = createBody(position, world);  // Cria o corpo físico
        physicsComponent.body = bodyComponent.body;

        // Outros componentes do jogador
        VelocityComponent velocity = engine.createComponent(VelocityComponent.class);
        StateComponent state = engine.createComponent(StateComponent.class);

        AnimationComponent anim = engine.createComponent(AnimationComponent.class);
        anim.animations.putAll(animations);

        SoundComponent sound = engine.createComponent(SoundComponent.class);
        sound.sounds.putAll(sounds);


        PositionComponent positionComponent = new PositionComponent();
        positionComponent.position.set(position.x, position.y);

     //   TransformComponent transformComponent = new TransformComponent(position.x, position.y);
        CameraComponent cameraComponent = new CameraComponent(); // precisa ter isto para seguir o player

//        ParticleComponent particle = engine.createComponent(ParticleComponent.class);
//        particle.effects.addAll(effects);

        AttackComponent attack = engine.createComponent(AttackComponent.class);
        JumpComponent jump = engine.createComponent(JumpComponent.class);
        PlayerComponent player = engine.createComponent(PlayerComponent.class);
        HealthComponent health = engine.createComponent(HealthComponent.class);

        // Adiciona os componentes à entidade
        entity.add(transform);
        entity.add(velocity);
        entity.add(state);
        entity.add(anim);
        entity.add(sound);
        entity.add(attack);
        entity.add(jump);
        entity.add(player);
        entity.add(physicsComponent);
        entity.add(positionComponent);
        entity.add(cameraComponent);
        entity.add(health);
        // Adiciona o componente de física que contém o Body do Box2D
        entity.add(bodyComponent);

        // Adiciona a entidade à engine
        engine.addEntity(entity);

        return entity;
    }

    // Função para criar o corpo físico usando Box2D
    public static BodyComponent createBody(Vector2 position, World world) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.position.set(position);
        bodyDef.type = BodyDef.BodyType.DynamicBody;

        // Criar o corpo no mundo Box2D
        Body body = world.createBody(bodyDef);

        // Definir forma e propriedades físicas do corpo
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(30f, 30f, new Vector2(0, 45f), 0f); // Ajuste conforme o tamanho do sprite

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 1.0f;
        fixtureDef.friction = 0.5f;
        fixtureDef.restitution = 0.3f;

        body.createFixture(fixtureDef);
        shape.dispose();

        // Criar e retornar o componente
        BodyComponent bodyComponent = new BodyComponent();
        bodyComponent.body = body;
        return bodyComponent;
    }
}
