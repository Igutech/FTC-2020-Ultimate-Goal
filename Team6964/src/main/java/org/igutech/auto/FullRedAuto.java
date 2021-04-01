package org.igutech.auto;

import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.arcrobotics.ftclib.vision.UGContourRingPipeline;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.igutech.auto.paths.PrepareToShootState;
import org.igutech.auto.roadrunner.SampleMecanumDrive;

import dev.raneri.statelib.StateLibrary;

import org.igutech.config.Hardware;
import org.igutech.teleop.Modules.Index;
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

    public void setShooterEnabled(boolean shooterEnabled) {
        isShooterEnabled = shooterEnabled;
    }

    private boolean isShooterEnabled = false;
    private TimerService timerService;
    private SampleMecanumDrive drive;
    private Shooter shooter;
    private UGContourRingPipeline.Height height;
    private Index index;

    @Override
    public void runOpMode() throws InterruptedException {

        hardware = new Hardware(hardwareMap);
        hardware.getServos().get("wobbleGoalServo").setPosition(0.47);
        hardware.getServos().get("shooterServo1").setPosition(0.21);
        hardware.getServos().get("shooterServo2").setPosition(0.46);
        hardware.getServos().get("liftServo").setPosition(0.86);

        timerService = new TimerService();
        index = new Index(hardware,timerService,false);
        shooter = new Shooter(hardware, false,index);
        shooter.frontShooterTargetVelo = -1650;
        drive = new SampleMecanumDrive(hardwareMap,false);
        Pose2d startPose = new Pose2d(-63, -35, Math.toRadians(0));
        drive.setPoseEstimate(startPose);

        shooter.init();
        StateLibrary transitioner = new StateLibrary();
        transitioner.addStateTransitionHandler(event -> {
            if (event.getFinalState() == null) {
                return;
            }
            System.out.println("Exiting " + event.getInitialState().getClass().getName() + " and going into " + event.getFinalState().getClass().getName());
        });
        transitioner.addLoopStartHandler(event -> System.out.println("Current state: " + event.getState().getClass().getName()));
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

        timerService.start();
        if (isStopRequested()) return;
        transitioner.init(new PrepareToShootState(this, startPose));
        System.out.println("Height :" + getHeight());
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
        index.setIndexStatus(true);
        timerService.registerUniqueTimerEvent(1000,"Lift",()->{
            int time = 0;
            for (int i = 0; i < 2; i++) {
                timerService.registerSingleTimerEvent(time, () -> index.setIndexServoStatus(true));
                time += 400;
                timerService.registerSingleTimerEvent(time, () -> index.setIndexServoStatus(false));
                time += 400;
            }
            time+=425;
            timerService.registerSingleTimerEvent(time, () -> index.setIndexServoStatus(true));
            time+=150;
            timerService.registerSingleTimerEvent(time, () -> index.setIndexServoStatus(false));
            time+=150;
            timerService.registerSingleTimerEvent(time, () -> {
                hardware.getServos().get("liftServo").setPosition(0.86);
                index.setIndexStatus(false);
                shooter.setShooterStatus(false);
                callback.call();
            });
        });
    }





    public void dropWobbleGoal(Callback callback) {
        hardware.getServos().get("wobbleGoalLift").setPosition(1);
        timerService.registerUniqueTimerEvent(1500, "Wobble", () -> {
            hardware.getServos().get("wobbleGoalServo").setPosition(0.25);
            timerService.registerUniqueTimerEvent(500, "Wobble", () -> {
                callback.call();
            });
        });
    }

    public void grabWobbleGoal(Callback callback) {
        hardware.getServos().get("wobbleGoalLift").setPosition(1);
        timerService.registerUniqueTimerEvent(50, "Wobble Servo", () -> {
            hardware.getServos().get("wobbleGoalServo").setPosition(0.25);
            timerService.registerUniqueTimerEvent(250, "Wobble Servo", () -> {
                hardware.getServos().get("wobbleGoalServo").setPosition(0.47);
                timerService.registerUniqueTimerEvent(250, "Wobble Lift", () -> {
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
