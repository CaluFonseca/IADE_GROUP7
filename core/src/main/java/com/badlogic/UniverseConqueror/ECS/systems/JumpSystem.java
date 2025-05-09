package com.badlogic.UniverseConqueror.ECS.systems;

import com.badlogic.UniverseConqueror.ECS.components.BodyComponent;
import com.badlogic.UniverseConqueror.ECS.components.JumpComponent;
import com.badlogic.UniverseConqueror.ECS.components.StateComponent;
import com.badlogic.ashley.core.*;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.RayCastCallback;
import com.badlogic.gdx.physics.box2d.World;

public class JumpSystem extends EntitySystem {
    private ComponentMapper<JumpComponent> jm = ComponentMapper.getFor(JumpComponent.class);
    private ComponentMapper<BodyComponent> bm = ComponentMapper.getFor(BodyComponent.class);
    private ComponentMapper<StateComponent> sm = ComponentMapper.getFor(StateComponent.class);

    private ImmutableArray<Entity> entities;
    private World world;

    public JumpSystem(World world) {
        this.world = world;
    }

    @Override
    public void addedToEngine(Engine engine) {
        entities = engine.getEntitiesFor(Family.all(JumpComponent.class, BodyComponent.class).get());
    }

    @Override
    public void update(float deltaTime) {
        for (Entity entity : entities) {
            JumpComponent jump = jm.get(entity);
            BodyComponent body = bm.get(entity);
            StateComponent state = sm.get(entity);

            boolean isOnGround = isPlayerOnGround(body); // Verifica se está no chão

            // Se o jogador está tentando pular e pode
            if (jump.isJumping && jump.canJump && isOnGround) {
                body.body.applyLinearImpulse(new Vector2(0, jump.jumpForce), body.body.getWorldCenter(), true);
                jump.isJumping = false;
                jump.canJump = false; // Temporariamente não pode pular novamente até tocar o chão
                state.currentState = StateComponent.State.JUMP;
                jump.jumpDuration = 0.6f;
            }
            // Atualiza a duração do pulo
            if (jump.jumpDuration > 0) {
                jump.jumpDuration -= deltaTime;
            }
            // Permite pular novamente quando o jogador tocar o chão
            if (isOnGround) {
                jump.canJump = true;
            }
            // Se a animação de pulo acabou (duração expirou), muda para o estado de queda ou idle
            if (jump.jumpDuration <= 0 && body.body.getLinearVelocity().x <= 0 && (state.currentState == StateComponent.State.JUMP || state.currentState == StateComponent.State.WALK)) {
                state.set(StateComponent.State.IDLE); // Ou STATE.IDLE se preferir
            }
        }
    }

    // Método para verificar se o jogador está no chão usando RayCast
    private boolean isPlayerOnGround(BodyComponent bodyComponent) {
        Vector2 rayStart = new Vector2(bodyComponent.body.getPosition().x, bodyComponent.body.getPosition().y);
        Vector2 rayEnd = new Vector2(rayStart.x, rayStart.y - 0.1f); // Deslocamento para baixo

        RayCastCallback callback = new RayCastCallback() {
            @Override
            public float reportRayFixture(Fixture fixture, Vector2 point, Vector2 normal, float fraction) {
                // Retorna 1 se o raio tocar algo (ex: o chão)
                return 1;
            }
        };

        // Realiza o raycast para verificar se o jogador está tocando o chão
        world.rayCast(callback, rayStart, rayEnd);

        // Se o raio tocou algo, significa que o jogador está no chão
        return true;
    }
}
