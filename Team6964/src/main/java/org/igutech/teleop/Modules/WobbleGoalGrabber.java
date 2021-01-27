package org.igutech.teleop.Modules;

import org.igutech.teleop.Module;
import org.igutech.teleop.Teleop;
import org.igutech.utils.ButtonToggle;

public class WobbleGoalGrabber extends Module {
    private GamepadService gamepadService;
    private ButtonToggle wobbleGoalLift;
    private ButtonToggle wobbleGoalServo;
    public WobbleGoalGrabber() {
        super(50, "WobbleGoalGrabber");
    }

    @Override
    public void init() {
        Teleop.getInstance().getHardware().getServos().get("wobbleGoalLift").setPosition(0);

        gamepadService = (GamepadService) Teleop.getInstance().getService("GamepadService");
        wobbleGoalLift = new ButtonToggle(1, "dpad_left", () -> {
            Teleop.getInstance().getHardware().getServos().get("wobbleGoalLift").setPosition(1);
        }, () -> {
            Teleop.getInstance().getHardware().getServos().get("wobbleGoalLift").setPosition(0);
        });

        wobbleGoalServo = new ButtonToggle(1, "dpad_right", () -> {
            Teleop.getInstance().getHardware().getServos().get("wobbleGoalServo").setPosition(0.47);
        }, () -> {
            Teleop.getInstance().getHardware().getServos().get("wobbleGoalServo").setPosition(0.25);
        });
        wobbleGoalLift.init();
        wobbleGoalServo.init();

        Teleop.getInstance().getHardware().getServos().get("wobbleGoalServo").setPosition(0.25);

    }

    @Override
    public void loop() {
        wobbleGoalLift.loop();
        wobbleGoalServo.loop();
    }
}
