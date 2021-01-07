package org.igutech.teleop.Modules;

import org.igutech.teleop.Module;
import org.igutech.teleop.Teleop;
import org.igutech.utils.ButtonToggle;

import static org.igutech.teleop.Modules.Index.currentShooterServoLevel;

public class Intake extends Module {
    GamepadService gamepadService;
    ButtonToggle xToggle;
    ButtonToggle releaseIntake;
    public Intake() {
        super(200, "Intake");
    }

    @Override
    public void init() {
        gamepadService=(GamepadService) Teleop.getInstance().getService("GamepadService");
        xToggle = new ButtonToggle(1,"x",()->{},()->{});
        xToggle.init();

        releaseIntake = new ButtonToggle(1,"dpad_left",()->{
            Teleop.getInstance().getHardware().getServos().get("releaseLiftServo").setPosition(0.2);
        },()->{
            Teleop.getInstance().getHardware().getServos().get("releaseLiftServo").setPosition(0.4);
        });
        Teleop.getInstance().getHardware().getServos().get("releaseLiftServo").setPosition(0.4);

        releaseIntake.init();
    }

    @Override
    public void start() {
        Teleop.getInstance().getHardware().getServos().get("releaseLiftServo").setPosition(0.2);

    }

    @Override
    public void loop() {
        double power = gamepadService.getAnalog(1,"left_trigger");
        if(power>0.1){
            if(currentShooterServoLevel !=0){
               // Teleop.getInstance().getHardware().getServos().get("liftServo").setPosition(0.78);
            }
            power=-1;
        }
        if(xToggle.getState()){
            power = gamepadService.getAnalog(1,"left_trigger");
        }

        Teleop.getInstance().getHardware().getMotors().get("intake").setPower(power);

        xToggle.loop();
        releaseIntake.loop();

    }
}