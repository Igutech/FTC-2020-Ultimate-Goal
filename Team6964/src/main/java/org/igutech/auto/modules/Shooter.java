package org.igutech.auto.modules;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.igutech.utils.control.PIDFController;

public class Shooter {
    private PIDFController frontShooterController;
    private PIDFController backShooterController;
    private boolean status = false;
    private boolean wasPidRunning;
    private HardwareMap hardwareMap;
    private DcMotor frontShooter;
    private DcMotor backShooter;

    public Shooter(HardwareMap hardwareMap) {
        this.hardwareMap = hardwareMap;
    }

    public void init() {
        frontShooterController = new PIDFController(0.007, 0.001, 0, 0);
        backShooterController = new PIDFController(0.0032, 0.00145, 0, 0);
        frontShooterController.init();
        backShooterController.init();
        frontShooter = hardwareMap.dcMotor.get("frontshooter");
        backShooter = hardwareMap.dcMotor.get("backshooter");
    }

    public void loop() {
        if (status) {
            if (!wasPidRunning) {
                frontShooterController.init();
                backShooterController.init();
            }
            frontShooterController.updateSetpoint(-1400);
            backShooterController.updateSetpoint(-1080);
            double frontShooterPower = frontShooterController.update(frontShooter.getCurrentPosition());
            double backShooterPower = backShooterController.update(backShooter.getCurrentPosition());

            frontShooter.setPower(frontShooterPower);
            backShooter.setPower(backShooterPower);
        } else {
            wasPidRunning = false;
        }
    }

    public void setStatus(boolean currentStatus) {
        this.status = currentStatus;
        if (currentStatus) {
            wasPidRunning = true;
        } else {
            wasPidRunning = false;
        }
    }
}
