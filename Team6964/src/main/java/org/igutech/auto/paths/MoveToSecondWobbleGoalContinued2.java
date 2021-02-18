package org.igutech.auto.paths;

import androidx.annotation.Nullable;

import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.acmerobotics.roadrunner.geometry.Vector2d;
import com.acmerobotics.roadrunner.trajectory.Trajectory;

import org.igutech.auto.FullRedAuto;
import org.igutech.auto.statelib.State;

public class MoveToSecondWobbleGoalContinued2 extends State {
    private boolean done = false;
    private FullRedAuto fullRedAuto;
    private Trajectory moveToSecondWobbleGoalContinued2;

    public MoveToSecondWobbleGoalContinued2(FullRedAuto fullRedAuto, Pose2d previous) {
        this.fullRedAuto = fullRedAuto;
        Trajectory moveToSecondWobbleGoalContinued2 = fullRedAuto.getDrive().trajectoryBuilder(previous)
                .lineToConstantHeading(new Vector2d(-44.5, -38.5))
                .addDisplacementMarker(() -> fullRedAuto.grabWobbleGoal(() -> done = true))
                .build();
        this.moveToSecondWobbleGoalContinued2 = moveToSecondWobbleGoalContinued2;
    }


    @Override
    public void onEntry(@Nullable State previousState) {
        fullRedAuto.getDrive().followTrajectoryAsync(moveToSecondWobbleGoalContinued2);
    }

    @Nullable
    @Override
    public State getNextState() {
        if (done) {
            return new MoveToShootRingStack(fullRedAuto, moveToSecondWobbleGoalContinued2.end());
        } else {
            return null;
        }
    }
}
