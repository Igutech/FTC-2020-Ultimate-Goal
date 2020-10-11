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
    public static double p = 0.00;
    public static double i = 0.00;
    public static double d = 0.00;
    public static double motorVelo;
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
        pidController = new PIDController(p, i, d);
        veloToggle = new ButtonToggle(2, "x", ()->{},()->{});
        veloToggle.init();
    }

    @Override
    public void loop() {
        veloToggle.loop();
        veloControlActive = veloToggle.getState();
        double manualPower = gamepadService.getAnalog(2, "right_stick_y");
        if (Math.abs(manualPower) != 0.0) {
            Teleop.getInstance().getHardware().getMotors().get("frontleft").setPower(manualPower);
            Teleop.getInstance().getHardware().getMotors().get("backleft").setPower(manualPower);
        }
//        } else if (veloControlActive) {
//            pidController.updateSetpoint(motorVelo);
//            double autoPower = pidController.update(bulkRead.getBackRightVelo());
//            Teleop.getInstance().getHardware().getMotors().get("shooter1").setPower(autoPower);
//            Teleop.getInstance().getHardware().getMotors().get("shooter2").setPower(autoPower);
//        }

        dashboardTelemetry.addData("manual", manualPower);
        dashboardTelemetry.addData("Target Velo",motorVelo);
        dashboardTelemetry.addData("MotorLeft Velo",bulkRead.getFrontLeftVelo());
        dashboardTelemetry.addData("MotorRight Velo",bulkRead.getBackLeftVelo());
        dashboardTelemetry.addData("PID Active ",veloControlActive);
        dashboardTelemetry.update();

    }
}
