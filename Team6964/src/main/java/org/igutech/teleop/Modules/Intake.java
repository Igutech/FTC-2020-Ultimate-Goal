package org.igutech.teleop.Modules;

import org.igutech.teleop.Module;
import org.igutech.teleop.Teleop;

public class Intake extends Module {
    GamepadService gamepadService;
    public Intake() {
        super(200, "Intake");
    }

    @Override
    public void init() {
        gamepadService=(GamepadService) Teleop.getInstance().getService("GamepadService");
    }

    @Override
    public void loop() {
        double power = gamepadService.getAnalog(2,"right_trigger");
        Teleop.getInstance().getHardware().getMotors().get("intake").setPower(power);
    }
}
