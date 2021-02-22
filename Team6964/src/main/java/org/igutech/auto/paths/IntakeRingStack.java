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
    private INTAKESTATE intakestate = INTAKESTATE.Intake;
    private Pose2d previous;

    public IntakeRingStack(FullRedAuto fullRedAuto, Pose2d previous) {
        this.fullRedAuto = fullRedAuto;
        this.previous = previous;
        if (fullRedAuto.getHeight() == UGContourRingPipeline.Height.ZERO) {
            intakestate = INTAKESTATE.OFF;
            done = true;
        } else if (fullRedAuto.getHeight() == UGContourRingPipeline.Height.ONE) {
            Trajectory intakeRingStackB = fullRedAuto.getDrive().trajectoryBuilder(previous, new DriveConstraints(30.0, 30.0, 0.0, Math.toRadians(180), Math.toRadians(180), 0.0))
                    .addDisplacementMarker(() -> {
                        fullRedAuto.getHardware().getMotors().get("intake").setPower(-1);
                        fullRedAuto.getHardware().getMotors().get("intake2").setPower(-1);
                    })
                    .splineToConstantHeading(new Vector2d(-25.0, -38), Math.toRadians(180.0))
                    .addDisplacementMarker(() -> {
                        done = true;
                        intakestate = INTAKESTATE.OFF;
                    })
                    .build();
            inTakeRingStack = intakeRingStackB;
        } else {
            Trajectory intakeRingStackC = fullRedAuto.getDrive().trajectoryBuilder(previous, new DriveConstraints(30.0, 30.0, 0.0, Math.toRadians(180), Math.toRadians(180), 0.0))
                    .addDisplacementMarker(() -> {
                      //  fullRedAuto.getHardware().getMotors().get("intake").setPower(-1);
                      //  fullRedAuto.getHardware().getMotors().get("intake2").setPower(-1);
                    })
                    .splineToConstantHeading(new Vector2d(-29.0, -38.0), Math.toRadians(180.0))
                    .addDisplacementMarker(() -> {
                        //fullRedAuto.getTimerService().registerUniqueTimerEvent(500, "Intake", () -> intakestate = INTAKESTATE.IntakeC2);
                       intakestate = INTAKESTATE.IntakeC2;
                    })
                    .build();
            inTakeRingStack = intakeRingStackC;

            intakeRingStackC2 = fullRedAuto.getDrive().trajectoryBuilder(intakeRingStackC.end(), new DriveConstraints(30.0, 30.0, 0.0, Math.toRadians(180), Math.toRadians(180), 0.0))
                    .lineToConstantHeading(new Vector2d(-27.0, -38.0))
                    .addDisplacementMarker(() -> {
                        //fullRedAuto.getTimerService().registerUniqueTimerEvent(500, "Intake", () -> intakestate = INTAKESTATE.IntakeC3);
                        intakestate = INTAKESTATE.IntakeC3;
                    })
                    .build();
            intakeRingStackC3 = fullRedAuto.getDrive().trajectoryBuilder(intakeRingStackC2.end(), new DriveConstraints(30.0, 30.0, 0.0, Math.toRadians(180), Math.toRadians(180), 0.0))
                    .lineToConstantHeading(new Vector2d(-25.0, -38.0))
                    .addDisplacementMarker(() -> {
                        //fullRedAuto.getTimerService().registerUniqueTimerEvent(500, "Intake", () -> intakestate = INTAKESTATE.OFF);
                        intakestate = INTAKESTATE.OFF;
                    })
                    .build();
        }
    }

    @Override
    public void onEntry(@Nullable State previousState) {
        if (inTakeRingStack != null) {
            fullRedAuto.getDrive().followTrajectoryAsync(inTakeRingStack);
            intakestate = INTAKESTATE.RUNNING;
        }
    }

    @Override
    public @Nullable
    State getNextState() {
        if (done) {
            System.out.println("Transitioning intake ring stack to go to second wobble goal");

            if (fullRedAuto.getHeight() == UGContourRingPipeline.Height.ZERO) {
                return new GoToSecondWobbleGoal(fullRedAuto, previous);
            } else if (fullRedAuto.getHeight() == UGContourRingPipeline.Height.ONE) {
                return new GoToSecondWobbleGoal(fullRedAuto, inTakeRingStack.end());
            } else {
                return new MoveToTargetZoneFirstTime(fullRedAuto, intakeRingStackC3.end());
            }
        }
        return null;
    }

    @Override
    public void loop() {
        if (intakestate == INTAKESTATE.IntakeC2) {
            fullRedAuto.getDrive().followTrajectoryAsync(intakeRingStackC2);
            intakestate = INTAKESTATE.RUNNING;
        } else if (intakestate == INTAKESTATE.IntakeC3) {
            fullRedAuto.getDrive().followTrajectoryAsync(intakeRingStackC3);
            intakestate = INTAKESTATE.RUNNING;
        } else if (intakestate == INTAKESTATE.RUNNING) {

        } else {
            done = true;
        }
    }

    private enum INTAKESTATE {
        Intake,
        IntakeC2,
        IntakeC3,
        RUNNING,
        OFF;
    }

}
