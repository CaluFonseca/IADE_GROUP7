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


    public static Entity  createPlayer(PooledEngine engine,
                                      Vector2 position,
                                      ObjectMap<StateComponent.State, Animation<TextureRegion>> animations,
                                      ObjectMap<String, Sound> sounds,
                                      World world) {
        Entity entity = engine.createEntity();

        // Criar e adicionar componentes relacionados à posição e transformações
        TransformComponent transform = engine.createComponent(TransformComponent.class);
        transform.position.set(position.x, position.y, 0);

        // Criar componentes relacionados à física e Box2D
        PhysicsComponent physicsComponent = engine.createComponent(PhysicsComponent.class);
        BodyComponent bodyComponent = createBody(position, world);  // Cria o corpo físico
        Body body = bodyComponent.body; // <- agora tens acesso ao body
        physicsComponent.body = body;

        // Outros componentes do jogador
        VelocityComponent velocity = engine.createComponent(VelocityComponent.class);
        StateComponent state = engine.createComponent(StateComponent.class);

        AnimationComponent anim = engine.createComponent(AnimationComponent.class);
        anim.animations.putAll(animations);

        SoundComponent sound = engine.createComponent(SoundComponent.class);
        sound.sounds.putAll(sounds);

        PositionComponent positionComponent = new PositionComponent();
        positionComponent.position.set(position.x, position.y);

        CameraComponent cameraComponent = new CameraComponent();

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
        body.setUserData(entity);
        // Adiciona a entidade à engine
        engine.addEntity(entity);
        return entity;
    }

    // Função para criar o corpo físico usando Box2D
    public static BodyComponent createBody(Vector2 position, World world) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.position.set(position);  // Define a posição do corpo no mundo (centro do jogador)
        bodyDef.type = BodyDef.BodyType.DynamicBody;  // Corpo dinâmico

        // Criação do corpo no mundo
        Body body = world.createBody(bodyDef);

        // Criando a forma de colisão (garantindo que ela esteja centralizada)
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(40f, 40f);  // Forma de colisão centralizada (box de 64x64)

        // Definição do fixture para o corpo
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 1.0f;  // Definir a densidade do jogador
        fixtureDef.friction = 0.5f;  // Fricção para controlar deslizamento
        fixtureDef.restitution = 0.3f;  // Baixa restituição para evitar quicar

        // Aplica o fixture ao corpo
        body.createFixture(fixtureDef);

        // Ajuste do centro de massa (garantindo que está no centro do corpo)
//        MassData massData = new MassData();
//        massData.center.set(0, 0);  // Definir o centro de massa no centro do corpo
//        massData.mass = 1.0f;       // Defina a massa do jogador (ajuste conforme necessário)
//        massData.I = 0.1f;          // Definindo a inércia (resistência à rotação)
//        body.setMassData(massData); // Aplicando os dados de massa ao corpo


        // Descartar o shape após o uso para liberar memória
        shape.dispose();

        // Criando o BodyComponent e retornando
        BodyComponent bodyComponent = new BodyComponent();
        bodyComponent.body = body;
        return bodyComponent;
    }


}
