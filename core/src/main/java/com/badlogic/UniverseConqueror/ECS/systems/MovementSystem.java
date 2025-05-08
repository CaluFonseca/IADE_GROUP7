package com.badlogic.UniverseConqueror.ECS.systems;

import com.badlogic.UniverseConqueror.ECS.components.PhysicsComponent;
import com.badlogic.UniverseConqueror.ECS.components.TransformComponent;
import com.badlogic.UniverseConqueror.ECS.components.VelocityComponent;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;

public class MovementSystem extends IteratingSystem {
    private ComponentMapper<PhysicsComponent> phm = ComponentMapper.getFor(PhysicsComponent.class);
    private ComponentMapper<VelocityComponent> vm = ComponentMapper.getFor(VelocityComponent.class);
    private ComponentMapper<TransformComponent> tm = ComponentMapper.getFor(TransformComponent.class);

    public MovementSystem() {
        super(Family.all(PhysicsComponent.class, VelocityComponent.class, TransformComponent.class).get());
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        // Obtém os componentes necessários
        PhysicsComponent physics = phm.get(entity);
        VelocityComponent velocity = vm.get(entity);

        if (physics.body != null && velocity != null) {
            // Aplica a velocidade no corpo de Box2D
            float deltaX = velocity.velocity.x * deltaTime; // Ajusta com o deltaTime
            float deltaY = velocity.velocity.y * deltaTime;
            physics.body.setLinearVelocity(deltaX, deltaY);
            System.out.println("Physics: " + physics.body.getWorldCenter()+"\n velocidade:"+velocity.velocity);
            // Atualiza a posição do TransformComponent
            TransformComponent transform = tm.get(entity);
            transform.position.set(physics.body.getPosition().x, physics.body.getPosition().y, transform.position.z);

        }
    }
}
