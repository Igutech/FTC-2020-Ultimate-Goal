package org.igutech.auto;

import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.acmerobotics.roadrunner.trajectory.Trajectory;
import com.arcrobotics.ftclib.vision.UGContourRingPipeline;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.igutech.auto.paths.RedA;
import org.igutech.auto.roadrunner.SampleMecanumDrive;
import org.igutech.auto.vision.UGRectDetector;
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
    private int currentShooterServoLevel = 0;
    private HashMap<Integer, Double> liftPositions;
    private TimerService timerService;
    private SampleMecanumDrive drive;
    private Shooter shooter;
    private Map<State, Trajectory> trajectories;

    @Override
    public void runOpMode() throws InterruptedException {
        hardware = new Hardware(hardwareMap);
        hardware.getServos().get("wobbleGoalServo").setPosition(0.47);

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
        trajectories = RedA.createTrajectory(drive, startPose, () -> transition(currentState), hardware);


         UGContourRingPipeline pipeline = new UGContourRingPipeline(telemetry, true);
        int cameraMonitorViewId = this.hardwareMap.appContext.getResources().getIdentifier("cameraMonitorViewId", "id", hardwareMap.appContext.getPackageName());

        OpenCvCamera camera = OpenCvCameraFactory.getInstance().createWebcam(hardwareMap.get(WebcamName.class, "Webcam 1"), cameraMonitorViewId);

        camera.setPipeline(pipeline);

        UGContourRingPipeline.Config.setCAMERA_WIDTH(320);

        UGContourRingPipeline.Config.setHORIZON(100);

        camera.openCameraDeviceAsync(() -> camera.startStreaming(320, 240, OpenCvCameraRotation.UPRIGHT));
        while (!opModeIsActive() && !isStopRequested()) {
            UGContourRingPipeline.Height height =  pipeline.getHeight();
            telemetry.addData("Status: ", "Ready");
            telemetry.addData("Stack: ", height);
            telemetry.addData("Pose: ", drive.getPoseEstimate());
            telemetry.update();

        }

        //waitForStart();
        timerService.start();
        if (isStopRequested()) return;
        drive.followTrajectoryAsync(trajectories.get(currentState));

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
        switch (currentState) {
            case PREPARE_TO_SHOOT:
                //drive.followTrajectoryAsync(prepareToShoot);
                break;
            case SHOOTING_PRELOAD_RINGS:
                isShooterEnabled = true;
                handleLift();
                break;
            case MOVE_TO_DROP_FIRST_WOBBLE_GOAL:
                hardware.getServos().get("releaseLiftServo").setPosition(0.2);
                drive.followTrajectoryAsync(trajectories.get(currentState));
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
            case GRAB_SECOND_WOBBLE_GOAL:
                timerService.registerUniqueTimerEvent(500, "Wobble", () -> {
                    hardware.getServos().get("wobbleGoalLift").setPosition(1);
                    timerService.registerUniqueTimerEvent(500, "Wobble", () -> {
                        hardware.getServos().get("wobbleGoalServo").setPosition(0.25);
                        timerService.registerUniqueTimerEvent(500, "Wobble", () -> {
                            hardware.getServos().get("wobbleGoalServo").setPosition(0.47);
                            timerService.registerUniqueTimerEvent(1000, "Wobble", () -> {
                                hardware.getServos().get("wobbleGoalLift").setPosition(0.15);
                                transition(currentState);
                            });
                        });
                    });
                });
                break;
            case MOVE_TO_SHOOT_RING_STACK:
                drive.followTrajectoryAsync(trajectories.get(currentState));
                break;
            case SHOOT_RING_STACK:
                isShooterEnabled = true;
                handleLift();
                break;
            default:
        }

    }

    public void handleLift() {

        shooter.setEnableShooter(true);
        System.out.println("Running Indexer");
        if (currentShooterServoLevel > 3) {
            currentShooterServoLevel = 0;
        }
        timerService.registerUniqueTimerEvent(600, "Index", () -> {
            hardware.getServos().get("liftServo").setPosition(liftPositions.get(currentShooterServoLevel));
            timerService.registerUniqueTimerEvent(600, "Index", () -> {
                hardware.getServos().get("shooterServo").setPosition(1.0);
                timerService.registerUniqueTimerEvent(150, "Index", () -> {
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
                        timerService.registerUniqueTimerEvent(2000, "Index", () -> {
                            shooter.setEnableShooter(false);
                            transition(currentState);
                        });
                    }
                });
            });
        });
    }

}
