package org.igutech.auto.paths;

import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.acmerobotics.roadrunner.trajectory.Trajectory;
import com.arcrobotics.ftclib.vision.UGContourRingPipeline;

import org.igutech.auto.FullRedAuto;
import dev.raneri.statelib.State;
import org.jetbrains.annotations.Nullable;

public class GoToRingStack extends State {
    private boolean done = false;
    private FullRedAuto fullRedAuto;
    private Trajectory goToRingStack;
    private Pose2d previous;
    private UGContourRingPipeline.Height height;

    public GoToRingStack(FullRedAuto auto, Pose2d previous) {
        this.previous = previous;
        this.height = auto.getHeight();
        fullRedAuto = auto;
        if (fullRedAuto.getHeight() == UGContourRingPipeline.Height.ZERO) {
            done = true;
        } else {
            goToRingStack = fullRedAuto.getDrive().trajectoryBuilder(previous)
                    .addDisplacementMarker(() -> {
                        fullRedAuto.getHardware().getServos().get("releaseLiftServo").setPosition(0.2);
                    })
                    .splineToLinearHeading(new Pose2d(0.0, -35.0, Math.toRadians(180.0)), Math.toRadians(180.0))
                    .addDisplacementMarker(() -> done = true)
                    .build();
        }

    }

    @Override
    public void onEntry(@Nullable State previousState) {
        if (goToRingStack != null) {
            fullRedAuto.getDrive().followTrajectoryAsync(goToRingStack);
        }
    }

    @Override
    public @Nullable
    State getNextState() {
        if (done) {
            System.out.println("Transitioning from go to ring stack to intake ring stack");
            if (height == UGContourRingPipeline.Height.ZERO) {
                return new IntakeRingStack(fullRedAuto, previous);
            } else {
                return new IntakeRingStack(fullRedAuto, goToRingStack.end());
            }
        } else {
            return null;
        }
    }
}
