package org.igutech.auto.paths;

import androidx.annotation.Nullable;

import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.acmerobotics.roadrunner.geometry.Vector2d;
import com.acmerobotics.roadrunner.trajectory.Trajectory;
import com.arcrobotics.ftclib.vision.UGContourRingPipeline;

import org.igutech.auto.FullRedAuto;
import dev.raneri.statelib.State;

public class Park extends State {
    private FullRedAuto fullRedAuto;
    private Pose2d previous;
    private Trajectory park;
    private boolean done = false;

    public Park(FullRedAuto fullRedAuto, Pose2d previous) {
        this.fullRedAuto = fullRedAuto;
        this.previous = previous;
        if (fullRedAuto.getHeight() == UGContourRingPipeline.Height.ZERO) {
            park = fullRedAuto.getDrive().trajectoryBuilder(previous)
                    .splineToConstantHeading(new Vector2d(18, -35), Math.toRadians(0.0))
                    .addDisplacementMarker(() -> done = true)
                    .build();
        } else if (fullRedAuto.getHeight() == UGContourRingPipeline.Height.ONE) {
            park = fullRedAuto.getDrive().trajectoryBuilder(previous)
                    .lineToConstantHeading(new Vector2d(15, -20))
                    .addDisplacementMarker(() -> done = true)
                    .build();
        } else {
            park = fullRedAuto.getDrive().trajectoryBuilder(previous)
                    .lineToLinearHeading(new Pose2d(-10, -10,Math.toRadians(90)))
                    .addDisplacementMarker(() -> done = true)
                    .build();
        }
    }

    @Override
    public void onEntry(@Nullable State previousState) {
        fullRedAuto.getDrive().followTrajectoryAsync(park);
    }

    @Nullable
    @Override
    public State getNextState() {
        return null;
    }
}
