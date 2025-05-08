package com.badlogic.UniverseConqueror.ECS.components;

import com.badlogic.ashley.core.Component;

public class HealthComponent implements Component {
    public int currentHealth = 100;
    public int maxHealth = 100;
    public boolean wasDamagedThisFrame = false;

    public boolean isDead() {
        return currentHealth <= 0;
    }



    public void damage(int amount) {
        currentHealth = Math.max(0, currentHealth - amount);
        wasDamagedThisFrame = true;
    }

    public void heal(int amount) {
        currentHealth = Math.min(maxHealth, currentHealth + amount);
    }
}
