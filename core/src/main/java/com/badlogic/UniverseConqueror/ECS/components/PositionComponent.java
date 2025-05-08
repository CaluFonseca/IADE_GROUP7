package com.badlogic.UniverseConqueror.ECS.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Vector2;

public class PositionComponent implements Component {
    public final Vector2 position = new Vector2();

    public PositionComponent(float x, float y) {
        position.set(x, y);
    }

    public PositionComponent() {
        // posição inicial em (0, 0)
    }
}
