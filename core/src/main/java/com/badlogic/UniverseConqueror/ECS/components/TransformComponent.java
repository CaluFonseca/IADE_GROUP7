package com.badlogic.UniverseConqueror.ECS.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Vector3;



public class TransformComponent implements Component {
    public final Vector3 position = new Vector3();

    public TransformComponent(float x, float y) {
        this.position.set(x, y, 0);
    }

    public TransformComponent() {
        this(0, 0);
    }
}


