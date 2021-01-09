package org.igutech.auto;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

import org.igutech.auto.util.AutoUtilManager;
import org.igutech.teleop.Teleop;
import org.igutech.utils.control.PIDFController;

@Autonomous
public class JankAuto extends LinearOpMode {
    AutoUtilManager autoUtilManager;
    public static double frontShooterTargetVelo = -1400;
    public static double backShooterTargetVelo = -1080;
    private PIDFController frontShooterController;
    private PIDFController backShooterController;
    public static double frontShooterkP = 0.007;
    public static double frontShooterkI = 0.001;
    public static double frontShooterkD = 0.0;
    public static double frontShooterkF = 0.;

    public static double backShooterkP = 0.0032;
    public static double backShooterkI = 0.00145;
    public static double backShooterkD = 0.00;
    public static double backShooterkF = 0.0;

    @Override
    public void runOpMode() throws InterruptedException {
        frontShooterController = new PIDFController(frontShooterkP, frontShooterkI, frontShooterkD, frontShooterkF);
        backShooterController = new PIDFController(backShooterkP, backShooterkI, backShooterkD, backShooterkF);
        autoUtilManager = new AutoUtilManager(hardwareMap, "Jank_Auto");
        autoUtilManager.getHardware().getServos().get("liftServo").setPosition(0.78);

        waitForStart();
       // autoUtilManager.getHardware().getServos().get("releaseLiftServo").setPosition(0.2);
        autoUtilManager.getHardware().getMotors().get("frontleft").setPower(0.4);
        autoUtilManager.getHardware().getMotors().get("frontright").setPower(-0.4);
        autoUtilManager.getHardware().getMotors().get("backleft").setPower(0.4);
        autoUtilManager.getHardware().getMotors().get("backright").setPower(-0.4);
        sleep(2100);
        autoUtilManager.getHardware().getMotors().get("frontleft").setPower(0.0);
        autoUtilManager.getHardware().getMotors().get("frontright").setPower(0.0);
        autoUtilManager.getHardware().getMotors().get("backleft").setPower(0.0);
        autoUtilManager.getHardware().getMotors().get("backright").setPower(0.0);

        frontShooterController.updateSetpoint(frontShooterTargetVelo);
        backShooterController.updateSetpoint(backShooterTargetVelo);
        autoUtilManager.getHardware().getMotors().get("frontshooter").setPower(-0.6);
        autoUtilManager.getHardware().getMotors().get("backshooter").setPower(-0.5);

        // Teleop.getInstance().getHardware().getMotors().get("frontshooter").setPower(frontShooterPower);
        // Teleop.getInstance().getHardware().getMotors().get("backshooter").setPower(backShooterPower);
        sleep(1500);
        autoUtilManager.getHardware().getServos().get("liftServo").setPosition(0.65);
        sleep(600);
        autoUtilManager.getHardware().getServos().get("shooterServo").setPosition(0.32);
        sleep(600);
        autoUtilManager.getHardware().getServos().get("shooterServo").setPosition(0.1);
        sleep(600);
        autoUtilManager.getHardware().getServos().get("liftServo").setPosition(0.59);
        sleep(600);
        autoUtilManager.getHardware().getServos().get("shooterServo").setPosition(0.32);
        sleep(600);
        autoUtilManager.getHardware().getServos().get("shooterServo").setPosition(0.1);
        sleep(600);
        autoUtilManager.getHardware().getServos().get("liftServo").setPosition(0.5);
        sleep(600);
        autoUtilManager.getHardware().getServos().get("shooterServo").setPosition(0.32);
        sleep(600);
        autoUtilManager.getHardware().getServos().get("shooterServo").setPosition(0.1);
        sleep(600);

        autoUtilManager.getHardware().getMotors().get("frontleft").setPower(0.4);
        autoUtilManager.getHardware().getMotors().get("frontright").setPower(-0.4);
        autoUtilManager.getHardware().getMotors().get("backleft").setPower(0.4);
        autoUtilManager.getHardware().getMotors().get("backright").setPower(-0.4);
        sleep(750);
        autoUtilManager.getHardware().getMotors().get("frontleft").setPower(0.0);
        autoUtilManager.getHardware().getMotors().get("frontright").setPower(0.0);
        autoUtilManager.getHardware().getMotors().get("backleft").setPower(0.0);
        autoUtilManager.getHardware().getMotors().get("backright").setPower(0.0);


    }
}
