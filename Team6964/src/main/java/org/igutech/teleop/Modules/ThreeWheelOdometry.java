package org.igutech.teleop.Modules;

import com.acmerobotics.roadrunner.geometry.Pose2d;

import org.igutech.auto.roadrunner.StandardTrackingWheelLocalizer;
import org.igutech.teleop.Module;
import org.igutech.teleop.Teleop;


public class ThreeWheelOdometry extends Module {
    private Pose2d myPose = new Pose2d(0, 0, 0);
    private StandardTrackingWheelLocalizer localizer;

    public ThreeWheelOdometry() {
        super(Integer.MAX_VALUE - 1000, "ThreeWheelOdometry");
    }

    @Override
    public void init() {
        localizer = new StandardTrackingWheelLocalizer(Teleop.getInstance().getHardware().getHardwareMap());
    }

    @Override
    public void start() {
        localizer.update();
    }

    @Override
    public void loop() {
        localizer.update();
        Teleop.getInstance().telemetry.addData("x",localizer.getPoseEstimate().getX());
        Teleop.getInstance().telemetry.addData("y",localizer.getPoseEstimate().getY());
        Teleop.getInstance().telemetry.addData("heading",localizer.getPoseEstimate().getHeading());
    }

    public Pose2d getPose(){
        return localizer.getPoseEstimate();
    }

}
