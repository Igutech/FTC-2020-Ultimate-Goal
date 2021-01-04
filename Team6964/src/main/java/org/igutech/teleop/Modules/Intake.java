package org.igutech.teleop.Modules;

import org.igutech.teleop.Module;
import org.igutech.teleop.Teleop;
import org.igutech.utils.ButtonToggle;

public class Intake extends Module {
    GamepadService gamepadService;
    ButtonToggle xToggle;
    public Intake() {
        super(200, "Intake");
    }

    @Override
    public void init() {
        gamepadService=(GamepadService) Teleop.getInstance().getService("GamepadService");
        xToggle = new ButtonToggle(1,"x",()->{},()->{});
        xToggle.init();
    }

    @Override
    public void loop() {
        double power = gamepadService.getAnalog(1,"left_trigger");
        if(power>0.1){
            power=-1;
        }
        if(xToggle.getState()){
            power = power*-1;
        }
        Teleop.getInstance().getHardware().getMotors().get("intake").setPower(power);

        xToggle.loop();

    }
}
