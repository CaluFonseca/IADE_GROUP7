package com.badlogic.UniverseConqueror.ECS.systems;

import com.badlogic.UniverseConqueror.ECS.components.HealthComponent;
import com.badlogic.UniverseConqueror.ECS.components.PlayerComponent;
import com.badlogic.UniverseConqueror.ECS.components.StateComponent;
import com.badlogic.ashley.core.*;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.audio.Sound;

// Adiciona uma interface para permitir a comunicação com a UI
public class HealthSystem extends EntitySystem {
    private ImmutableArray<Entity> entities;
    private ComponentMapper<HealthComponent> hm = ComponentMapper.getFor(HealthComponent.class);
    private ComponentMapper<StateComponent> sm = ComponentMapper.getFor(StateComponent.class);
    private ComponentMapper<PlayerComponent> pm = ComponentMapper.getFor(PlayerComponent.class);

    private Sound deathSound, hurtSound;
    private HealthChangeListener healthChangeListener; // Referência à UI

    // Interface para notificar a mudança de saúde
    public interface HealthChangeListener {
        void onHealthChanged(int currentHealth);
    }

    public HealthSystem(Sound deathSound, Sound hurtSound, HealthChangeListener listener) {
        this.deathSound = deathSound;
        this.hurtSound = hurtSound;
        this.healthChangeListener = listener;
    }

    @Override
    public void addedToEngine(Engine engine) {
        entities = engine.getEntitiesFor(Family.all(HealthComponent.class, StateComponent.class).get());
    }

    @Override
    public void update(float deltaTime) {
        for (Entity entity : entities) {
            HealthComponent health = hm.get(entity);
            StateComponent state = sm.get(entity);

            // Verifica se a saúde mudou e notifica a UI
            if (health.wasDamagedThisFrame) {
                if (healthChangeListener != null) {
                    healthChangeListener.onHealthChanged(health.currentHealth);
                }
                health.wasDamagedThisFrame = false;
            }

            // Lógica para a morte
            if (health.currentHealth <= 0 && state.get() != StateComponent.State.DEATH) {
                state.set(StateComponent.State.DEATH);
                if (pm.has(entity)) deathSound.play(); // Só toca som se for o player
            }

            // Lógica para reagir a dano
            if (health.wasDamagedThisFrame && health.currentHealth > 0) {
                state.set(StateComponent.State.HURT);
                if (pm.has(entity)) hurtSound.play();
                health.wasDamagedThisFrame = false;
            }
        }
    }
}
