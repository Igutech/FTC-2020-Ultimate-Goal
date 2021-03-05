package org.igutech.auto.paths;

import androidx.annotation.Nullable;

import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.acmerobotics.roadrunner.geometry.Vector2d;
import com.acmerobotics.roadrunner.trajectory.Trajectory;
import com.arcrobotics.ftclib.vision.UGContourRingPipeline;

import org.igutech.auto.FullRedAuto;
import dev.raneri.statelib.State;

public class MoveToSecondWobbleGoalContinued2 extends State {
    private boolean done = false;
    private FullRedAuto fullRedAuto;
    private Trajectory moveToSecondWobbleGoalContinued2;

    public MoveToSecondWobbleGoalContinued2(FullRedAuto fullRedAuto, Pose2d previous) {
        this.fullRedAuto = fullRedAuto;
        if (fullRedAuto.getHeight() == UGContourRingPipeline.Height.FOUR) {
            moveToSecondWobbleGoalContinued2 = fullRedAuto.getDrive().trajectoryBuilder(previous)
                    .lineToLinearHeading(new Pose2d(-43, -38,Math.toRadians(0)))
                    .addDisplacementMarker(() -> fullRedAuto.grabWobbleGoal(() -> done = true))
                    .build();
        } else {
            moveToSecondWobbleGoalContinued2 = fullRedAuto.getDrive().trajectoryBuilder(previous)
                    .lineToConstantHeading(new Vector2d(-44.5, -37))
                    .addDisplacementMarker(() -> fullRedAuto.grabWobbleGoal(() -> done = true))
                    .build();
        }

    }

    @Override
    public void onEntry(@Nullable State previousState) {
        fullRedAuto.getDrive().followTrajectoryAsync(moveToSecondWobbleGoalContinued2);
    }

    @Nullable
    @Override
    public State getNextState() {
        if (done) {
            if (fullRedAuto.getHeight() == UGContourRingPipeline.Height.FOUR) {
                return new MoveToTargetZoneSecondTime(fullRedAuto, moveToSecondWobbleGoalContinued2.end());
            }
            return new MoveToShootRingStack(fullRedAuto, moveToSecondWobbleGoalContinued2.end());
        } else {
            return null;
        }
    }
}
