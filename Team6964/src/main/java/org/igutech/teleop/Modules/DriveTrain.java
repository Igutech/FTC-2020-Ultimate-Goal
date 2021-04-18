package org.igutech.teleop.Modules;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.apache.commons.math3.util.FastMath;
import org.igutech.auto.roadrunner.SampleMecanumDrive;
import org.igutech.config.Hardware;
import org.igutech.teleop.Module;
import org.igutech.teleop.Teleop;
import org.igutech.utils.FTCMath;

public class DriveTrain extends Module {
    public double VX_WEIGHT = 1;
    public double VY_WEIGHT = 1;
    public double OMEGA_WEIGHT = 1;
    public HardwareMap hardwareMap;
    private Gamepad gamepad1;
    SampleMecanumDrive drive ;
    Pose2d baseVel;
    public DriveTrain(HardwareMap hwMap, Gamepad gamepad1) {
        super(1400, "TestDriveTrain");
        hardwareMap = hwMap;
        this.gamepad1 = gamepad1;
        drive= new SampleMecanumDrive(hwMap);

    }

    @Override
    public void loop() {

        double slowMo = gamepad1.right_trigger;
        double vdMult = FTCMath.lerp(1, 0.4, FastMath.abs(slowMo));
        double vThetaMult = FTCMath.lerp(.8, 0.15, FastMath.abs(slowMo));
        Pose2d vel;
        baseVel= new Pose2d(
                -gamepad1.right_stick_y*vdMult,
                -gamepad1.right_stick_x*vdMult,
                -gamepad1.left_stick_x*vThetaMult
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

        System.out.println(poseEstimate.toString());
        Teleop.getInstance().telemetry.addData("Pose",poseEstimate);


    }
    public double angleWrap ( double angle){
        return (angle + (2 * Math.PI)) % (2 * Math.PI);
    }
}