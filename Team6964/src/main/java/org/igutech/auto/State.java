package org.igutech.auto;

public enum State {

    OFF,
    SHOOT_RING_STACK,
    MOVE_TO_SHOOT_RING_STACK,
    GRAB_SECOND_WOBBLE_GOAL,
    MOVE_TO_GRAB_SECOND_GOAL,
    INTAKE_RING_STACK,
    MOVE_TO_TO_RING_STACK,
    DROP_FIRST_WOBBLE_GOAL,
    SHOOTING_PRELOAD_RINGS,
    PREPARE_TO_SHOOT;

    static {
        PREPARE_TO_SHOOT.nextState = SHOOTING_PRELOAD_RINGS;
        SHOOTING_PRELOAD_RINGS.nextState = DROP_FIRST_WOBBLE_GOAL;
        DROP_FIRST_WOBBLE_GOAL.nextState = MOVE_TO_TO_RING_STACK;
        MOVE_TO_TO_RING_STACK.nextState = INTAKE_RING_STACK;
        INTAKE_RING_STACK.nextState = MOVE_TO_GRAB_SECOND_GOAL;
        MOVE_TO_GRAB_SECOND_GOAL.nextState = GRAB_SECOND_WOBBLE_GOAL;
        GRAB_SECOND_WOBBLE_GOAL.nextState = MOVE_TO_SHOOT_RING_STACK;
        MOVE_TO_SHOOT_RING_STACK.nextState = SHOOT_RING_STACK;
        SHOOT_RING_STACK.nextState=OFF;
    }

    private State nextState;

    public State getNextState() {
        if (this.nextState == null) return this;
        return this.nextState;
    }
}
