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

    public ShootingPreloadRingsState(FullRedAuto fullRedAuto, Pose2d previous) {
        this.fullRedAuto = fullRedAuto;
        this.previous = previous;
    }

    @Override
    public void onEntry(@Nullable State previousState) {
        fullRedAuto.getHardware().getServos().get("releaseLiftServo").setPosition(0.2);
        fullRedAuto.setShooterEnabled(true);
        fullRedAuto.handleLift(1, true, () -> {
           done = true;
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
