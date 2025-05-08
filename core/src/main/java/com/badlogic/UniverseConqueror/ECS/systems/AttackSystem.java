package com.badlogic.UniverseConqueror.ECS.systems;

import com.badlogic.UniverseConqueror.ECS.components.AttackComponent;
import com.badlogic.UniverseConqueror.ECS.components.StateComponent;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.ashley.core.Family;

public class AttackSystem extends IteratingSystem {

    private final ComponentMapper<AttackComponent> am = ComponentMapper.getFor(AttackComponent.class);
    private final ComponentMapper<StateComponent> sm = ComponentMapper.getFor(StateComponent.class);

    public AttackSystem() {
        super(Family.all(AttackComponent.class, StateComponent.class).get());
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        AttackComponent attack = am.get(entity);
        StateComponent state = sm.get(entity);

        // Atualiza timers
        attack.timeSinceLastAttack += deltaTime;

        if (attack.isAttacking) {
            attack.attackTimer += deltaTime;

            // Termina ataque quando duração expira
            if (attack.attackTimer >= attack.attackDuration) {
                attack.isAttacking = false;
                attack.attackTimer = 0f;

                // Volta para idle após o ataque
                if (state.currentState == StateComponent.State.ATTACK) {
                    state.set(StateComponent.State.IDLE);
                }
            }

        } else {
            // Se pode atacar e está pronto (lógica de controle pode chamar isso externamente)
            if (attack.canAttack()) {
                // Exemplo: iniciar ataque manualmente fora do sistema, ou via input
                // Aqui não iniciamos automaticamente, mas poderias fazer isso:
                // attack.startAttack(); state.set(StateComponent.State.ATTACK);
            }
        }
    }
}
