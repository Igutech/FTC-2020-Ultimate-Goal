package org.igutech.teleop.Modules;

import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.robotcore.hardware.DcMotorEx;

import org.igutech.teleop.Service;
import org.igutech.teleop.Teleop;

import java.util.List;

public class BulkRead extends Service {

    private DcMotorEx frontLeft, frontRight, backLeft, backRight; // Motor Objects
    private long e1, e2, e3, e4; // Encoder Values
    List<LynxModule> allHubs;

    public BulkRead() {
        super("BulkRead");
    }

    @Override
    public void init() {

        frontLeft = (DcMotorEx) Teleop.getInstance().getHardware().getMotors().get("frontleft");  // Configure the robot to use these 4 motor names,
        frontRight =(DcMotorEx) Teleop.getInstance().getHardware().getMotors().get("frontright");  // or change these strings to match your existing Robot Configuration.
        backLeft = (DcMotorEx) Teleop.getInstance().getHardware().getMotors().get("backleft");
        backRight = (DcMotorEx) Teleop.getInstance().getHardware().getMotors().get("backright");
        allHubs= Teleop.getInstance().hardwareMap.getAll(LynxModule.class);
        for (LynxModule module : allHubs) {
            module.setBulkCachingMode(LynxModule.BulkCachingMode.MANUAL);
        }
    }

    @Override
    public void loop() {
        for (LynxModule module : allHubs) {
            module.clearBulkCache();
        }

        e1 = frontLeft.getCurrentPosition();   // Uses 1 bulk-read to obtain ALL the motor data
        e2 = frontRight.getCurrentPosition();   // There is no penalty for doing more `get` operations in this cycle,
        e3 = backLeft.getCurrentPosition();   // but they will return the same data.e4 = m4.getCurrentPosition();
        e4=-1;

    }

    public long getLeftOdoTick() {
        return e1;
    }

    public long getRightOdoTick() {
        return e2;
    }

    public long getStrafeOdoTick() {
        return e3;
    }

    public long getBackRightTicks() {
        return e4;
    }
}