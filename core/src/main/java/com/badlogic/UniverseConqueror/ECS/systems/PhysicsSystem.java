package com.badlogic.UniverseConqueror.ECS.systems;

import com.badlogic.UniverseConqueror.ECS.components.TransformComponent;
import com.badlogic.UniverseConqueror.ECS.components.VelocityComponent;
import com.badlogic.UniverseConqueror.ECS.components.PhysicsComponent;
import com.badlogic.UniverseConqueror.ECS.components.PositionComponent;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.Body;

public class PhysicsSystem extends EntitySystem {
    private World world;
    private Box2DDebugRenderer debugRenderer;

    private ComponentMapper<PhysicsComponent> pm = ComponentMapper.getFor(PhysicsComponent.class);
    private ComponentMapper<VelocityComponent> vm = ComponentMapper.getFor(VelocityComponent.class);
    private ComponentMapper<PositionComponent> posm = ComponentMapper.getFor(PositionComponent.class);
    private ComponentMapper<TransformComponent> tm = ComponentMapper.getFor(TransformComponent.class);

    public PhysicsSystem(World world) {
        this.world = world;
        this.debugRenderer = new Box2DDebugRenderer();
    }

    @Override
    public void update(float deltaTime) {
        // Atualiza o mundo Box2D
        world.step(deltaTime, 6, 2);

        // Atualiza a posição e velocidade das entidades físicas
        for (Entity entity : getEngine().getEntitiesFor(
            Family.all(PhysicsComponent.class, VelocityComponent.class, PositionComponent.class).get())) {

            PhysicsComponent physics = pm.get(entity);
            VelocityComponent velocity = vm.get(entity);
            PositionComponent position = posm.get(entity);
            TransformComponent transform = tm.get(entity);

            Body body = physics.body;

            // Verifica se o corpo é válido
            if (body != null) {

                // Aplica a velocidade física
                physics.body.setLinearVelocity(velocity.velocity);

                // Sincroniza a posição física com a posição lógica
                position.position.set(body.getPosition().x, body.getPosition().y);
                transform.position.set(body.getPosition().x, body.getPosition().y, 0);

                // Debug: Exibe as informações de posição e velocidade
                System.out.println("Posição do corpo: " + body.getPosition());
                System.out.println("Velocidade aplicada: " + velocity.velocity);
            }
        }
    }

    public void setContactListener(com.badlogic.gdx.physics.box2d.ContactListener listener) {
        world.setContactListener(listener);
    }

    public void render(float deltaTime) {
        // Se necessário, renderiza a física no modo de depuração
        debugRenderer.render(world, getEngine().getSystem(CameraSystem.class).getCamera().combined);
    }
}
