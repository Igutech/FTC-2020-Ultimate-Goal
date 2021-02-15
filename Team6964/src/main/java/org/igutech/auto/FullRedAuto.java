package org.igutech.auto;

import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.acmerobotics.roadrunner.trajectory.Trajectory;
import com.arcrobotics.ftclib.vision.UGContourRingPipeline;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.igutech.auto.paths.RedB;
import org.igutech.auto.paths.RedA;
import org.igutech.auto.roadrunner.SampleMecanumDrive;
import org.igutech.config.Hardware;
import org.igutech.teleop.Modules.Shooter;
import org.igutech.teleop.Modules.TimerService;
import org.openftc.easyopencv.OpenCvCamera;
import org.openftc.easyopencv.OpenCvCameraFactory;
import org.openftc.easyopencv.OpenCvCameraRotation;

import java.util.HashMap;
import java.util.Map;

@Autonomous
public class FullRedAuto extends LinearOpMode {
    private State currentState = State.PREPARE_TO_SHOOT;
    private Hardware hardware;
    private boolean isShooterEnabled = false;
    private int currentShooterServoLevel = 1;
    private HashMap<Integer, Double> liftPositions;
    private TimerService timerService;
    private SampleMecanumDrive drive;
    private Shooter shooter;
    private Map<State, Trajectory> trajectories;
    private UGContourRingPipeline.Height height;

    @Override
    public void runOpMode() throws InterruptedException {
        hardware = new Hardware(hardwareMap);
        hardware.getServos().get("wobbleGoalServo").setPosition(0.47);
        hardware.getServos().get("shooterServo").setPosition(0.1);
        hardware.getServos().get("wobbleGoalLift").setPosition(0.15);

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
        Map<State, Trajectory> trajectoryA = RedA.createTrajectory(drive, startPose, () -> transition(currentState), hardware);
        Map<State, Trajectory> trajectoryB = RedB.createTrajectory(drive, startPose, () -> transition(currentState), hardware);

        UGContourRingPipeline pipeline = new UGContourRingPipeline(telemetry, true);
        int cameraMonitorViewId = this.hardwareMap.appContext.getResources().getIdentifier("cameraMonitorViewId", "id", hardwareMap.appContext.getPackageName());

        OpenCvCamera camera = OpenCvCameraFactory.getInstance().createWebcam(hardwareMap.get(WebcamName.class, "Webcam 1"), cameraMonitorViewId);

        camera.setPipeline(pipeline);

        UGContourRingPipeline.Config.setCAMERA_WIDTH(320);

        UGContourRingPipeline.Config.setHORIZON(100);

        camera.openCameraDeviceAsync(() -> camera.startStreaming(320, 240, OpenCvCameraRotation.UPRIGHT));
        while (!opModeIsActive() && !isStopRequested()) {
            height = pipeline.getHeight();
            telemetry.addData("Status: ", "Ready");
            telemetry.addData("Stack: ", height);
            telemetry.addData("Pose: ", drive.getPoseEstimate());
            telemetry.update();

        }

        //waitForStart();
        timerService.start();
        if (isStopRequested()) return;
        if (height == UGContourRingPipeline.Height.ONE) {
            trajectories = trajectoryB;
        } else if (height == UGContourRingPipeline.Height.ZERO) {
            trajectories = trajectoryA;
        } else {

        }

        drive.followTrajectoryAsync(trajectories.get(currentState));
        camera.stopStreaming();
        camera.closeCameraDevice();
        while (!isStopRequested() && opModeIsActive()) {
            shooter.loop();
            timerService.loop();
            drive.update();
            telemetry.addData("Pose", drive.getPoseEstimate());
            telemetry.addData("State", currentState);
            telemetry.update();
        }

    }


    public void transition(State state) {
        System.out.println("Transitioning to state " + state.getNextState() + " from " + currentState);
        currentState = state.getNextState();
        if (height == UGContourRingPipeline.Height.ONE) {
            switch (currentState) {
                case PREPARE_TO_SHOOT:
                    //drive.followTrajectoryAsync(prepareToShoot);
                    break;
                case SHOOTING_PRELOAD_RINGS:
                    isShooterEnabled = true;
                    handleLift(1);
                    break;
                case MOVE_TO_DROP_FIRST_WOBBLE_GOAL:
                    drive.followTrajectoryAsync(trajectories.get(currentState));
                    break;
                case DROP_FIRST_WOBBLE_GOAL:
                    timerService.registerUniqueTimerEvent(700, "Wobble", () -> {
                        hardware.getServos().get("wobbleGoalLift").setPosition(1);
                        timerService.registerUniqueTimerEvent(500, "Wobble", () -> {
                            hardware.getServos().get("wobbleGoalServo").setPosition(0.25);
                            timerService.registerUniqueTimerEvent(500, "Wobble", () -> {
                                drive.followTrajectory((trajectories.get(currentState)));
                            });
                        });
                    });
                    break;
                case MOVE_TO_TO_RING_STACK:
                    drive.followTrajectoryAsync(trajectories.get(currentState));
                    break;
                case INTAKE_RING_STACK:
                    drive.followTrajectoryAsync(trajectories.get(currentState));
                    break;
                case MOVE_TO_GRAB_SECOND_GOAL:
                    drive.followTrajectoryAsync(trajectories.get(currentState));
                    break;
                case MOVE_TO_GRAB_SECOND_GOAL_CONTINUED:
                    drive.followTrajectoryAsync(trajectories.get(currentState));
                    break;
                case MOVE_TO_GRAB_SECOND_GOAL_CONTINUED2:
                    drive.followTrajectoryAsync(trajectories.get(currentState));
                    break;
                case GRAB_SECOND_WOBBLE_GOAL:
                    hardware.getServos().get("wobbleGoalLift").setPosition(1);
                    timerService.registerUniqueTimerEvent(100, "Wobble Servo", () -> {
                        hardware.getServos().get("wobbleGoalServo").setPosition(0.25);
                        timerService.registerUniqueTimerEvent(250, "Wobble Servo", () -> {
                            hardware.getServos().get("wobbleGoalServo").setPosition(0.47);
                            timerService.registerUniqueTimerEvent(400, "Wobble Lift", () -> {
                                hardware.getServos().get("wobbleGoalLift").setPosition(0.15);
                                transition(currentState);
                            });
                        });
                    });
                    break;
                case MOVE_TO_SHOOT_RING_STACK:
                    drive.followTrajectoryAsync(trajectories.get(currentState));
                    break;
                case SHOOT_RING_STACK:
                    isShooterEnabled = true;
                    handleLift(3);
                    break;
                case MOVE_TO_DROP_SECOND_WOBBLE_GOAL:
                    drive.followTrajectoryAsync(trajectories.get(currentState));
                    break;
                case DROP_SECOND_WOBBLE_GOAL:
                    timerService.registerUniqueTimerEvent(700, "Wobble", () -> {
                        hardware.getServos().get("wobbleGoalLift").setPosition(1);
                        timerService.registerUniqueTimerEvent(500, "Wobble", () -> {
                            hardware.getServos().get("wobbleGoalServo").setPosition(0.25);
                            timerService.registerUniqueTimerEvent(300, "Wobble", () -> {
                                transition(currentState);
                            });
                        });
                    });
                    break;
                case PARK:
                    drive.followTrajectoryAsync(trajectories.get(currentState));
                    break;

                default:
            }
        } else if (UGContourRingPipeline.Height.ZERO == height) {
            switch (currentState) {
                case PREPARE_TO_SHOOT:
                    break;
                case SHOOTING_PRELOAD_RINGS:
                    isShooterEnabled = true;
                    handleLift(1);
                    break;
                case MOVE_TO_DROP_FIRST_WOBBLE_GOAL:
                    drive.followTrajectoryAsync(trajectories.get(currentState));
                    break;
                case DROP_FIRST_WOBBLE_GOAL:
                    timerService.registerUniqueTimerEvent(700, "Wobble", () -> {
                        hardware.getServos().get("wobbleGoalLift").setPosition(1);
                        timerService.registerUniqueTimerEvent(500, "Wobble", () -> {
                            hardware.getServos().get("wobbleGoalServo").setPosition(0.25);
                            timerService.registerUniqueTimerEvent(500, "Wobble", () -> {
                                drive.followTrajectory((trajectories.get(currentState)));
                            });
                        });
                    });
                    break;
                case MOVE_TO_TO_RING_STACK:
                    transition(currentState);
                    break;
                case INTAKE_RING_STACK:
                    transition(currentState);
                    break;
                case MOVE_TO_GRAB_SECOND_GOAL:
                    drive.followTrajectoryAsync(trajectories.get(currentState));
                    break;
                case MOVE_TO_GRAB_SECOND_GOAL_CONTINUED:
                    drive.followTrajectoryAsync(trajectories.get(currentState));
                    break;
                case MOVE_TO_GRAB_SECOND_GOAL_CONTINUED2:
                    transition(currentState);
                    break;
                case GRAB_SECOND_WOBBLE_GOAL:
                    hardware.getServos().get("wobbleGoalLift").setPosition(1);
                    timerService.registerUniqueTimerEvent(100, "Wobble Servo", () -> {
                        hardware.getServos().get("wobbleGoalServo").setPosition(0.25);
                        timerService.registerUniqueTimerEvent(250, "Wobble Servo", () -> {
                            hardware.getServos().get("wobbleGoalServo").setPosition(0.47);
                            timerService.registerUniqueTimerEvent(400, "Wobble Lift", () -> {
                                hardware.getServos().get("wobbleGoalLift").setPosition(0.15);
                                transition(currentState);
                            });
                        });
                    });
                    break;
                case MOVE_TO_SHOOT_RING_STACK:
                    transition(currentState);
                    break;
                case SHOOT_RING_STACK:
                    transition(currentState);
                    break;
                case MOVE_TO_DROP_SECOND_WOBBLE_GOAL:
                    drive.followTrajectoryAsync(trajectories.get(currentState));
                    break;
                case DROP_SECOND_WOBBLE_GOAL:
                    timerService.registerUniqueTimerEvent(700, "Wobble", () -> {
                        hardware.getServos().get("wobbleGoalLift").setPosition(1);
                        timerService.registerUniqueTimerEvent(500, "Wobble", () -> {
                            hardware.getServos().get("wobbleGoalServo").setPosition(0.25);
                            timerService.registerUniqueTimerEvent(300, "Wobble", () -> {
                                transition(currentState);
                            });
                        });
                    });
                    break;
                case PARK:
                    drive.followTrajectoryAsync(trajectories.get(currentState));
                    break;

                default:
            }
        }
    }

    public void handleLift(int shooterLevel) {

        shooter.setEnableShooter(true);
        currentShooterServoLevel = shooterLevel;
        System.out.println("Running Indexer");
        hardware.getServos().get("liftServo").setPosition(liftPositions.get(currentShooterServoLevel));
        timerService.registerUniqueTimerEvent(1200, "Index", () -> {
            hardware.getServos().get("shooterServo").setPosition(1.0);
            timerService.registerUniqueTimerEvent(600, "Index", () -> {
                hardware.getServos().get("shooterServo").setPosition(0.0);
                currentShooterServoLevel++;
                if (currentShooterServoLevel > 3) {
                    isShooterEnabled = false;
                }
                if (isShooterEnabled) {
                    handleLift(currentShooterServoLevel);
                } else {
                    currentShooterServoLevel = 0;
                    hardware.getServos().get("liftServo").setPosition(liftPositions.get(currentShooterServoLevel));
                    timerService.registerUniqueTimerEvent(500, "Index", () -> {
                        shooter.setEnableShooter(false);
                        transition(currentState);
                    });
                }
            });
        });
    }


}
