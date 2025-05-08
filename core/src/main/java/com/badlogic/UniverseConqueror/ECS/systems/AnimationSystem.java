package com.badlogic.UniverseConqueror.ECS.systems;

import com.badlogic.UniverseConqueror.ECS.components.AnimationComponent;
import com.badlogic.UniverseConqueror.ECS.components.StateComponent;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class AnimationSystem extends IteratingSystem {
    private ComponentMapper<StateComponent> sm = ComponentMapper.getFor(StateComponent.class);
    private ComponentMapper<AnimationComponent> am = ComponentMapper.getFor(AnimationComponent.class);

    public AnimationSystem() {
        super(Family.all(AnimationComponent.class, StateComponent.class).get());
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        StateComponent state = sm.get(entity);
        AnimationComponent anim = am.get(entity);


        // Atualiza o tempo da animação
        anim.stateTime += deltaTime;

        //System.out.println("Estado atual: " + state.currentState);
        Animation<TextureRegion> animation = anim.animations.get(state.currentState);
        if (animation != null) {
            anim.currentFrame = animation.getKeyFrame(anim.stateTime, false);
           // System.out.println("Quadro atual da animação: " + anim.currentFrame);
        } else {
           // System.out.println("Nenhuma animação encontrada para o estado: " + state.currentState);
        }
    }
}
