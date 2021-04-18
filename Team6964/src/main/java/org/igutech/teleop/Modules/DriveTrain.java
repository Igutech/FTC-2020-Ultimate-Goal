package org.igutech.teleop.Modules;

import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.apache.commons.math3.util.FastMath;
import org.igutech.auto.roadrunner.SampleMecanumDrive;
import org.igutech.teleop.Module;
import org.igutech.teleop.Teleop;
import org.igutech.utils.FTCMath;

public class DriveTrain extends Module {
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
        gamepadService = (GamepadService) Teleop.getInstance().getService("GamepadService");
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
        if (Math.abs(baseVel.getX()) + Math.abs(baseVel.getY()) + Math.abs(baseVel.getHeading()) > 1) {
            // re-normalize the powers according to the weights
            double denom = VX_WEIGHT * Math.abs(baseVel.getX())
                    + VY_WEIGHT * Math.abs(baseVel.getY())
                    + OMEGA_WEIGHT * Math.abs(baseVel.getHeading());
            vel = new Pose2d(
                    VX_WEIGHT * baseVel.getX(),
                    VY_WEIGHT * baseVel.getY(),
                    OMEGA_WEIGHT * baseVel.getHeading()
            ).div(denom);
        } else {
            vel = baseVel;
        }

        drive.setDrivePower(vel);
        drive.update();

        Pose2d poseEstimate = drive.getPoseEstimate();
        Teleop.getInstance().telemetry.addData("Pose", poseEstimate);


    }

    public double angleWrap(double angle) {
        return (angle + (2 * Math.PI)) % (2 * Math.PI);
    }
}