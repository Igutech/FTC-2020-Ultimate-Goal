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
import org.igutech.teleop.Teleop;

import java.util.HashMap;


@Autonomous
public class FullRedAuto extends LinearOpMode {
    private State currentState = State.PREPARE_TO_SHOOT;
    private Hardware hardware;
    private boolean isShooterEnabled = false;
    private int currentShooterServoLevel = 0;
    private HashMap<Integer, Double> liftPositions;
    private TimerService timerService;
    private final MarkerCallback TRANSITION_STATES = () -> transition(currentState);
    private SampleMecanumDrive drive;
    private Shooter shooter;
    private Trajectory prepareToShoot;
    private Trajectory dropOffFirstWobbleGoal;
    private Trajectory goToRingStack;
    private Trajectory intakeRingStack;
    private Trajectory goToSecondWobbleGoal;
    private Trajectory moveToShootRingStack;

    @Override
    public void runOpMode() throws InterruptedException {
        hardware = new Hardware(hardwareMap);

        hardware.getServos().get("wobbleGoalServo").setPosition(0.47);
        hardware.getServos().get("releaseLiftServo").setPosition(0.4);


        liftPositions = new HashMap<>();
        liftPositions.put(0, 0.78);
        liftPositions.put(1, 0.65);
        liftPositions.put(2, 0.59);
        liftPositions.put(3, 0.5);

        shooter = new Shooter(hardware, false);
        timerService = new TimerService();
        drive = new SampleMecanumDrive(hardwareMap);
        Pose2d startPose = new Pose2d(-63, -35, Math.toRadians(0));
        drive.setPoseEstimate(startPose);

        shooter.init();

        prepareToShoot = drive.trajectoryBuilder(startPose, new DriveConstraints(20,20,0,Math.toRadians(180),Math.toRadians(180),0))
                .addDisplacementMarker(()->{
                    hardware.getServos().get("wobbleGoalLift").setPosition(0.15);
                })
                .splineToConstantHeading(new Vector2d(-25.0, -10.0), Math.toRadians(0.0))
                .splineToConstantHeading(new Vector2d(-15.0, -10.0), Math.toRadians(0.0))
                .splineToConstantHeading(new Vector2d(-5.0, -40.0), Math.toRadians(0.0))
                .addDisplacementMarker(()->{
                    transition(currentState);
                })
                .build();

        dropOffFirstWobbleGoal = drive.trajectoryBuilder(prepareToShoot.end())
                .splineToConstantHeading(new Vector2d(15.0, -45.0), Math.toRadians(0.0))
                .addDisplacementMarker(5,()->{
                    hardware.getServos().get("wobbleGoalLift").setPosition(1);
                })
                .addDisplacementMarker(()->{
                    hardware.getServos().get("wobbleGoalServo").setPosition(0.25);
                })
                .splineToConstantHeading(new Vector2d(15.0, -35.0), Math.toRadians(0.0))
                .addDisplacementMarker(()->{
                    transition(currentState);
                })
                .build();

        goToRingStack = drive.trajectoryBuilder(dropOffFirstWobbleGoal.end())
                .addDisplacementMarker(()->{
                    hardware.getServos().get("wobbleGoalLift").setPosition(0.15);
                })
                .splineToLinearHeading(new Pose2d(0.0, -40.0, Math.toRadians(180.0)), Math.toRadians(180.0))
                .addDisplacementMarker(() -> {
                    transition(currentState);
                })
                .build();
        intakeRingStack = drive.trajectoryBuilder(goToRingStack.end(), new DriveConstraints(20, 20, 0, Math.toRadians(180), Math.toRadians(180), 0))
                .addDisplacementMarker(()->{
                    hardware.getMotors().get("intake").setPower(-1);
                    hardware.getMotors().get("intake2").setPower(-1);
                })
                .splineToConstantHeading(new Vector2d(-25.0, -40.0), Math.toRadians(180.0))
                .addDisplacementMarker(TRANSITION_STATES)
                .build();
        goToSecondWobbleGoal = drive.trajectoryBuilder(intakeRingStack.end())
                .addDisplacementMarker(()->{
                    hardware.getMotors().get("intake").setPower(0);
                    hardware.getMotors().get("intake2").setPower(0);
                })
                .splineToLinearHeading(new Pose2d(-45.0, -40.0, Math.toRadians(0.0)), Math.toRadians(0.0))

                .addDisplacementMarker(() -> {
                    transition(currentState);
                })
                .build();
        moveToShootRingStack = drive.trajectoryBuilder(goToSecondWobbleGoal.end())
                .splineToLinearHeading(new Pose2d(-0.0, -40.0, Math.toRadians(0.0)), Math.toRadians(0.0))
                .addDisplacementMarker(() -> {
                    transition(currentState);
                })
                .build();
        telemetry.addData("Status: ", "Ready");
        telemetry.addData("Pose: ", drive.getPoseEstimate());
        telemetry.update();
        waitForStart();
        timerService.start();
        drive.followTrajectoryAsync(prepareToShoot);

        //if (isStopRequested()) return;

        while (!isStopRequested() && opModeIsActive()) {
            shooter.loop();
            timerService.loop();
            drive.update();
            telemetry.addData("Pose", drive.getPoseEstimate());
            telemetry.addData("State", currentState);

            telemetry.update();
        }

    }

    public void handleLift() {
        shooter.setEnableShooter(true);
        System.out.println("Running Indexer");
        if (currentShooterServoLevel > 3) {
            currentShooterServoLevel = 0;
        }
        timerService.registerUniqueTimerEvent(600, () -> {
            hardware.getServos().get("liftServo").setPosition(liftPositions.get(currentShooterServoLevel));
            timerService.registerUniqueTimerEvent(600, () -> {
                hardware.getServos().get("shooterServo").setPosition(1.0);
                timerService.registerUniqueTimerEvent(150, () -> {
                    hardware.getServos().get("shooterServo").setPosition(0.0);
                    currentShooterServoLevel++;
                    if (currentShooterServoLevel > 3) {
                        isShooterEnabled = false;
                    }
                    if (isShooterEnabled) {
                        handleLift();
                    } else {
                        currentShooterServoLevel = 0;
                        hardware.getServos().get("liftServo").setPosition(liftPositions.get(currentShooterServoLevel));
                        timerService.registerUniqueTimerEvent(750,()->{
                            shooter.setEnableShooter(false);
                            transition(currentState);
                        });
                    }
                });
            });
        });
    }


    public void transition(State state) {
        System.out.println("Transitioning to state " + state.getNextState() + " from " + currentState);
        currentState = state.getNextState();
        switch (currentState) {
            case PREPARE_TO_SHOOT:
                //drive.followTrajectoryAsync(prepareToShoot);
                break;
            case SHOOTING_PRELOAD_RINGS:
                isShooterEnabled = true;
                handleLift();
                break;
            case DROP_FIRST_WOBBLE_GOAL:
                hardware.getServos().get("releaseLiftServo").setPosition(0.2);
                drive.followTrajectoryAsync(dropOffFirstWobbleGoal);
                break;
            case MOVE_TO_TO_RING_STACK:
                    drive.followTrajectoryAsync(goToRingStack);

                break;
            case INTAKE_RING_STACK:
                drive.followTrajectoryAsync(intakeRingStack);
                break;
            case MOVE_TO_GRAB_SECOND_GOAL:
                drive.followTrajectoryAsync(goToSecondWobbleGoal);
                break;
            case GRAB_SECOND_WOBBLE_GOAL:
                timerService.registerUniqueTimerEvent(500,()->{
                    hardware.getServos().get("wobbleGoalLift").setPosition(1);
                    timerService.registerUniqueTimerEvent(500,()->{
                       hardware.getServos().get("wobbleGoalServo").setPosition(0.25);
                        timerService.registerUniqueTimerEvent(500,()->{
                            hardware.getServos().get("wobbleGoalServo").setPosition(0.25);
                            timerService.registerUniqueTimerEvent(500,()->{
                                hardware.getServos().get("wobbleGoalServo").setPosition(0.47);
                                timerService.registerUniqueTimerEvent(1000,()->{
                                    hardware.getServos().get("wobbleGoalLift").setPosition(0.15);
                                    transition(currentState);
                                });
                            });
                        });
                    });
                });
                break;
            case MOVE_TO_SHOOT_RING_STACK:
                drive.followTrajectoryAsync(moveToShootRingStack);
                break;
            case SHOOT_RING_STACK:
                isShooterEnabled = true;
                handleLift();
                break;
            default:
        }

    }


}
