package org.igutech.auto.paths;

import androidx.annotation.Nullable;

import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.acmerobotics.roadrunner.geometry.Vector2d;
import com.acmerobotics.roadrunner.trajectory.Trajectory;
import com.arcrobotics.ftclib.vision.UGContourRingPipeline;

import org.igutech.auto.FullRedAuto;
import org.igutech.auto.statelib.State;

public class MoveToTargetZoneSecondTime extends State {
    private FullRedAuto fullRedAuto;
    private Pose2d previous;
    private boolean done = false;
    private Trajectory moveToTargetZone;

    public MoveToTargetZoneSecondTime(FullRedAuto fullRedAuto, Pose2d previous) {
        this.fullRedAuto = fullRedAuto;
        this.previous = previous;
        if (fullRedAuto.getHeight() == UGContourRingPipeline.Height.ZERO) {
            moveToTargetZone = fullRedAuto.getDrive().trajectoryBuilder(previous)
                    .splineToConstantHeading(new Vector2d(20, -45), Math.toRadians(0.0))
                    .addDisplacementMarker(() -> fullRedAuto.dropWobbleGoal(() -> done = true))
                    .build();
        } else if (fullRedAuto.getHeight() == UGContourRingPipeline.Height.ONE) {
            moveToTargetZone = fullRedAuto.getDrive().trajectoryBuilder(previous)
                    .splineToConstantHeading(new Vector2d(36, -24), Math.toRadians(0.0))
                    .addDisplacementMarker(() -> fullRedAuto.dropWobbleGoal(() -> done = true))
                    .build();
        } else {
            moveToTargetZone = fullRedAuto.getDrive().trajectoryBuilder(previous)
                    .lineToLinearHeading(new Pose2d(45, -55, Math.toRadians(90)))
                    .addSpatialMarker(new Vector2d(35, -50), () -> fullRedAuto.dropWobbleGoal(() -> done = true))
                    .build();
        }

    }

    @Override
    public void onEntry(@Nullable State previousState) {
        fullRedAuto.getDrive().followTrajectoryAsync(moveToTargetZone);
    }

    @Nullable
    @Override
    public State getNextState() {
        if (done) {
            return new Park(fullRedAuto, previous);
        } else {
            return null;
        }
    }
}
