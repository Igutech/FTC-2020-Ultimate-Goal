package org.igutech.teleop.Modules;

import org.igutech.teleop.Module;
import org.igutech.teleop.Teleop;
import org.igutech.utils.ButtonToggle;

import java.util.HashMap;

public class Index extends Module {
    private GamepadService gamepadService;
    private TimerService timerService;
    private HashMap<Integer, Double> liftPositions;
    private ButtonToggle shootToggle;
    private ButtonToggle shooterServoToggle;

    private int currentShooterServoLevel = 0;

    public Index() {
        super(300, "Index");
    }

    @Override
    public void init() {
        Teleop.getInstance().getHardware().getServos().get("shooterServo").setPosition(0.1);
        gamepadService = (GamepadService) Teleop.getInstance().getService("GamepadService");
        timerService = (TimerService) Teleop.getInstance().getService("TimerService");
        liftPositions = new HashMap<>();
        liftPositions.put(0, 0.7);
        liftPositions.put(1, 0.42);
        liftPositions.put(2, 0.37);
        liftPositions.put(3, 0.28);
        shootToggle = new ButtonToggle(1, "left_bumper", () -> {
            handleLift();
        }, () -> {
            handleLift();
        });
        shootToggle.init();
        Teleop.getInstance().getHardware().getServos().get("liftServo").setPosition(liftPositions.get(currentShooterServoLevel));

        shooterServoToggle = new ButtonToggle(1, "y", () -> {
            Teleop.getInstance().getHardware().getServos().get("shooterServo").setPosition(0.32);

        }, () -> {
            Teleop.getInstance().getHardware().getServos().get("shooterServo").setPosition(0.1);

        });
        shooterServoToggle.init();
    }

    @Override
    public void loop() {
        shootToggle.loop();
        shooterServoToggle.loop();
    }


    private void handleLift() {
        timerService.registerSingleTimerEvent(1, () -> {
            if (currentShooterServoLevel > 3) {
                currentShooterServoLevel = 0;
            }
            Teleop.getInstance().getHardware().getServos().get("liftServo").setPosition(liftPositions.get(currentShooterServoLevel));
            currentShooterServoLevel++;
            timerService.registerSingleTimerEvent(600, () -> {
                Teleop.getInstance().getHardware().getServos().get("shooterServo").setPosition(0.32);
                timerService.registerSingleTimerEvent(60, () -> {
                    Teleop.getInstance().getHardware().getServos().get("shooterServo").setPosition(0.1);
                });
            });


        });
    }
}
