package org.igutech.auto.paths;

import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.acmerobotics.roadrunner.geometry.Vector2d;
import com.acmerobotics.roadrunner.trajectory.Trajectory;
import com.arcrobotics.ftclib.vision.UGContourRingPipeline;

import org.igutech.auto.FullRedAuto;
import dev.raneri.statelib.State;
import org.jetbrains.annotations.Nullable;


public class MoveAwayFromTargetZone extends State {
    private boolean done = false;
    private FullRedAuto fullRedAuto;
    private Trajectory moveAwayFromTargetZone;

    public MoveAwayFromTargetZone(FullRedAuto fullRedAuto, Pose2d previous) {
        this.fullRedAuto = fullRedAuto;
        if (fullRedAuto.getHeight() == UGContourRingPipeline.Height.ZERO) {
            Trajectory moveAwayFromRedA = fullRedAuto.getDrive().trajectoryBuilder(previous)
                    .splineToConstantHeading(new Vector2d(12, -35), Math.toRadians(0.0))
                    .addDisplacementMarker(() -> done = true)
                    .build();
            moveAwayFromTargetZone = moveAwayFromRedA;
        } else if (fullRedAuto.getHeight() == UGContourRingPipeline.Height.ONE) {
            Trajectory moveAwayFromRedB = fullRedAuto.getDrive().trajectoryBuilder(previous)
                    .splineToConstantHeading(new Vector2d(42.0, -20), Math.toRadians(0.0))
                    .splineToConstantHeading(new Vector2d(20.0, -20), Math.toRadians(0.0))
                    .addDisplacementMarker(() -> done = true)
                    .build();
            moveAwayFromTargetZone = moveAwayFromRedB;
        } else {
            Trajectory moveAwayFromRedC = fullRedAuto.getDrive().trajectoryBuilder(previous)
                    .splineToConstantHeading(new Vector2d(50, -40), Math.toRadians(0.0))
                    .splineToConstantHeading(new Vector2d(20, -40), Math.toRadians(0.0))
                    .addDisplacementMarker(() -> done = true)
                    .build();
            moveAwayFromTargetZone = moveAwayFromRedC;
        }
    }

    @Override
    public void onEntry(@Nullable State previousState) {
        fullRedAuto.getDrive().followTrajectoryAsync(moveAwayFromTargetZone);
    }

    @Override
    public @Nullable
    State getNextState() {
        if (done) {
            if (fullRedAuto.getHeight() == UGContourRingPipeline.Height.FOUR) {
                return new Park(fullRedAuto, moveAwayFromTargetZone.end());
            }
            return new GoToRingStack(fullRedAuto, moveAwayFromTargetZone.end());
        } else {
            return null;
        }
    }
}
