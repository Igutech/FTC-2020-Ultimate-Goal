package org.igutech.auto.paths;

import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.acmerobotics.roadrunner.geometry.Vector2d;
import com.acmerobotics.roadrunner.trajectory.Trajectory;
import com.arcrobotics.ftclib.vision.UGContourRingPipeline;

import org.igutech.auto.FullRedAuto;
import org.igutech.auto.statelib.State;
import org.jetbrains.annotations.Nullable;


public class MoveToTargetZoneFirstTime extends State {

    private boolean done = false;
    private Trajectory moveToTargetZone;
    private FullRedAuto fullRedAuto;

    public MoveToTargetZoneFirstTime(FullRedAuto fullRedAuto, Pose2d previous) {
        this.fullRedAuto = fullRedAuto;
        if (fullRedAuto.getHeight() == UGContourRingPipeline.Height.ZERO) {
            Trajectory moveToRedA = fullRedAuto.getDrive().trajectoryBuilder(previous)
                    .splineToConstantHeading(new Vector2d(12.0, -50), Math.toRadians(0.0))
                    .addDisplacementMarker(() -> done = true)
                    .build();
            moveToTargetZone = moveToRedA;
        } else if (fullRedAuto.getHeight() == UGContourRingPipeline.Height.ONE) {
            Trajectory moveToRedB = fullRedAuto.getDrive().trajectoryBuilder(previous)
                    .splineToConstantHeading(new Vector2d(42.0, -28.0), Math.toRadians(0.0))
                    .addDisplacementMarker(() -> done = true)
                    .build();
            moveToTargetZone = moveToRedB;

        } else {
            Trajectory moveToRedC = fullRedAuto.getDrive().trajectoryBuilder(previous)
                    .lineToLinearHeading(new Pose2d(50.0, -54, Math.toRadians(90)))
                    .addDisplacementMarker(() -> done = true)
                    .build();
            moveToTargetZone = moveToRedC;
        }

    }

    @Override
    public void onEntry(@Nullable State previousState) {
        fullRedAuto.getDrive().followTrajectoryAsync(moveToTargetZone);
    }


    @Override
    public @Nullable
    State getNextState() {
        if (done) {
            return new MoveToTargetZoneFirstTime(fullRedAuto, moveToTargetZone.end());
        } else {
            return null;
        }
    }
}
