package org.igutech.auto.paths;

import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.acmerobotics.roadrunner.geometry.Vector2d;
import com.acmerobotics.roadrunner.trajectory.Trajectory;
import com.acmerobotics.roadrunner.trajectory.constraints.DriveConstraints;
import com.arcrobotics.ftclib.vision.UGContourRingPipeline;

import org.igutech.auto.FullRedAuto;
import org.igutech.auto.statelib.State;
import org.jetbrains.annotations.Nullable;


public class IntakeRingStack extends State {
    private boolean done = false;
    private FullRedAuto fullRedAuto;
    private Trajectory inTakeRingStack;
    private Trajectory intakeRingStackC2;
    private Trajectory intakeRingStackC3;
    public IntakeRingStack(FullRedAuto fullRedAuto, Pose2d previous) {
        this.fullRedAuto = fullRedAuto;
        if (fullRedAuto.getHeight() == UGContourRingPipeline.Height.ZERO) {
            done=true;
        } else if (fullRedAuto.getHeight() == UGContourRingPipeline.Height.ONE) {
            Trajectory intakeRingStackB = fullRedAuto.getDrive().trajectoryBuilder(previous, new DriveConstraints(30.0, 30.0, 0.0, Math.toRadians(180), Math.toRadians(180), 0.0))
                    .addDisplacementMarker(() -> {
                        fullRedAuto.getHardware().getMotors().get("intake").setPower(-1);
                        fullRedAuto.getHardware().getMotors().get("intake2").setPower(-1);
                    })
                    .splineToConstantHeading(new Vector2d(-25.0, -35), Math.toRadians(180.0))
                    .addDisplacementMarker(() -> done = true)
                    .build();
            inTakeRingStack = intakeRingStackB;
        }else{
            Trajectory intakeRingStackC = fullRedAuto.getDrive().trajectoryBuilder(previous, new DriveConstraints(30.0, 30.0, 0.0, Math.toRadians(180), Math.toRadians(180), 0.0))
                    .addDisplacementMarker(() -> {
                        fullRedAuto.getHardware().getMotors().get("intake").setPower(-1);
                        fullRedAuto.getHardware().getMotors().get("intake2").setPower(-1);
                    })
                    .splineToConstantHeading(new Vector2d(-12.0, -38), Math.toRadians(180.0))
                    .addDisplacementMarker(()->{
                        fullRedAuto.getTimerService().registerUniqueTimerEvent(1000,"Intake",() -> fullRedAuto.getDrive().followTrajectoryAsync(intakeRingStackC2) );
                    })
                    .build();
            inTakeRingStack = intakeRingStackC;

            intakeRingStackC2 = fullRedAuto.getDrive().trajectoryBuilder(intakeRingStackC.end(), new DriveConstraints(30.0, 30.0, 0.0, Math.toRadians(180), Math.toRadians(180), 0.0))
                    .lineToConstantHeading(new Vector2d(-14.0, -38))
                    .addDisplacementMarker(()->{
                        fullRedAuto.getTimerService().registerUniqueTimerEvent(1000,"Intake",() ->fullRedAuto.getDrive().followTrajectoryAsync(intakeRingStackC3));
                    })
                    .build();
            intakeRingStackC3 = fullRedAuto.getDrive().trajectoryBuilder(intakeRingStackC.end(), new DriveConstraints(30.0, 30.0, 0.0, Math.toRadians(180), Math.toRadians(180), 0.0))
                    .lineToConstantHeading(new Vector2d(-16.0, -38))
                    .addDisplacementMarker(()->{
                        fullRedAuto.getTimerService().registerUniqueTimerEvent(1000,"Intake",() -> done = true );
                    })
                    .build();
        }
    }

    @Override
    public void onEntry(@Nullable State previousState) {
        fullRedAuto.getDrive().followTrajectoryAsync(inTakeRingStack);
    }

    @Override
    public @Nullable State getNextState() {
        return null;
    }

    @Override
    public void loop() {

    }
}
