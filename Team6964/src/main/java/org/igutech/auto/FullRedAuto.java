package org.igutech.auto;

import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.acmerobotics.roadrunner.geometry.Vector2d;
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
                .addDisplacementMarker(() -> {
                    currentState = State.SHOOTING_PRELOAD_RINGS;
                })
                .build();

        Trajectory intakeRingStack = drive.trajectoryBuilder(prepareToShoot.end(), new DriveConstraints(30.0, 30.0, 0.0, Math.toRadians(180), Math.toRadians(180), 0.0))
                .addDisplacementMarker(() -> {
                    hardware.getMotors().get("intake").setPower(1.0);
                })
                .splineTo(new Vector2d(-25.0, -35.0), Math.toRadians(0.0))
                .addDisplacementMarker(() -> {
                    hardware.getMotors().get("intake").setPower(0.0);
                    currentState = State.DROP_FIRST_WOBBLE_GOAL;
                })
                .build();

        Trajectory dropFirstWobbleGoal = drive.trajectoryBuilder(intakeRingStack.end())
                .splineTo(new Vector2d(10.0, -45.0), Math.toRadians(0.0))
                .addDisplacementMarker(() -> {
                    currentState = State.MOVE_TO_SHOOT_RING_STACK;
                })
                .build();
        Trajectory moveToShootRingStack = drive.trajectoryBuilder(dropFirstWobbleGoal.end(), true)
                .splineToConstantHeading(new Vector2d(-5.0, -40.0), Math.toRadians(0.0))
                .addDisplacementMarker(() -> {
                    currentState = State.SHOOTING_RING_STACK;
                })
                .build();
        Trajectory grabSecondGoal = drive.trajectoryBuilder(moveToShootRingStack.end())
                .splineToConstantHeading(new Vector2d(-55.0, -40.0), Math.toRadians(0.0))
                .addDisplacementMarker(() -> {
                    currentState = State.DROP_SECOND_GOAL;
                })
                .build();
        Trajectory dropSecondWobbleGoal = drive.trajectoryBuilder(grabSecondGoal.end())
                .splineTo(new Vector2d(10.0, -45.0), Math.toRadians(0.0))
                .addDisplacementMarker(() -> {
                    currentState = State.OFF;
                })
                .build();

        telemetry.addData("Status: ", "Ready");
        telemetry.update();
        waitForStart();
        timerService.start();
        if (isStopRequested()) return;

        while (!isStopRequested() && opModeIsActive()) {
            switch (currentState) {
                case PREPARE_TO_SHOOT:
                    drive.followTrajectoryAsync(prepareToShoot);
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

        }

    }

    public void handleLift() {
        if (currentShooterServoLevel > 3) {
            currentShooterServoLevel = 0;
        }
        timerService.registerSingleTimerEvent(600, () -> {
            hardware.getServos().get("liftServo").setPosition(liftPositions.get(currentShooterServoLevel));
            timerService.registerSingleTimerEvent(600, () -> {
                hardware.getServos().get("shooterServo").setPosition(1.0);
                timerService.registerSingleTimerEvent(150, () -> {
                    hardware.getServos().get("shooterServo").setPosition(0.0);
                    currentShooterServoLevel++;
                    if (currentShooterServoLevel > 3) {
                        isShooterEnabled = false;
                    }
                    if (isShooterEnabled) {
                        handleLift();
                    }else{
                        transition();
                    }
                });
            });
        });
    }


    public void transition(){
        if(currentState==State.SHOOTING_PRELOAD_RINGS){
            currentState=State.INTAKE_RING_STACK;
        }else if(currentState==State.SHOOTING_RING_STACK){
            currentState = State.MOVE_TO_GRAB_SECOND_GOAL;
        }
    }


}
