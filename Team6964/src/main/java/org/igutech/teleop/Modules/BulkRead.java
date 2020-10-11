package org.igutech.teleop.Modules;

import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.robotcore.hardware.DcMotorEx;

import org.igutech.teleop.Service;
import org.igutech.teleop.Teleop;

import java.util.List;

public class BulkRead extends Service {

    private DcMotorEx frontLeft, frontRight, backLeft, backRight,shooterLeft,shooterRight; // Motor Objects
    private long frontLeftTick, frontRightTick, backLeftTick, backRightTick,shooterLeftTick,shooterRightTick; // Encoder Values


    private double frontLeftVelo, frontRightVelo, backLeftVelo, backRightVelo,shooterLeftVelo,shooterRightVelo;
    List<LynxModule> allHubs;

    public BulkRead() {
        super("BulkRead");
    }

    @Override
    public void init() {

        frontLeft = (DcMotorEx) Teleop.getInstance().getHardware().getMotors().get("frontleft");  // Configure the robot to use these 4 motor names,
        frontRight = (DcMotorEx) Teleop.getInstance().getHardware().getMotors().get("frontright");  // or change these strings to match your existing Robot Configuration.
        backLeft = (DcMotorEx) Teleop.getInstance().getHardware().getMotors().get("backleft");
        backRight = (DcMotorEx) Teleop.getInstance().getHardware().getMotors().get("backright");
        //shooterLeft=(DcMotorEx)Teleop.getInstance().getHardware().getMotors().get("shooterLeft");
        //shooterRight=(DcMotorEx)Teleop.getInstance().getHardware().getMotors().get("shooterRight");
        allHubs = Teleop.getInstance().hardwareMap.getAll(LynxModule.class);
        for (LynxModule module : allHubs) {
            module.setBulkCachingMode(LynxModule.BulkCachingMode.MANUAL);
        }
    }

    @Override
    public void loop() {
        for (LynxModule module : allHubs) {
            module.clearBulkCache();
        }

        frontLeftTick = frontLeft.getCurrentPosition();   // Uses 1 bulk-read to obtain ALL the motor data
        frontRightTick = frontRight.getCurrentPosition();   // There is no penalty for doing more `get` operations in this cycle,
        backLeftTick = backLeft.getCurrentPosition();   // but they will return the same data.e4 = m4.getCurrentPosition();
        backRightTick = backRight.getCurrentPosition();
        shooterLeftTick=shooterLeft.getCurrentPosition();
        shooterRightTick=shooterRight.getCurrentPosition();

        frontLeftVelo = frontLeft.getVelocity();
        frontRightVelo = frontRight.getVelocity();
        backLeftVelo = backLeft.getVelocity();
        backRightVelo = backRight.getVelocity();
        shooterLeftVelo=shooterLeft.getVelocity();
        shooterRightVelo=shooterRight.getVelocity();

    }

    public long getShooterLeftTick() {
        return shooterLeftTick;
    }

    public long getShooterRightTick() {
        return shooterRightTick;
    }

    public double getShooterLeftVelo() {
        return shooterLeftVelo;
    }

    public double getShooterRightVelo() {
        return shooterRightVelo;
    }

    public long getLeftOdoTick() {
        return frontLeftTick;
    }

    public long getRightOdoTick() {
        return frontRightTick;
    }

    public long getStrafeOdoTick() {
        return backLeftTick;
    }

    public long getBackRightTicks() {
        return backRightTick;
    }

    public double getFrontLeftVelo() {
        return frontLeftVelo;
    }

    public double getFrontRightVelo() {
        return frontRightVelo;
    }

    public double getBackLeftVelo() {
        return backLeftVelo;
    }

    public double getBackRightVelo() {
        return backRightVelo;
    }
}