package com.badlogic.UniverseConqueror.ECS.systems;

import com.badlogic.UniverseConqueror.ECS.components.CameraComponent;
import com.badlogic.UniverseConqueror.ECS.components.TransformComponent;
import com.badlogic.ashley.core.*;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector3;

public class CameraSystem extends EntitySystem {
    private ImmutableArray<Entity> entities;
    private OrthographicCamera camera;
    private ComponentMapper<TransformComponent> tm = ComponentMapper.getFor(TransformComponent.class);
    private ComponentMapper<CameraComponent> cm = ComponentMapper.getFor(CameraComponent.class);

    private float mapWidth;
    private float mapHeight;

    public CameraSystem(OrthographicCamera camera, float mapWidth, float mapHeight) {
        this.camera = camera;
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;
    }

    @Override
    public void addedToEngine(Engine engine) {
        entities = engine.getEntitiesFor(Family.all(TransformComponent.class, CameraComponent.class).get());
    }

    @Override
    public void update(float deltaTime) {
        if (entities.size() == 0) return;

        Entity target = entities.first();
        TransformComponent transform = tm.get(target);
        CameraComponent camComp = cm.get(target);

        if (camComp.followPlayer) {
            Vector3 targetPos = new Vector3(transform.position.x, transform.position.y, 0);

            // Smooth follow
            camera.position.lerp(targetPos, 0.1f);

            // Clamp camera to bounds
            float camWidth = camera.viewportWidth * camera.zoom;
            float camHeight = camera.viewportHeight * camera.zoom;

            //camera.position.x = MathUtils.clamp(camera.position.x, camWidth / 2f, mapWidth - camWidth / 2f);
            //camera.position.y = MathUtils.clamp(camera.position.y, camHeight / 2f, mapHeight - camHeight / 2f);
        }

        camera.update();
    }
    public OrthographicCamera getCamera() {
        return camera;
    }
}

