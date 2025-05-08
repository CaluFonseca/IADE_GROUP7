package com.badlogic.UniverseConqueror.ECS.systems;

import com.badlogic.UniverseConqueror.ECS.components.PositionComponent;
import com.badlogic.ashley.core.*;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.UniverseConqueror.ECS.components.AnimationComponent;
import com.badlogic.UniverseConqueror.ECS.components.TransformComponent;
import com.badlogic.gdx.graphics.OrthographicCamera;

public class RenderSystem extends IteratingSystem {
    private SpriteBatch batch;
    private OrthographicCamera camera;  // Adiciona a referência para a câmera

    public RenderSystem(SpriteBatch batch, OrthographicCamera camera) {
        super(Family.all(TransformComponent.class, AnimationComponent.class).get());
        this.batch = batch;
        this.camera = camera;  // Recebe a câmera no construtor
    }

    @Override
    public void update(float deltaTime) {
        batch.setProjectionMatrix(camera.combined);  // Configura a projeção da câmera
        batch.begin();
        super.update(deltaTime);
        batch.end();
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        PositionComponent position = entity.getComponent(PositionComponent.class);  // Use PositionComponent
        AnimationComponent animation = entity.getComponent(AnimationComponent.class);

        if (animation.currentFrame != null) {
            TextureRegion frame = animation.currentFrame;

            // Debugging the flipping logic
            // System.out.println("facingRight: " + animation.facingRight);

            // Apply scaling
            float scale = 0.4f;
            float drawWidth = frame.getRegionWidth() * scale;
            float drawHeight = frame.getRegionHeight() * scale;

            // Flip logic if facing left
            if (frame.isFlipX()) {
                frame.flip(true, false);  // Undo flip
            }

            if (!animation.facingRight) {
                frame.flip(true, false);  // Flip the sprite horizontally
            }

            // Use PositionComponent for position
            float x = position.position.x - drawWidth / 2f;  // Center the player horizontally
            float y = position.position.y;

            // Draw the sprite
            batch.draw(frame, x, y, drawWidth, drawHeight);


        }

    }

}




