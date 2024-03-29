package org.igutech.auto.paths;

import androidx.annotation.Nullable;

import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.acmerobotics.roadrunner.geometry.Vector2d;
import com.acmerobotics.roadrunner.trajectory.Trajectory;
import com.arcrobotics.ftclib.vision.UGContourRingPipeline;
import dev.raneri.statelib.State;
import org.igutech.auto.FullRedAuto;

public class GoToSecondWobbleGoal extends State {
    private Trajectory moveToSecondWobbleGoal;
    private boolean done = false;
    private FullRedAuto fullRedAuto;

    public GoToSecondWobbleGoal(FullRedAuto fullRedAuto, Pose2d previous) {
        this.fullRedAuto = fullRedAuto;
        if (fullRedAuto.getHeight() == UGContourRingPipeline.Height.ZERO) {
            Trajectory moveToWobbleGoalA = fullRedAuto.getDrive().trajectoryBuilder(previous)
                    .lineToConstantHeading(new Vector2d(-44.5, -30.0))
                    .addDisplacementMarker(() -> done = true)
                    .build();
            moveToSecondWobbleGoal = moveToWobbleGoalA;
        } else if(fullRedAuto.getHeight()== UGContourRingPipeline.Height.ONE){
            Trajectory moveToSecondWobbleGoalB = fullRedAuto.getDrive().trajectoryBuilder(previous)
                    .addDisplacementMarker(3, () -> {
                        fullRedAuto.getHardware().getMotors().get("intake").setPower(0);
                        fullRedAuto.getHardware().getMotors().get("intake2").setPower(0);
                    })
                    .splineToLinearHeading(new Pose2d(-35.0, -30.0, Math.toRadians(0.0)), Math.toRadians(0.0))
                    .addDisplacementMarker(() -> done = true)
                    .build();
            moveToSecondWobbleGoal = moveToSecondWobbleGoalB;
        } else {
            Trajectory moveToSecondWobbleGoalC = fullRedAuto.getDrive().trajectoryBuilder(previous)
                    .addDisplacementMarker(3, () -> {
                        fullRedAuto.getHardware().getMotors().get("intake").setPower(0);
                        fullRedAuto.getHardware().getMotors().get("intake2").setPower(0);
                    })
                    .lineToLinearHeading(new Pose2d(0.0, -25.0, Math.toRadians(0.0)))
                    .addDisplacementMarker(() -> done = true)
                    .build();
            moveToSecondWobbleGoal = moveToSecondWobbleGoalC;
        }
    }

    @Override
    public void onEntry(@Nullable State previousState) {
        fullRedAuto.getDrive().followTrajectoryAsync(moveToSecondWobbleGoal);
    }

    @Nullable
    @Override
    public State getNextState() {
        if (done) {
            return new MoveToSecondWobbleGoalContinued(fullRedAuto, moveToSecondWobbleGoal.end());
        } else {
            return null;
        }
    }
}
