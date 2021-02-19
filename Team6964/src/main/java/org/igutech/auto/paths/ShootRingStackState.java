package org.igutech.auto.paths;

import androidx.annotation.Nullable;

import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.arcrobotics.ftclib.vision.UGContourRingPipeline;

import org.igutech.auto.FullRedAuto;
import org.igutech.auto.statelib.State;

public class ShootRingStackState extends State {

    private FullRedAuto fullRedAuto;
    private Pose2d previous;
    private boolean done = false;

    public ShootRingStackState(FullRedAuto fullRedAuto, Pose2d previous) {
        this.fullRedAuto = fullRedAuto;
        this.previous = previous;
    }

    @Override
    public void onEntry(@Nullable State previousState) {
        if (fullRedAuto.getHeight() == UGContourRingPipeline.Height.ZERO) {
            done = true;
        } else if (fullRedAuto.getHeight() == UGContourRingPipeline.Height.ONE) {
            fullRedAuto.setShooterEnabled(true);
            fullRedAuto.handleLift(3, true, () -> done = true);
        } else {
            fullRedAuto.setShooterEnabled(true);
            fullRedAuto.handleLift(1, true, () -> done = true);
        }
    }

    @Nullable
    @Override
    public State getNextState() {
        if (done) {
            return new MoveToTargetZoneSecondTime(fullRedAuto, previous);
        }
        return null;
    }
}
