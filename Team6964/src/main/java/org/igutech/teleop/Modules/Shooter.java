package org.igutech.teleop.Modules;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.config.Config;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.igutech.teleop.Module;
import org.igutech.teleop.Teleop;
import org.igutech.utils.ButtonToggle;
import org.igutech.utils.control.PIDFController;

import java.util.logging.Logger;

//back motor=0.5 power and front =0.6
//powershot back=-6 front = 0.55
@Config
public class Shooter extends Module {
    FtcDashboard dashboard = FtcDashboard.getInstance();
    Telemetry dashboardTelemetry = dashboard.getTelemetry();
    private GamepadService gamepadService;
    private BulkRead bulkRead;

    public static double frontShooterkP = 0.007;
    public static double frontShooterkI = 0.001;
    public static double frontShooterkD = 0.0;
    public static double frontShooterkF = 0.;

    public static double backShooterkP = 0.0032;
    public static double backShooterkI = 0.00145;
    public static double backShooterkD = 0.00;
    public static double backShooterkF = 0.0;

    public static double frontShooterTargetVelo = -1400;
    public static double backShooterTargetVelo = -1080;
    public static double frontShooterPowershotVelo = -1200;
    public static double backShooterPowershotVelo = -1100;
    private PIDFController frontShooterController;
    private PIDFController backShooterController;
    private boolean veloControlActive = false;
    private ButtonToggle veloToggle;
    private ButtonToggle powershotToggle;
    public static double frontPower = 0;
    public static double backPower = 0;

    private boolean wasPidRunning;

    public Shooter() {
        super(500, "Shooter");
    }

    @Override
    public void init() {
        gamepadService = (GamepadService) Teleop.getInstance().getService("GamepadService");
        bulkRead = (BulkRead) Teleop.getInstance().getService("BulkRead");
        frontShooterController = new PIDFController(frontShooterkP, frontShooterkI, frontShooterkD, frontShooterkF);
        backShooterController = new PIDFController(backShooterkP, backShooterkI, backShooterkD, backShooterkF);
        veloToggle = new ButtonToggle(1, "right_bumper", () -> {
            frontShooterController.init();
            backShooterController.init();
        }, () -> {
            frontShooterController.init();
            backShooterController.init();
        });
        veloToggle.init();
        powershotToggle = new ButtonToggle(1, "b", () -> {
            frontShooterController.init();
            backShooterController.init();
        }, () -> {
            frontShooterController.init();
            backShooterController.init();
        });
        powershotToggle.init();

    }

    @Override
    public void loop() {

        veloToggle.loop();
        powershotToggle.loop();
        double manualPower = gamepadService.getAnalog(2, "right_trigger");
        if (Math.abs(manualPower) != 0.0) {
            wasPidRunning = false;
            Teleop.getInstance().getHardware().getMotors().get("frontshooter").setPower(-manualPower);
            Teleop.getInstance().getHardware().getMotors().get("backshooter").setPower(-manualPower);
        } else if (veloToggle.getState()) {
            if (powershotToggle.getState()) {
                powershotToggle.setState(false);
            }
            if (!wasPidRunning) {
                frontShooterController.init();
                backShooterController.init();
            }
            frontShooterController.updateSetpoint(frontShooterTargetVelo);
            backShooterController.updateSetpoint(backShooterTargetVelo);
            double frontShooterPower = frontShooterController.update(bulkRead.getFrontShooterVelo());
            double backShooterPower = backShooterController.update(bulkRead.getBackShooterVelo());

            Teleop.getInstance().getHardware().getMotors().get("frontshooter").setPower(frontShooterPower);
            Teleop.getInstance().getHardware().getMotors().get("backshooter").setPower(backShooterPower);

            dashboardTelemetry.addData("frontShooterPower", frontShooterPower);
            dashboardTelemetry.addData("backShooterPower", backShooterPower);
            Logger.getLogger("SHOOTER").info("IN SHOOTER");
        } else if (powershotToggle.getState()) {

            if (!wasPidRunning) {
                frontShooterController.init();
                backShooterController.init();
            }
            frontShooterController.updateSetpoint(frontShooterPowershotVelo);
            backShooterController.updateSetpoint(backShooterPowershotVelo);
            double frontShooterPower = frontShooterController.update(bulkRead.getFrontShooterVelo());
            double backShooterPower = backShooterController.update(bulkRead.getBackShooterVelo());

            Teleop.getInstance().getHardware().getMotors().get("frontshooter").setPower(frontShooterPower);
            Teleop.getInstance().getHardware().getMotors().get("backshooter").setPower(backShooterPower);

            dashboardTelemetry.addData("frontShooterPower", frontShooterPower);
        } else {
            wasPidRunning = false;
            Teleop.getInstance().getHardware().getMotors().get("frontshooter").setPower(0.0);
            Teleop.getInstance().getHardware().getMotors().get("backshooter").setPower(0.0);
        }
        // Teleop.getInstance().getHardware().getMotors().get("frontshooter").setPower(frontPower);
        // Teleop.getInstance().getHardware().getMotors().get("backshooter").setPower(backPower);
        dashboardTelemetry.addData("manualPower", manualPower);
        dashboardTelemetry.addData("Target frontShooter Velo", frontShooterTargetVelo);
        dashboardTelemetry.addData("Target backShooter Velo", backShooterTargetVelo);
        dashboardTelemetry.addData("Front Shooter Velo", bulkRead.getFrontShooterVelo());
        dashboardTelemetry.addData("Back Shooter Velo", bulkRead.getBackShooterVelo());
        dashboardTelemetry.addData("PID Active ", veloControlActive);
        dashboardTelemetry.addData("Front P ", frontShooterController.getkP());
        dashboardTelemetry.addData("Back P ", backShooterController.getkP());
        dashboardTelemetry.update();

        if (veloToggle.getState() || powershotToggle.getState()) {
            wasPidRunning = true;
        }

    }

    @Override
    public void stop() {

    }
}
