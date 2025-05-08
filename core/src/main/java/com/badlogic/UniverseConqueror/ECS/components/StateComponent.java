package com.badlogic.UniverseConqueror.ECS.components;

import com.badlogic.ashley.core.Component;

public class StateComponent implements Component {

    public enum State {
        IDLE,
        WALK,
        CLIMB,
        FAST_MOVE,
        JUMP,
        FALL,
        DEATH,
        HURT,
        ATTACK,
        SUPER_ATTACK,
        DEFENSE,
        WALK_INJURED,
        IDLE_INJURED,
        DEFENSE_INJURED
    }

    public State currentState = State.IDLE;
    public State previousState = State.IDLE;
    public float timeInState  = 0f;
    public float superAttackTimeLeft = 0f;

    public void update(float delta) {
        timeInState  += delta;
    }

    public void set(State newState) {
        if (newState != currentState) {
            previousState = currentState;
            currentState = newState;
            timeInState  = 0;

        }
    }

    public State get() {
        return currentState;
    }
}
