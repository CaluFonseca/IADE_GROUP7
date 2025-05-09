package com.badlogic.UniverseConqueror.ECS.systems;

import com.badlogic.UniverseConqueror.ECS.components.HealthComponent;
import com.badlogic.UniverseConqueror.ECS.components.PlayerComponent;
import com.badlogic.UniverseConqueror.ECS.components.StateComponent;
import com.badlogic.ashley.core.*;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.audio.Sound;

public class HealthSystem extends EntitySystem {
    private ImmutableArray<Entity> entities;
    private ComponentMapper<HealthComponent> hm = ComponentMapper.getFor(HealthComponent.class);
    private ComponentMapper<StateComponent> sm = ComponentMapper.getFor(StateComponent.class);
    private ComponentMapper<PlayerComponent> pm = ComponentMapper.getFor(PlayerComponent.class);

    private Sound deathSound, hurtSound;
    private HealthChangeListener healthChangeListener;

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

            // Atualiza o cooldown de invulnerabilidade
            if (health.hurtCooldownTimer > 0f) {
                health.hurtCooldownTimer -= deltaTime;
            }
// Reduz os timers
            if (health.hurtCooldownTimer > 0f) health.hurtCooldownTimer -= deltaTime;
            if (health.hurtDuration > 0f) health.hurtDuration -= deltaTime;

            // Se levou dano, muda para HURT
            if (health.wasDamagedThisFrame && !health.isDead()) {
                state.set(StateComponent.State.HURT);
                if (pm.has(entity) && hurtSound != null) hurtSound.play();
                if (healthChangeListener != null) healthChangeListener.onHealthChanged(health.currentHealth);
                health.wasDamagedThisFrame = false;
            }

            // Lógica de morte
            if (health.currentHealth <= 0 && state.get() != StateComponent.State.DEATH) {
                state.set(StateComponent.State.DEATH);
                if (pm.has(entity) && deathSound != null) deathSound.play();

            }

            // Volta para IDLE quando a duração visual acabar
            if (state.get() == StateComponent.State.HURT && health.hurtDuration <= 0f && !health.isDead()) {
                state.set(StateComponent.State.IDLE);
            }
        }
    }
}
