package org.igutech.teleop.Modules;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.config.Config;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.igutech.teleop.Module;
import org.igutech.teleop.Teleop;
import org.igutech.utils.ButtonToggle;
import org.igutech.utils.control.PIDController;


@Config
public class Shooter extends Module {
    FtcDashboard dashboard = FtcDashboard.getInstance();
    Telemetry dashboardTelemetry = dashboard.getTelemetry();
    private GamepadService gamepadService;
    private BulkRead bulkRead;
    public static double pShooterLeft = 0.008;
    public static double iShooterLeft = 0.00;
    public static double dShooterLeft = 0.00;

    public static double motorVelo=1200;
    private PIDController pidController;
    private boolean veloControlActive = false;
    private ButtonToggle veloToggle;

    public Shooter() {
        super(500, "Shooter");
    }

    @Override
    public void init() {
        gamepadService = (GamepadService) Teleop.getInstance().getService("GamepadService");
        bulkRead = (BulkRead) Teleop.getInstance().getService("BulkRead");
        pidController = new PIDController(pShooterLeft, iShooterLeft, dShooterLeft);
        veloToggle = new ButtonToggle(2, "x", () -> {
        }, () -> {
        });
        veloToggle.init();
    }

    @Override
    public void loop() {
        veloToggle.loop();
        veloControlActive = veloToggle.getState();
        double manualPower = gamepadService.getAnalog(2, "right_stick_y");
        if (Math.abs(manualPower) != 0.0) {
            Teleop.getInstance().getHardware().getMotors().get("shooterLeft").setPower(manualPower);
            Teleop.getInstance().getHardware().getMotors().get("shooterRight").setPower(manualPower);
        } else if (veloControlActive) {
            pidController.setkP(pShooterLeft);
            pidController.updateSetpoint(motorVelo);
            double autoPower = pidController.update(bulkRead.getShooterLeftVelo());
            dashboardTelemetry.addData("autoPower",autoPower);
            Teleop.getInstance().getHardware().getMotors().get("shooterLeft").setPower(-autoPower);
            Teleop.getInstance().getHardware().getMotors().get("shooterRight").setPower(-autoPower);
        } else {
            Teleop.getInstance().getHardware().getMotors().get("shooterLeft").setPower(0.0);
            Teleop.getInstance().getHardware().getMotors().get("shooterRight").setPower(0.0);
        }


        dashboardTelemetry.addData("manual", manualPower);
        dashboardTelemetry.addData("Target Velo", motorVelo);
        dashboardTelemetry.addData("MotorLeft Velo", bulkRead.getShooterLeftVelo());
        dashboardTelemetry.addData("MotorRight Velo", bulkRead.getShooterRightVelo());
        dashboardTelemetry.addData("PID Active ", veloControlActive);
        dashboardTelemetry.update();

    }
}
