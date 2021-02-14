package org.igutech.teleop.Modules;

import org.igutech.teleop.Module;
import org.igutech.teleop.Teleop;
import org.igutech.utils.ButtonToggle;

import java.util.HashMap;

public class WobbleGoalGrabber extends Module {
    private TimerService timerService;
    private GamepadService gamepadService;
    private ButtonToggle wobbleGoalLift;
    private ButtonToggle wobbleGoalServo;
    private int wobbleGoalLiftServoPosition = 1;
    private HashMap<Integer, Double> wobbleGoalLiftPositions;

    public WobbleGoalGrabber() {
        super(50, "WobbleGoalGrabber");
    }

    @Override
    public void init() {
        Teleop.getInstance().getHardware().getServos().get("wobbleGoalLift").setPosition(0.15);
        Teleop.getInstance().getHardware().getServos().get("wobbleGoalServo").setPosition(0.25);

        timerService = (TimerService) Teleop.getInstance().getService("TimerService");
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
        if (wobbleGoalLiftServoPosition > 3) {
            wobbleGoalLiftServoPosition = 0;
        }
        Teleop.getInstance().getHardware().getServos().get("wobbleGoalLift").setPosition(wobbleGoalLiftPositions.get(wobbleGoalLiftServoPosition));
        switch (wobbleGoalLiftServoPosition) {
            case 0:
                wobbleGoalLiftServoPosition++;
                break;
            case 1:
                timerService.registerUniqueTimerEvent(300, "Wobble", () -> {
                    Teleop.getInstance().getHardware().getServos().get("wobbleGoalServo").setPosition(0.25);
                    wobbleGoalLiftServoPosition++;
                });
                break;
            case 2:
                wobbleGoalLiftServoPosition++;
                break;
            case 3:
                timerService.registerUniqueTimerEvent(1000, "Wobble", () -> {
                    Teleop.getInstance().getHardware().getServos().get("wobbleGoalServo").setPosition(25);
                    wobbleGoalLiftServoPosition=0;
                    timerService.registerUniqueTimerEvent(1000, "Wobble", () -> {
                        Teleop.getInstance().getHardware().getServos().get("wobbleGoalLift").setPosition(wobbleGoalLiftPositions.get(wobbleGoalLiftServoPosition));
                        wobbleGoalLiftServoPosition++;
                        System.out.println();
                    });
                });
                break;

        }


    }


    @Override
    public void loop() {
        wobbleGoalLift.loop();
        wobbleGoalServo.loop();
    }
}
