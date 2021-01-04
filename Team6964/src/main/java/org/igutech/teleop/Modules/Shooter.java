package org.igutech.teleop.Modules;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.config.Config;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.igutech.teleop.Module;
import org.igutech.teleop.Teleop;
import org.igutech.utils.ButtonToggle;
import org.igutech.utils.control.PIDFController;

//back motor=0.5 power and front =0.6
//powershot back=-6 front = 0.55
@Config
public class Shooter extends Module {
    FtcDashboard dashboard = FtcDashboard.getInstance();
    Telemetry dashboardTelemetry = dashboard.getTelemetry();
    private GamepadService gamepadService;
    private BulkRead bulkRead;

    public static double frontShooterkP = 0.013;
    public static double frontShooterkI = 0.00;
    public static double frontShooterkD = 0.0;
    public static double frontShooterkF = 0.0002;

    public static double backShooterkP = 0.00013;
    public static double backShooterkI = 0.00;
    public static double backShooterkD = 0.00;
    public static double backShooterkF = 0.0;

    public static double frontShooterTargetVelo = -1370;
    public static double backShooterTargetVelo = -400;
    public static double frontShooterPowershotVelo = -1220;
    private PIDFController frontShooterController;
    private PIDFController backShooterController;
    private boolean veloControlActive = false;
    private ButtonToggle veloToggle;
    private ButtonToggle powershotToggle;
    public static double frontPower=-0.5;
    public static double backPower=-0.4;

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
        }, () -> {
        });
        veloToggle.init();
        powershotToggle = new ButtonToggle(1,"b",()->{
        },()->{});
        powershotToggle.init();

    }

    @Override
    public void loop() {

        veloToggle.loop();
        powershotToggle.loop();
        veloControlActive = veloToggle.getState();
        double manualPower = gamepadService.getAnalog(2, "right_trigger");
        if (Math.abs(manualPower) != 0.0) {
            Teleop.getInstance().getHardware().getMotors().get("frontshooter").setPower(-manualPower);
            Teleop.getInstance().getHardware().getMotors().get("backshooter").setPower(-manualPower);
        } else if (veloControlActive) {
            frontShooterController.setPIDFValues(frontShooterkP, frontShooterkI, frontShooterkD, frontShooterkF);
            frontShooterController.updateSetpoint(frontShooterTargetVelo);
            backShooterController.setPIDFValues(backShooterkP, backShooterkI, backShooterkD, backShooterkF);
            backShooterController.updateSetpoint(backShooterTargetVelo);
            double frontShooterPower = frontShooterController.update(bulkRead.getFrontShooterVelo());
            double backShooterPower = backShooterController.update(bulkRead.getBackShooterVelo());

            Teleop.getInstance().getHardware().getMotors().get("frontshooter").setPower(frontShooterPower);
            Teleop.getInstance().getHardware().getMotors().get("backshooter").setPower(-0.5);

            dashboardTelemetry.addData("frontShooterPower", frontShooterPower);
            dashboardTelemetry.addData("backShooterPower", backShooterPower);
        } else if(powershotToggle.getState()){
            frontShooterController.setPIDFValues(frontShooterkP, frontShooterkI, frontShooterkD, frontShooterkF);
            frontShooterController.updateSetpoint(frontShooterPowershotVelo);
            double frontShooterPower = frontShooterController.update(bulkRead.getFrontShooterVelo());

            Teleop.getInstance().getHardware().getMotors().get("frontshooter").setPower(frontShooterPower);
            Teleop.getInstance().getHardware().getMotors().get("backshooter").setPower(-0.6);

            dashboardTelemetry.addData("frontShooterPower", frontShooterPower);
        } else {
            Teleop.getInstance().getHardware().getMotors().get("frontshooter").setPower(0.0);
            Teleop.getInstance().getHardware().getMotors().get("backshooter").setPower(0.0);
        }
//            Teleop.getInstance().getHardware().getMotors().get("frontshooter").setPower(frontPower);
//            Teleop.getInstance().getHardware().getMotors().get("backshooter").setPower(backPower);
        dashboardTelemetry.addData("manualPower", manualPower);
        dashboardTelemetry.addData("Target frontShooter Velo", frontShooterTargetVelo);
        dashboardTelemetry.addData("Target backShooter Velo", backShooterTargetVelo);
        dashboardTelemetry.addData("Front Shooter Velo", bulkRead.getFrontShooterVelo());
        dashboardTelemetry.addData("Back Shooter Velo", bulkRead.getBackShooterVelo());
        dashboardTelemetry.addData("PID Active ", veloControlActive);
        dashboardTelemetry.update();

    }

    @Override
    public void stop() {

    }
}
