package org.igutech.teleop.Modules;

import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.apache.commons.math3.util.FastMath;
import org.igutech.auto.roadrunner.SampleMecanumDrive;
import org.igutech.teleop.Module;
import org.igutech.teleop.Teleop;
import org.igutech.utils.ButtonToggle;
import org.igutech.utils.FTCMath;
import org.igutech.utils.PoseStorage;
import org.igutech.utils.control.PIDFController;

public class DriveTrain extends Module {
    public static double kp = 0.03;
    public static double ki = 0;
    public static double kd = 0.000;

    public static double kpr = 0.6;
    public static double kir = 0;
    public static double kdr = 0.00;
    public static int targetX = 0;
    public static int targetY = 0;
    public static double targetTheta = Math.PI;
    private PIDFController xController;
    private PIDFController yController;
    private PIDFController thetaController;
    private ButtonToggle gotoPointToggle;


    private double VX_WEIGHT = 1;
    private double VY_WEIGHT = 1;
    private double OMEGA_WEIGHT = 1;
    private SampleMecanumDrive drive;
    private Pose2d baseVel;
    private Pose2d vel;
    private GamepadService gamepadService;

    public DriveTrain(HardwareMap hwMap) {
        super(1400, "DriveTrain");
        drive = new SampleMecanumDrive(hwMap);
        drive.setPoseEstimate(PoseStorage.currentPose);
        gamepadService = (GamepadService) Teleop.getInstance().getService("GamepadService");

        xController = new PIDFController(kp, ki, kd, 0);
        yController = new PIDFController(kp, ki, kd, 0);
        thetaController = new PIDFController(kpr, kir, kdr, 0);
        gotoPointToggle = new ButtonToggle(1, "dpad_left", () -> {
        }, () -> {
        });
        gotoPointToggle.init();
    }

    @Override
    public void loop() {

        double slowMo = gamepadService.getAnalog(1, "right_trigger");
        double vdMult = FTCMath.lerp(1, 0.4, FastMath.abs(slowMo));
        double vThetaMult = FTCMath.lerp(.8, 0.15, FastMath.abs(slowMo));

        baseVel = new Pose2d(
                -gamepadService.getAnalog(1, "right_stick_y") * vdMult,
                -gamepadService.getAnalog(1, "right_stick_x") * vdMult,
                -gamepadService.getAnalog(1, "left_stick_x") * vThetaMult
        );

        if (baseVel.getX() != 0 || baseVel.getY() != 0 || baseVel.getHeading() != 0) {
            vel = normalize(baseVel);
        } else if (gotoPointToggle.getState()) {
            vel = goToPoint(new Pose2d(targetX, targetY, targetTheta), drive.getPoseEstimate());
        } else {
            vel = new Pose2d(0, 0, 0);
        }

        drive.setDrivePower(vel);
        drive.update();

        Teleop.getInstance().telemetry.addData("Pose", drive.getPoseEstimate());

        gotoPointToggle.loop();
    }

    public Pose2d normalize(Pose2d baseVel) {
        Pose2d temp;
        if (Math.abs(baseVel.getX()) + Math.abs(baseVel.getY()) + Math.abs(baseVel.getHeading()) > 1) {
            // re-normalize the powers according to the weights
            double denom = VX_WEIGHT * Math.abs(baseVel.getX())
                    + VY_WEIGHT * Math.abs(baseVel.getY())
                    + OMEGA_WEIGHT * Math.abs(baseVel.getHeading());
            temp = new Pose2d(
                    VX_WEIGHT * baseVel.getX(),
                    VY_WEIGHT * baseVel.getY(),
                    OMEGA_WEIGHT * baseVel.getHeading()
            ).div(denom);
        } else {
            temp = baseVel;
        }
        return temp;
    }

    public Pose2d goToPoint(Pose2d target, Pose2d current) {

        double heading = current.getHeading();  //0 radian
        double target_heading = target.getHeading(); //PI
        xController.updateSetpoint(target.getX());
        yController.updateSetpoint(target.getY());
        thetaController.updateSetpoint(target_heading);
        if (current.getHeading() <= Math.PI) {
            heading = current.getHeading();
        } else {
            heading = -((2 * Math.PI) - current.getHeading());
        }

        if (Math.abs(target.getHeading() - heading) >= Math.toRadians(180.0)) {
            target_heading = -((2 * Math.PI) - target.getHeading());
        }
        double xPower = xController.update(current.getX());
        double yPower = yController.update(current.getY());
        double rotPower = thetaController.update(heading);

        return convertToPowerCentric(xPower, yPower, rotPower, current.getHeading());

    }

    public Pose2d convertToPowerCentric(double x, double y, double rot, double heading) {
        x = x * Math.cos(heading) - y * Math.sin(heading);
        y = x * Math.sin(heading) + y * Math.cos(heading);
        return (normalize(new Pose2d(x, y, rot)));
    }
}