package org.igutech.auto;

public enum State {

    OFF,
    DROP_SECOND_GOAL,
    MOVE_TO_GRAB_SECOND_GOAL,
    SHOOTING_RING_STACK,
    MOVE_TO_SHOOT_RING_STACK,
    DROP_FIRST_WOBBLE_GOAL,
    INTAKE_RING_STACK,
    SHOOTING_PRELOAD_RINGS,
    PREPARE_TO_SHOOT;

    static {
        PREPARE_TO_SHOOT.nextState = SHOOTING_PRELOAD_RINGS;
        SHOOTING_PRELOAD_RINGS.nextState = INTAKE_RING_STACK;
        INTAKE_RING_STACK.nextState = DROP_FIRST_WOBBLE_GOAL;
        DROP_FIRST_WOBBLE_GOAL.nextState = MOVE_TO_SHOOT_RING_STACK;
        MOVE_TO_SHOOT_RING_STACK.nextState = SHOOTING_RING_STACK;
        SHOOTING_RING_STACK.nextState = MOVE_TO_GRAB_SECOND_GOAL;
        MOVE_TO_GRAB_SECOND_GOAL.nextState = DROP_SECOND_GOAL;
        MOVE_TO_GRAB_SECOND_GOAL.nextState = OFF;
    }

    private State nextState;

    public State getNextState() {
        if (this.nextState == null) return this;
        return this.nextState;
    }
}
