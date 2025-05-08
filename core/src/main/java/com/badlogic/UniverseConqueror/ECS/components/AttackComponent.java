package com.badlogic.UniverseConqueror.ECS.components;

import com.badlogic.ashley.core.Component;

public class AttackComponent implements Component {
    public boolean isAttacking = false;
    public float attackTimer = 0f;
    public float attackDuration = 0.3f;
    public float attackCooldown = 0.5f;
    public float timeSinceLastAttack = 0f;

    public int maxAttackPower = 100;          // Valor total máximo
    public int remainingAttackPower = 100;    // Quanto ainda pode atacar
    public int attackCost = 10;               // Quanto consome por ataque


    public AttackComponent() {
    }
    public AttackComponent(int maxAttackPower, int attackCost) {
        this.maxAttackPower = maxAttackPower;
        this.remainingAttackPower = maxAttackPower;
        this.attackCost = attackCost;
    }

    public boolean canAttack() {
        return timeSinceLastAttack >= attackCooldown && remainingAttackPower >= attackCost;
    }

    public void startAttack() {
        isAttacking = true;
        attackTimer = 0f;
        timeSinceLastAttack = 0f;
        remainingAttackPower -= attackCost;

        if (remainingAttackPower < 0) {
            remainingAttackPower = 0;
        }
    }

    public void restorePower(int amount) {
        remainingAttackPower += amount;
        if (remainingAttackPower > maxAttackPower) {
            remainingAttackPower = maxAttackPower;
        }
    }
}
