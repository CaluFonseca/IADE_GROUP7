package com.badlogic.UniverseConqueror.ECS.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.ObjectMap;

public class SoundComponent implements Component {
    public ObjectMap<String, Sound> sounds = new ObjectMap<>();
}
