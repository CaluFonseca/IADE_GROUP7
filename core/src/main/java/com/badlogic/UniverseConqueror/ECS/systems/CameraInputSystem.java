package com.badlogic.UniverseConqueror.ECS.systems;

import com.badlogic.UniverseConqueror.ECS.components.CameraComponent;
import com.badlogic.ashley.core.*;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

public class CameraInputSystem extends EntitySystem {
    private OrthographicCamera camera;
    private Engine engine;
    private float dragStartX, dragStartY;
    private boolean dragging = false;
    private InputAdapter inputAdapter;
    private Runnable onCameraToggle;
    private ComponentMapper<CameraComponent> cm = ComponentMapper.getFor(CameraComponent.class);

    public CameraInputSystem(OrthographicCamera camera) {
        this.camera = camera;
        setupInputProcessor();
    }

    @Override
    public void addedToEngine(Engine engine) {
        this.engine = engine;
    }
    public void setOnCameraToggle(Runnable onCameraToggle) {
        this.onCameraToggle = onCameraToggle;
    }
    private void setupInputProcessor() {
        inputAdapter = new InputAdapter() {
            @Override
            public boolean scrolled(float amountX, float amountY) {
                float targetZoom = MathUtils.clamp(camera.zoom + amountY * 0.1f, 0.5f, 3f);
                camera.zoom = MathUtils.lerp(camera.zoom, targetZoom, 0.2f);
                return true;
            }

            @Override
            public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.C) {
                    toggleCameraFollow();
                    return true;
                }
                return false;
            }

            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                if (button == Input.Buttons.RIGHT && !isFollowingPlayer()) {
                    Vector3 world = camera.unproject(new Vector3(screenX, screenY, 0));
                    dragStartX = world.x;
                    dragStartY = world.y;
                    dragging = true;
                    return true;
                }
                return false;
            }

            @Override
            public boolean touchDragged(int screenX, int screenY, int pointer) {
                if (dragging && !isFollowingPlayer()) {
                    Vector3 world = camera.unproject(new Vector3(screenX, screenY, 0));
                    float dx = dragStartX - world.x;
                    float dy = dragStartY - world.y;
                    camera.position.add(dx, dy, 0);
                    dragStartX = world.x;
                    dragStartY = world.y;
                    return true;
                }
                return false;
            }

            @Override
            public boolean touchUp(int screenX, int screenY, int pointer, int button) {
                if (button == Input.Buttons.RIGHT) {
                    dragging = false;
                    return true;
                }
                return false;
            }
        };
    }


    public void toggleCameraFollow() {
        ImmutableArray<Entity> cameraEntities = engine.getEntitiesFor(Family.all(CameraComponent.class).get());
        for (Entity entity : cameraEntities) {
            CameraComponent cam = cm.get(entity);
            cam.followPlayer = !cam.followPlayer;
        }

        // Chamar callback para notificar que a câmera foi alternada
        if (onCameraToggle != null) {
            onCameraToggle.run();
        }
    }

    public boolean isFollowingPlayer() {
        ImmutableArray<Entity> cameraEntities = engine.getEntitiesFor(Family.all(CameraComponent.class).get());
        for (Entity entity : cameraEntities) {
            CameraComponent cam = cm.get(entity);
            return cam.followPlayer;
        }
        return true; // default
    }

    public void setCameraFollow(boolean follow) {
        ImmutableArray<Entity> cameraEntities = engine.getEntitiesFor(Family.all(CameraComponent.class).get());
        for (Entity entity : cameraEntities) {
            CameraComponent cam = cm.get(entity);
            cam.followPlayer = follow;
        }
    }

    public InputAdapter getInputAdapter() {
        return inputAdapter;
    }
}
