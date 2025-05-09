package com.badlogic.UniverseConqueror.ECS.components;

import com.badlogic.ashley.core.Component;

public class HealthComponent implements Component {
    public int currentHealth = 100;
    public int maxHealth = 100;
    public boolean wasDamagedThisFrame = false;
    public float hurtCooldownTimer = 0f;
    public float hurtDuration = 0f;

    public boolean isDead() {
        return currentHealth <= 0;
    }



    public void damage(int amount) {
        if (hurtCooldownTimer <= 0f) {
            currentHealth = Math.max(0, currentHealth - amount);
            wasDamagedThisFrame = true;
            hurtCooldownTimer = 1.0f; // 1 segundo de invulnerabilidade
            hurtDuration = 0.2f;
        }
    }

    public void heal(int amount) {
        currentHealth = Math.min(maxHealth, currentHealth + amount);
    }
}
