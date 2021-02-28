package org.igutech.auto.paths;

import androidx.annotation.Nullable;

import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.acmerobotics.roadrunner.trajectory.Trajectory;
import com.arcrobotics.ftclib.vision.UGContourRingPipeline;

import org.igutech.auto.FullRedAuto;
import dev.raneri.statelib.State;

public class ShootingPreloadRingsState extends State {
    private FullRedAuto fullRedAuto;
    private boolean done = false;
    private Pose2d previous;
    private Trajectory backUpFromRingStack;

    public ShootingPreloadRingsState(FullRedAuto fullRedAuto, Pose2d previous) {
        this.fullRedAuto = fullRedAuto;
        this.previous = previous;
    }

    @Override
    public void onEntry(@Nullable State previousState) {
        if (fullRedAuto.getHeight() == UGContourRingPipeline.Height.FOUR) {
            backUpFromRingStack = fullRedAuto.getDrive().trajectoryBuilder(previous)
                    .lineToLinearHeading(new Pose2d(-35.0, -38.0, 0))
                    .addDisplacementMarker(() -> {
                        fullRedAuto.getHardware().getServos().get("releaseLiftServo").setPosition(0.2);
                        done = true;
                    })
                    .build();
        }
        fullRedAuto.setShooterEnabled(true);
        fullRedAuto.handleLift(1, true, () -> {
            if (fullRedAuto.getHeight() == UGContourRingPipeline.Height.FOUR) {
                fullRedAuto.getDrive().followTrajectoryAsync(backUpFromRingStack);
            } else {
                done = true;
            }
            System.out.println("Shooting preload ring finished");
        });
    }

    @Override
    @Nullable
    public State getNextState() {
        if (done) {
            if (fullRedAuto.getHeight() == UGContourRingPipeline.Height.FOUR) {
                return new IntakeRingStack(fullRedAuto, previous);
            } else {
                return new MoveToTargetZoneFirstTime(fullRedAuto, previous);
            }
        }
        return null;
    }
}
