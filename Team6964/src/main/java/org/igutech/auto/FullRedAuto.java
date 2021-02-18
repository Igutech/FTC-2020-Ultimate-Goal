package org.igutech.auto;

import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.acmerobotics.roadrunner.trajectory.Trajectory;
import com.arcrobotics.ftclib.vision.UGContourRingPipeline;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.igutech.auto.paths.PrepareToShootState;
import org.igutech.auto.roadrunner.SampleMecanumDrive;
import org.igutech.auto.statelib.StateLibrary;
import org.igutech.config.Hardware;
import org.igutech.teleop.Modules.Shooter;
import org.igutech.teleop.Modules.TimerService;
import org.igutech.utils.events.Callback;
import org.openftc.easyopencv.OpenCvCamera;
import org.openftc.easyopencv.OpenCvCameraFactory;
import org.openftc.easyopencv.OpenCvCameraRotation;

import java.util.HashMap;
@Autonomous
public class FullRedAuto extends LinearOpMode {
    private Hardware hardware;
    private boolean isShooterEnabled = false;
    private int currentShooterServoLevel = 1;
    private HashMap<Integer, Double> liftPositions;
    private TimerService timerService;
    private SampleMecanumDrive drive;
    private Shooter shooter;
    private UGContourRingPipeline.Height height;

    @Override
    public void runOpMode() throws InterruptedException {

        hardware = new Hardware(hardwareMap);
        hardware.getServos().get("wobbleGoalServo").setPosition(0.47);
        hardware.getServos().get("shooterServo").setPosition(0.1);
        // hardware.getServos().get("wobbleGoalLift").setPosition(0.15);

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
        StateLibrary transitioner = new StateLibrary();

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
        transitioner.init(new PrepareToShootState(this, startPose));

        camera.stopStreaming();
        camera.closeCameraDevice();
        while (!isStopRequested() && opModeIsActive()) {
            transitioner.loop();
            shooter.loop();
            timerService.loop();
            drive.update();
            telemetry.addData("Pose", drive.getPoseEstimate());
            telemetry.update();
        }

    }


    public void handleLift(int level, boolean justStarted, Callback callback) {
        shooter.setShooterStatus(true);
        currentShooterServoLevel = level;
        hardware.getServos().get("liftServo").setPosition(liftPositions.get(currentShooterServoLevel));
        System.out.println("Lift set to " + liftPositions.get(currentShooterServoLevel));
        if (justStarted) {
            timerService.registerUniqueTimerEvent(1200, "Index", () -> increase(callback));
        } else {
            timerService.registerUniqueTimerEvent(600, "Index", () -> increase(callback));
        }
    }

    public void isAtMaxLevel(Callback callback) {
        currentShooterServoLevel++;
        if (currentShooterServoLevel > 3) {
            isShooterEnabled = false;
        }
        if (isShooterEnabled) {
            timerService.registerUniqueTimerEvent(300, "Index", () -> {
                handleLift(currentShooterServoLevel, false, callback);
            });
        } else {
            currentShooterServoLevel = 0;
            System.out.println("Lift set to " + liftPositions.get(currentShooterServoLevel));
            timerService.registerUniqueTimerEvent(500, "Index", () -> {
                callback.call();
                shooter.setShooterStatus(false);
                System.out.println("ending");
            });
        }
    }

    public void increase(Callback callback) {
        if (currentShooterServoLevel == 0) {
            timerService.registerUniqueTimerEvent(600, "Wobble", () -> {
                System.out.println("testing");
            });
        } else if (currentShooterServoLevel == 1) {
            hardware.getServos().get("shooterServo").setPosition(0.32);
            timerService.registerUniqueTimerEvent(300, "Wobble", () -> {
                hardware.getServos().get("shooterServo").setPosition(0.1);
                isAtMaxLevel(callback);

            });
        } else {
            hardware.getServos().get("shooterServo").setPosition(0.32);
            timerService.registerUniqueTimerEvent(300, "Wobble", () -> {
                hardware.getServos().get("shooterServo").setPosition(0.1);
                isAtMaxLevel(callback);

            });
        }
    }

    public void dropWobbleGoal(Callback callback){
        timerService.registerUniqueTimerEvent(700, "Wobble", () -> {
            hardware.getServos().get("wobbleGoalLift").setPosition(1);
            timerService.registerUniqueTimerEvent(500, "Wobble", () -> {
                hardware.getServos().get("wobbleGoalServo").setPosition(0.25);
                timerService.registerUniqueTimerEvent(300, "Wobble", () -> {
                    callback.call();
                });
            });
        });
    }

    public void grabWobbleGoal(Callback callback){
        hardware.getServos().get("wobbleGoalLift").setPosition(1);
        timerService.registerUniqueTimerEvent(100, "Wobble Servo", () -> {
            hardware.getServos().get("wobbleGoalServo").setPosition(0.25);
            timerService.registerUniqueTimerEvent(250, "Wobble Servo", () -> {
                hardware.getServos().get("wobbleGoalServo").setPosition(0.47);
                timerService.registerUniqueTimerEvent(400, "Wobble Lift", () -> {
                    hardware.getServos().get("wobbleGoalLift").setPosition(0.15);
                    callback.call();
                });
            });
        });
    }
    public Hardware getHardware() {
        return hardware;
    }

    public TimerService getTimerService() {
        return timerService;
    }

    public SampleMecanumDrive getDrive() {
        return drive;
    }

    public Shooter getShooter() {
        return shooter;
    }

    public UGContourRingPipeline.Height getHeight() {
        return height;
    }
}
