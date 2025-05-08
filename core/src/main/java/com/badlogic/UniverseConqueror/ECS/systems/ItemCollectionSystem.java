package com.badlogic.UniverseConqueror.ECS.systems;

import com.badlogic.UniverseConqueror.ECS.components.BoundsComponent;
import com.badlogic.UniverseConqueror.ECS.components.ItemComponent;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.ui.Label;

public class ItemCollectionSystem extends IteratingSystem {
    private final Rectangle playerBounds;
    private final Label itemsLabel;
    private int collectedCount = 0;

    private final ComponentMapper<ItemComponent> itemMapper = ComponentMapper.getFor(ItemComponent.class);
    private final ComponentMapper<BoundsComponent> boundsMapper = ComponentMapper.getFor(BoundsComponent.class);

    public ItemCollectionSystem(Rectangle playerBounds, Label itemsLabel) {
        super(Family.all(ItemComponent.class, BoundsComponent.class).get());
        this.playerBounds = playerBounds;
        this.itemsLabel = itemsLabel;
        updateLabel(); // mostra valor inicial
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        ItemComponent item = itemMapper.get(entity);
        BoundsComponent bounds = boundsMapper.get(entity);

        if (!item.isCollected && playerBounds.overlaps(bounds.bounds)) {
            item.isCollected = true;
            collectedCount++;
            updateLabel();
            // aqui você pode tocar um som ou ativar outro efeito
        }
    }

    private void updateLabel() {
        itemsLabel.setText("Items: " + collectedCount);
    }
}
