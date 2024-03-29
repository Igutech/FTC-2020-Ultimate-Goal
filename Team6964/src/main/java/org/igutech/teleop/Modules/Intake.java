package org.igutech.teleop.Modules;

import org.igutech.teleop.Module;
import org.igutech.teleop.Teleop;
import org.igutech.utils.ButtonToggle;

public class Intake extends Module {
    GamepadService gamepadService;
    ButtonToggle xToggle;
    ButtonToggle releaseIntake;

    public Intake() {
        super(200, "Intake");
    }

    @Override
    public void init() {
        gamepadService = (GamepadService) Teleop.getInstance().getService("GamepadService");
        xToggle = new ButtonToggle(1, "x", () -> {
        }, () -> {
        });
        xToggle.init();

        releaseIntake = new ButtonToggle(1, "a", () -> {
            Teleop.getInstance().getHardware().getServos().get("releaseLiftServo").setPosition(0.33);
        }, () -> {
            Teleop.getInstance().getHardware().getServos().get("releaseLiftServo").setPosition(0.2);
        });
        Teleop.getInstance().getHardware().getServos().get("releaseLiftServo").setPosition(0.33);

        releaseIntake.init();
    }

    @Override
    public void start() {
        Teleop.getInstance().getHardware().getServos().get("releaseLiftServo").setPosition(0.2);
    }

    @Override
    public void loop() {
        double power = gamepadService.getAnalog(1, "left_trigger");
        if (power > 0.1) {
            Index index = (Index) Teleop.getInstance().getModuleByName("Index");
            if (index.getIndexStatus()) {
                index.setIndexStatus(false);
            }
            power = -1;
        }
        if (xToggle.getState()) {
            power = gamepadService.getAnalog(1, "left_trigger");
        }

        Teleop.getInstance().getHardware().getMotors().get("intake").setPower(power);
        Teleop.getInstance().getHardware().getMotors().get("intake2").setPower(power);

        xToggle.loop();
        releaseIntake.loop();

    }
}