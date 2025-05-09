package com.badlogic.UniverseConqueror.Utils;

import com.badlogic.UniverseConqueror.ECS.components.HealthComponent;
import com.badlogic.UniverseConqueror.ECS.components.PlayerComponent;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.physics.box2d.*;

public class MyContactListener implements ContactListener {

    private Engine engine;

    public MyContactListener(Engine engine) {
        this.engine = engine;
    }

    @Override
    public void beginContact(Contact contact) {
        Body bodyA = contact.getFixtureA().getBody();
        Body bodyB = contact.getFixtureB().getBody();

        Object userDataA = bodyA.getUserData();
        Object userDataB = bodyB.getUserData();

        Entity playerEntity = null;

        if (userDataA instanceof Entity) {
            Entity entityA = (Entity) userDataA;
            if (entityA.getComponent(PlayerComponent.class) != null) {
                playerEntity = entityA;
            }
        }

        if (userDataB instanceof Entity) {
            Entity entityB = (Entity) userDataB;
            if (entityB.getComponent(PlayerComponent.class) != null) {
                playerEntity = entityB;
            }
        }

        if (playerEntity != null) {
            applyDamageToPlayer(playerEntity);
        }
    }

    private void applyDamageToPlayer(Entity playerEntity) {
        HealthComponent health = playerEntity.getComponent(HealthComponent.class);
        if (health != null && !health.isDead()) {
            health.damage(1); // Aplica 10 de dano ao colidir
        }
    }

    @Override
    public void endContact(Contact contact) {}

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {}

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {}
}
