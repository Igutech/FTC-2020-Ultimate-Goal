package org.igutech.auto.paths;

import androidx.annotation.Nullable;

import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.acmerobotics.roadrunner.geometry.Vector2d;
import com.acmerobotics.roadrunner.trajectory.Trajectory;

import org.igutech.auto.FullRedAuto;
import dev.raneri.statelib.State;

public class MoveToShootRingStack extends State {

    private FullRedAuto fullRedAuto;
    private Trajectory moveToRingStack;
    private boolean done = false;

    public MoveToShootRingStack(FullRedAuto fullRedAuto, Pose2d previous) {
        this.fullRedAuto = fullRedAuto;
        moveToRingStack = fullRedAuto.getDrive().trajectoryBuilder(previous)
                .splineToConstantHeading(new Vector2d(-7.0, -36.0), Math.toRadians(0.0))
                .addDisplacementMarker(() -> done = true)
                .build();
    }

    @Override
    public void onEntry(@Nullable State previousState) {
        fullRedAuto.getDrive().followTrajectoryAsync(moveToRingStack);
    }

    @Nullable
    @Override
    public State getNextState() {
        if (done) {
            return new ShootRingStackState(fullRedAuto, moveToRingStack.end());
        }
        return null;
    }
}
