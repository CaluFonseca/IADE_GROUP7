package com.badlogic.UniverseConqueror.Utils;

import com.badlogic.UniverseConqueror.ECS.components.HealthComponent;
import com.badlogic.UniverseConqueror.ECS.components.PlayerComponent;
import com.badlogic.UniverseConqueror.ECS.systems.ItemCollectionSystem;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.physics.box2d.*;

public class MyContactListener implements ContactListener {

    private final Engine engine;
    private final ItemCollectionSystem itemCollectionSystem; // Adicione o itemCollectionSystem

    // Construtor para receber o engine e o itemCollectionSystem
    public MyContactListener(Engine engine, ItemCollectionSystem itemCollectionSystem) {
        this.engine = engine;
        this.itemCollectionSystem = itemCollectionSystem; // Inicializa o itemCollectionSystem
    }

    @Override
    public void beginContact(Contact contact) {
        Body bodyA = contact.getFixtureA().getBody();
        Body bodyB = contact.getFixtureB().getBody();

        // Obter as entidades envolvidas na colisão
        Entity playerEntity = getPlayerEntity(bodyA, bodyB);

        if (playerEntity != null) {
            // Verificar colisão com item
            if (isPlayerAndItemCollision(bodyA, bodyB)) {
                // Coletar item e não aplicar dano
                collectItem(bodyA, bodyB);
            } else {
                // Se não for um item, aplicar dano
                applyDamageToPlayer(playerEntity);
            }
        }
    }

    // Recupera a entidade do jogador a partir da colisão
    private Entity getPlayerEntity(Body bodyA, Body bodyB) {
        Object userDataA = bodyA.getUserData();
        Object userDataB = bodyB.getUserData();

        if (userDataA instanceof Entity) {
            Entity entityA = (Entity) userDataA;
            if (entityA.getComponent(PlayerComponent.class) != null) {
                return entityA;
            }
        }

        if (userDataB instanceof Entity) {
            Entity entityB = (Entity) userDataB;
            if (entityB.getComponent(PlayerComponent.class) != null) {
                return entityB;
            }
        }

        return null; // Retorna null se não encontrar o jogador
    }

    // Verifica colisão entre o jogador e o item
    private boolean isPlayerAndItemCollision(Body bodyA, Body bodyB) {
        Fixture playerFixture = bodyA.getFixtureList().get(0);
        Fixture itemFixture = bodyB.getFixtureList().get(0);

        // A colisão com o item é identificada pela string "item" no userData do fixture
        return ("item".equals(itemFixture.getUserData()));
    }

    // Método de coleta de item
    private void collectItem(Body bodyA, Body bodyB) {
        Fixture itemFixture = bodyB.getFixtureList().get(0);

        // Verifica se o userData do fixture do item é "item" e coleta
        if ("item".equals(itemFixture.getUserData())) {
            Entity itemEntity = (Entity) itemFixture.getBody().getUserData();
            if (itemEntity != null) {
                // Chama o método do ItemCollectionSystem para coletar o item
                itemCollectionSystem.collectItem(itemEntity);
                engine.removeEntity(itemEntity);  // Remove o item do ECS
            }
        }
    }

    // Aplica dano ao jogador
    private void applyDamageToPlayer(Entity playerEntity) {
        HealthComponent health = playerEntity.getComponent(HealthComponent.class);
        if (health != null && !health.isDead()) {
            health.damage(1);  // Aplica 1 ponto de dano ao jogador
        }
    }

    @Override
    public void endContact(Contact contact) {}

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {}

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {}
}
