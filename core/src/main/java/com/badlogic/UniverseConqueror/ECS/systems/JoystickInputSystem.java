package com.badlogic.UniverseConqueror.ECS.systems;

import com.badlogic.UniverseConqueror.Utils.Joystick;
import com.badlogic.UniverseConqueror.ECS.components.VelocityComponent;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;

public class JoystickInputSystem extends IteratingSystem {
    private final Joystick joystick;
    private final float MAX_SPEED = 100f; // Ajusta à tua escala

    private final ComponentMapper<VelocityComponent> vm = ComponentMapper.getFor(VelocityComponent.class);

    public JoystickInputSystem(Joystick joystick) {
        super(Family.all(VelocityComponent.class).get());
        this.joystick = joystick;
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
//        VelocityComponent velocity = vm.get(entity);
//        if (joystick.isMoving()) {
//            Vector2 dir = joystick.getDirection(); // vetor normalizado
//            velocity.velocity.set(dir.x * MAX_SPEED, dir.y * MAX_SPEED);
//        } else {
//            velocity.velocity.setZero();
//        }
    }
}
