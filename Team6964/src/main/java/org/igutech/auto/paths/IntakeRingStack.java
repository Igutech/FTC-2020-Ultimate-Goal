package org.igutech.auto.paths;

import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.acmerobotics.roadrunner.geometry.Vector2d;
import com.acmerobotics.roadrunner.trajectory.Trajectory;
import com.acmerobotics.roadrunner.trajectory.constraints.DriveConstraints;
import com.arcrobotics.ftclib.vision.UGContourRingPipeline;

import org.igutech.auto.FullRedAuto;

import dev.raneri.statelib.State;

import org.igutech.teleop.Modules.Shooter;
import org.jetbrains.annotations.Nullable;


public class IntakeRingStack extends State {
    private boolean done = false;
    private FullRedAuto fullRedAuto;
    private Trajectory inTakeRingStack;
    private Trajectory intakeRingStackC4;
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

            Trajectory intakeRingStackC = fullRedAuto.getDrive().trajectoryBuilder(previous, new DriveConstraints(15, 15, 0.0, Math.toRadians(180), Math.toRadians(180), 0.0))
                    .addDisplacementMarker(() -> {
                        fullRedAuto.getHardware().getMotors().get("intake").setPower(-1);
                        fullRedAuto.getHardware().getMotors().get("intake2").setPower(-1);
                    })
                    .lineToLinearHeading(new Pose2d(-30, -38.0, 0))
                    .addDisplacementMarker(() -> {
                        fullRedAuto.getTimerService().registerUniqueTimerEvent(500, "shoot", () -> {
                            fullRedAuto.getHardware().getMotors().get("intake").setPower(0);
                            fullRedAuto.getHardware().getMotors().get("intake2").setPower(0);
                            fullRedAuto.setShooterEnabled(true);

                            fullRedAuto.handleLift(2, false, () -> {
                                    intakestate = INTAKESTATE.IntakeC4;
                                System.out.println("Finished shooting 1 ring of the stack");
                            });
                        });
                    })
                    .build();
            inTakeRingStack = intakeRingStackC;

            intakeRingStackC4 = fullRedAuto.getDrive().trajectoryBuilder(intakeRingStackC.end(), new DriveConstraints(15, 15, 0.0, Math.toRadians(180), Math.toRadians(180), 0.0))
                    .addDisplacementMarker(() -> {
                        fullRedAuto.getHardware().getMotors().get("intake").setPower(-1);
                        fullRedAuto.getHardware().getMotors().get("intake2").setPower(-1);
                    })
                    .lineToConstantHeading(new Vector2d(-15.0, -38.0))
                    .addDisplacementMarker(() -> {
                        Shooter.frontShooterTargetVelo = -1800;
                       // fullRedAuto.getHardware().getServos().get("liftServo").setPosition(0.86);

                       fullRedAuto.getTimerService().registerUniqueTimerEvent(300,"shooter",()->{

                           fullRedAuto.getHardware().getMotors().get("intake").setPower(0);
                           fullRedAuto.getHardware().getMotors().get("intake2").setPower(0);
                           fullRedAuto.setShooterEnabled(true);
                           fullRedAuto.handleLift(1, false, () -> {
                               System.out.println("Finished shooting 2-4th ring of the stack");
                               intakestate = INTAKESTATE.OFF;
                           });
                       });

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
            if (fullRedAuto.getHeight() == UGContourRingPipeline.Height.ZERO) {
                return new GoToSecondWobbleGoal(fullRedAuto, previous);
            } else if (fullRedAuto.getHeight() == UGContourRingPipeline.Height.ONE) {
                return new GoToSecondWobbleGoal(fullRedAuto, inTakeRingStack.end());
            } else {
                return new MoveToTargetZoneFirstTime(fullRedAuto, intakeRingStackC4.end());
            }
        }
        return null;
    }

    @Override
    public void loop() {
        if (intakestate == INTAKESTATE.IntakeC2) {
            intakestate = INTAKESTATE.RUNNING;
        } else if (intakestate == INTAKESTATE.IntakeC3) {
            intakestate = INTAKESTATE.RUNNING;
        } else if (intakestate == INTAKESTATE.IntakeC4) {
            fullRedAuto.getDrive().followTrajectoryAsync(intakeRingStackC4);
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
        IntakeC4,
        RUNNING,
        OFF;
    }

}
