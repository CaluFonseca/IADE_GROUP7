package com.badlogic.UniverseConqueror.ECS.systems;

import com.badlogic.UniverseConqueror.ECS.components.SoundComponent;
import com.badlogic.UniverseConqueror.ECS.components.StateComponent;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;

public class SoundSystem extends IteratingSystem {
    private ComponentMapper<SoundComponent> sm = ComponentMapper.getFor(SoundComponent.class);
    private ComponentMapper<StateComponent> stm = ComponentMapper.getFor(StateComponent.class);

    public SoundSystem() {
        super(Family.all(SoundComponent.class, StateComponent.class).get());
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        SoundComponent sound = sm.get(entity);
        StateComponent state = stm.get(entity);

        if (state.currentState == StateComponent.State.ATTACK && sound.sounds.containsKey("attack")) {
            sound.sounds.get("attack").play();
        }
    }
}
