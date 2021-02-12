package org.igutech.teleop.Modules;

import org.igutech.teleop.Module;
import org.igutech.teleop.Teleop;
import org.igutech.utils.ButtonToggle;

import java.util.HashMap;

public class WobbleGoalGrabber extends Module {
    private GamepadService gamepadService;
    private ButtonToggle wobbleGoalLift;
    private ButtonToggle wobbleGoalServo;
    private int wobbleGoalLiftServoPosition = 0;
    private HashMap<Integer, Double> wobbleGoalLiftPositions;

    public WobbleGoalGrabber() {
        super(50, "WobbleGoalGrabber");
    }

    @Override
    public void init() {
        Teleop.getInstance().getHardware().getServos().get("wobbleGoalLift").setPosition(0.15);
        Teleop.getInstance().getHardware().getServos().get("wobbleGoalServo").setPosition(0.25);

        gamepadService = (GamepadService) Teleop.getInstance().getService("GamepadService");
        wobbleGoalLift = new ButtonToggle(1, "dpad_left", () -> {
            handleWobbleGoalLift();
        }, () -> {
            handleWobbleGoalLift();
        });

        wobbleGoalServo = new ButtonToggle(1, "dpad_right", () -> {
            Teleop.getInstance().getHardware().getServos().get("wobbleGoalServo").setPosition(0.47);
        }, () -> {
            Teleop.getInstance().getHardware().getServos().get("wobbleGoalServo").setPosition(0.25);
        });
        wobbleGoalLift.init();
        wobbleGoalServo.init();

        wobbleGoalLiftPositions = new HashMap<>();
        //transport, pickup, transport, dump
        wobbleGoalLiftPositions.put(0, 0.15);
        wobbleGoalLiftPositions.put(1, 1.0);
        wobbleGoalLiftPositions.put(2, 0.15);
        wobbleGoalLiftPositions.put(3, 0.4);


    }

    private void handleWobbleGoalLift() {
        wobbleGoalLiftServoPosition++;
        if (wobbleGoalLiftServoPosition > 3) {
            wobbleGoalLiftServoPosition = 0;
        }

        Teleop.getInstance().getHardware().getServos().get("wobbleGoalLift").setPosition(wobbleGoalLiftPositions.get(wobbleGoalLiftServoPosition));


    }

    @Override
    public void loop() {
        wobbleGoalLift.loop();
        wobbleGoalServo.loop();
    }
}
