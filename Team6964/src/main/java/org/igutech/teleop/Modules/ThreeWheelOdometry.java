package org.igutech.teleop.Modules;

import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.acmerobotics.roadrunner.geometry.Vector2d;
import com.qualcomm.robotcore.hardware.DcMotorEx;

import org.igutech.auto.util.Encoder;
import org.igutech.teleop.Module;
import org.igutech.teleop.Teleop;


public class ThreeWheelOdometry extends Module {
    private Pose2d myPose = new Pose2d(0, 0, 0);
    double TRACK_WIDTH = 14.5948;
    double prevstrafe = 0;
    double prevvert = 0;
    double prevheading;
    double STRAFE_WIDTH = -6.94; //center of the robot to the perpendicular wheel
    private Encoder leftEncoder, rightEncoder, strafeEncoder;

    public ThreeWheelOdometry() {
        super(Integer.MAX_VALUE - 1000, "ThreeWheelOdometry");
    }

    @Override
    public void init() {
        leftEncoder = new Encoder((DcMotorEx) Teleop.getInstance().getHardware().getMotors().get("frontleft"));
        rightEncoder = new Encoder((DcMotorEx) Teleop.getInstance().getHardware().getMotors().get("backleft"));
        strafeEncoder = new Encoder((DcMotorEx) Teleop.getInstance().getHardware().getMotors().get("frontright"));
        leftEncoder.setDirection(Encoder.Direction.REVERSE);
    }

    @Override
    public void start() {
    }

    @Override
    public void loop() {
        double vert = ((leftEncoder.getCurrentPosition() + rightEncoder.getCurrentPosition()) / 2.0);
        double dvert = vert - prevvert;
        double dtheta = getHeading() - prevheading;
        double dstrafe = ((strafeEncoder.getCurrentPosition()) - prevstrafe) - (STRAFE_WIDTH * dtheta);

        prevstrafe = strafeEncoder.getCurrentPosition();
        prevvert = vert;

        Vector2d offset = constantVeloTrack(dstrafe, prevheading, dvert, dtheta);
        myPose = new Pose2d(myPose.getX() + offset.getX(), myPose.getY() + offset.getY(), getHeading());

        prevheading = getHeading();
        Teleop.getInstance().telemetry.addData("Current pose",myPose.toString());

    }

    public Vector2d constantVeloTrack(double dstrafe, double prevheading, double dvert, double dtheta) {
        double sinterm = 0;
        double costerm = 0;

        if (dtheta == 0) {
            sinterm = 1.0 - dtheta * dtheta / 6.0;
            costerm = dtheta / 2.0;
        } else {
            sinterm = Math.sin(dtheta) / dtheta;
            costerm = (1 - Math.cos(dtheta)) / dtheta;
        }

        return new Vector2d((dstrafe * sinterm) + (dvert * -costerm), (dstrafe * costerm) + (dvert * sinterm)).rotated(prevheading);
    }

    public double getHeading() {
        return (angleWrap((rightEncoder.getCurrentPosition() - leftEncoder.getCurrentPosition()) / TRACK_WIDTH));
    }


    public double angleWrap(double angle) {
        return (angle + (2 * Math.PI)) % (2 * Math.PI);
    }
    public Pose2d getPose(){
        return myPose;
    }
}
