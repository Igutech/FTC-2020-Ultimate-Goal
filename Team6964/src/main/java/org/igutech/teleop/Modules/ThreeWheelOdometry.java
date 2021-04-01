package org.igutech.teleop.Modules;

import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.acmerobotics.roadrunner.geometry.Vector2d;
import com.qualcomm.robotcore.hardware.DcMotorEx;

import org.igutech.auto.roadrunner.SampleMecanumDrive;
import org.igutech.auto.util.Encoder;
import org.igutech.teleop.Module;
import org.igutech.teleop.Teleop;


public class ThreeWheelOdometry extends Module {
    private SampleMecanumDrive drive;

    public ThreeWheelOdometry() {
        super(Integer.MAX_VALUE - 1000, "ThreeWheelOdometry");
    }

    @Override
    public void init() {
        drive = new SampleMecanumDrive(Teleop.getInstance().getHardware().getHardwareMap(),true);
    }

    @Override
    public void start() {
        drive.update();
    }

    @Override
    public void loop() {
        drive.update();

    }

    public Pose2d getPose(){
        return drive.getPoseEstimate();
    }

}
