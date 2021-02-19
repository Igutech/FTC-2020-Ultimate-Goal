package org.igutech.auto.paths;

import androidx.annotation.Nullable;

import com.acmerobotics.roadrunner.geometry.Pose2d;

import org.igutech.auto.FullRedAuto;
import org.igutech.auto.statelib.State;

public class DropFirstWobbleGoal extends State {

    private FullRedAuto fullRedAuto;
    private Pose2d previous;
    private boolean done = false;

    public DropFirstWobbleGoal(FullRedAuto fullRedAuto, Pose2d previous) {
        this.fullRedAuto = fullRedAuto;
        this.previous = previous;
    }

    @Override
    public void onEntry(@Nullable State previousState) {
        fullRedAuto.dropWobbleGoal(() -> done = true);
    }

    @Nullable
    @Override
    public State getNextState() {
        if (done) {
            System.out.println("Transitioning from Drop wobble goal to move away from target zone");

            return new MoveAwayFromTargetZone(fullRedAuto, previous);
        }
        return null;
    }
}
