package org.igutech.auto.paths;

import androidx.annotation.Nullable;

import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.acmerobotics.roadrunner.geometry.Vector2d;
import com.acmerobotics.roadrunner.trajectory.Trajectory;
import com.arcrobotics.ftclib.vision.UGContourRingPipeline;

import org.igutech.auto.FullRedAuto;
import dev.raneri.statelib.State;

public class MoveToSecondWobbleGoalContinued extends State {
    private boolean done = false;
    private Trajectory moveToWobbleGoalContinued;
    private FullRedAuto fullRedAuto;
    private Pose2d previous;

    public MoveToSecondWobbleGoalContinued(FullRedAuto fullRedAuto, Pose2d previous) {
        this.fullRedAuto = fullRedAuto;
        this.previous = previous;
        if (fullRedAuto.getHeight() == UGContourRingPipeline.Height.ZERO) {
            done = true;
        } else {
            moveToWobbleGoalContinued = fullRedAuto.getDrive().trajectoryBuilder(previous)
                    .lineToLinearHeading(new Pose2d(-43.0, -37.0,Math.toRadians(0)))
                    .addDisplacementMarker(() -> done = true)
                    .build();
        }
    }

    @Override
    public void onEntry(@Nullable State previousState) {
        if (moveToWobbleGoalContinued != null) {
            fullRedAuto.getDrive().followTrajectoryAsync(moveToWobbleGoalContinued);
        }
    }

    @Nullable
    @Override
    public State getNextState() {
        if (done) {
            if (fullRedAuto.getHeight() == UGContourRingPipeline.Height.ZERO) {
                return new MoveToSecondWobbleGoalContinued2(fullRedAuto, previous);
            } else {
                return new MoveToSecondWobbleGoalContinued2(fullRedAuto, moveToWobbleGoalContinued.end());
            }
        } else {
            return null;
        }
    }
}

