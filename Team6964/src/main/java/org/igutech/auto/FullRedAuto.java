package org.igutech.auto;

import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.acmerobotics.roadrunner.geometry.Vector2d;
import com.acmerobotics.roadrunner.trajectory.MarkerCallback;
import com.acmerobotics.roadrunner.trajectory.Trajectory;
import com.acmerobotics.roadrunner.trajectory.constraints.DriveConstraints;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.igutech.auto.roadrunner.SampleMecanumDrive;
import org.igutech.config.Hardware;
import org.igutech.teleop.Modules.Shooter;
import org.igutech.teleop.Modules.TimerService;

import java.util.HashMap;


@Autonomous
public class FullRedAuto extends LinearOpMode {
    private State currentState = State.PREPARE_TO_SHOOT;
    private Hardware hardware;
    private boolean isShooterEnabled = false;
    private int currentShooterServoLevel = 0;
    private HashMap<Integer, Double> liftPositions;
    private TimerService timerService;
    private final MarkerCallback TRANSITION_STATES = () -> currentState = transition(currentState);

    @Override
    public void runOpMode() throws InterruptedException {

        liftPositions = new HashMap<>();
        liftPositions.put(0, 0.78);
        liftPositions.put(1, 0.65);
        liftPositions.put(2, 0.59);
        liftPositions.put(3, 0.5);

        hardware = new Hardware(hardwareMap);
        Shooter shooter = new Shooter(hardware, false);
        timerService = new TimerService();
        SampleMecanumDrive drive = new SampleMecanumDrive(hardwareMap);
        Pose2d startPose = new Pose2d(-60, -48, Math.toRadians(0));
        drive.setPoseEstimate(startPose);

        shooter.init();

        Trajectory prepareToShoot = drive.trajectoryBuilder(startPose)
                .splineToConstantHeading(new Vector2d(-35.0, -35.0), Math.toRadians(0.0))
                .addDisplacementMarker(TRANSITION_STATES)
                .build();

        Trajectory intakeRingStack = drive.trajectoryBuilder(prepareToShoot.end(), new DriveConstraints(30.0, 30.0, 0.0, Math.toRadians(180), Math.toRadians(180), 0.0))
                .addDisplacementMarker(() -> {
                    hardware.getMotors().get("intake").setPower(1.0);
                })
                .splineTo(new Vector2d(-25.0, -35.0), Math.toRadians(0.0))
                .addDisplacementMarker(() -> {
                    hardware.getMotors().get("intake").setPower(0.0);
                    currentState = transition(currentState);
                })
                .build();

        Trajectory dropFirstWobbleGoal = drive.trajectoryBuilder(intakeRingStack.end())
                .splineTo(new Vector2d(10.0, -45.0), Math.toRadians(0.0))
                .addDisplacementMarker(TRANSITION_STATES)
                .build();
        Trajectory moveToShootRingStack = drive.trajectoryBuilder(dropFirstWobbleGoal.end(), true)
                .splineToConstantHeading(new Vector2d(-5.0, -40.0), Math.toRadians(0.0))
                .addDisplacementMarker(TRANSITION_STATES)
                .build();
        Trajectory grabSecondGoal = drive.trajectoryBuilder(moveToShootRingStack.end())
                .splineToConstantHeading(new Vector2d(-55.0, -40.0), Math.toRadians(0.0))
                .addDisplacementMarker(() -> {
                    currentState = transition(currentState);
                })
                .build();
        Trajectory dropSecondWobbleGoal = drive.trajectoryBuilder(grabSecondGoal.end())
                .splineTo(new Vector2d(10.0, -45.0), Math.toRadians(0.0))
                .addDisplacementMarker(() -> {
                    currentState = transition(currentState);
                })
                .build();
        drive.followTrajectoryAsync(prepareToShoot);
        telemetry.addData("Status: ", "Ready");
        telemetry.update();
        waitForStart();
        timerService.start();
        if (isStopRequested()) return;

        while (!isStopRequested() && opModeIsActive()) {
            switch (currentState) {
                case PREPARE_TO_SHOOT:
                    //drive.followTrajectoryAsync(prepareToShoot);
                    break;
                case SHOOTING_PRELOAD_RINGS:
                    isShooterEnabled = true;
                    handleLift();
                    break;
                case INTAKE_RING_STACK:
                    drive.followTrajectoryAsync(intakeRingStack);
                    break;
                case DROP_FIRST_WOBBLE_GOAL:
                    drive.followTrajectoryAsync(dropFirstWobbleGoal);
                    break;
                case MOVE_TO_SHOOT_RING_STACK:
                    drive.followTrajectoryAsync(moveToShootRingStack);
                    break;
                case SHOOTING_RING_STACK:
                    isShooterEnabled = true;
                    handleLift();
                    break;
                case MOVE_TO_GRAB_SECOND_GOAL:
                    drive.followTrajectoryAsync(grabSecondGoal);
                    break;
                case DROP_SECOND_GOAL:
                    drive.followTrajectoryAsync(dropSecondWobbleGoal);
                    break;
                default:
            }
            shooter.loop();
            timerService.loop();
            drive.update();
            telemetry.addData("Pose", drive.getPoseEstimate());
            telemetry.addData("State", currentState);
            telemetry.update();

        }

    }

    //    public void handleLift() {
//        System.out.println("Running Indexer");
//        if (currentShooterServoLevel > 3) {
//            currentShooterServoLevel = 0;
//        }
//        timerService.registerSingleTimerEvent(600, () -> {
//            System.out.println("Event stuff");
//            hardware.getServos().get("liftServo").setPosition(liftPositions.get(currentShooterServoLevel));
//            timerService.registerSingleTimerEvent(600, () -> {
//                hardware.getServos().get("shooterServo").setPosition(1.0);
//                timerService.registerSingleTimerEvent(150, () -> {
//                    hardware.getServos().get("shooterServo").setPosition(0.0);
//                    currentShooterServoLevel++;
//                    if (currentShooterServoLevel > 3) {
//                        isShooterEnabled = false;
//                    }
//                    if (isShooterEnabled) {
//                        handleLift();
//                    } else {
//                        currentState = transition(currentState);
//                    }
//                });
//            });
//        });
//    }
    public void handleLift() {
        currentShooterServoLevel++;
        if (currentShooterServoLevel > 3) {
            currentShooterServoLevel = 0;
        }
        hardware.getServos().get("liftServo").setPosition(liftPositions.get(currentShooterServoLevel));
        if (currentShooterServoLevel == 0) {
            timerService.registerUniqueTimerEvent(600, () -> {
            });
        } else if (currentShooterServoLevel == 1) {
            timerService.registerUniqueTimerEvent(600, () -> {
                hardware.getServos().get("shooterServo").setPosition(0.32);
                timerService.registerUniqueTimerEvent(150, () -> {
                    hardware.getServos().get("shooterServo").setPosition(0.1);
                });
            });
        } else {
            timerService.registerUniqueTimerEvent(250, () -> {
                hardware.getServos().get("shooterServo").setPosition(0.32);
                timerService.registerUniqueTimerEvent(200, () -> {
                    hardware.getServos().get("shooterServo").setPosition(0.1);
                    currentState = transition(currentState);
                });
            });
        }
    }


    public State transition(State state) {
        System.out.println("Transitioning to state " + state.getNextState() + " from " + currentState);
        return state.getNextState();

    }


}
