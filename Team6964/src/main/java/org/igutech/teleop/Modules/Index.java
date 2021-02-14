package org.igutech.teleop.Modules;

import org.igutech.auto.util.LoggingUtil;
import org.igutech.config.Hardware;
import org.igutech.teleop.Module;
import org.igutech.teleop.Teleop;
import org.igutech.utils.ButtonToggle;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashMap;

public class Index extends Module {
    private GamepadService gamepadService;
    private TimerService timerService;
    private HashMap<Integer, Double> liftPositions;
    private ButtonToggle shootToggle;
    private ButtonToggle shooterServoToggle;
    private ButtonToggle dpadUp;
    private ButtonToggle dpadDown;
    private int currentShooterServoLevel = 0;
    private Hardware hardware;
    private boolean isTeleop;

    public Index(Hardware hardware, TimerService timerService, boolean isTeleop) {
        super(300, "Index");
        this.timerService = timerService;
        this.hardware = hardware;
        this.isTeleop = isTeleop;
    }

    @Override
    public void init() {
        if (isTeleop) {
            gamepadService = (GamepadService) Teleop.getInstance().getService("GamepadService");

            shootToggle = new ButtonToggle(1, "left_bumper", () -> {
                handleLift();
            }, () -> {
                handleLift();
            });

            shooterServoToggle = new ButtonToggle(1, "y", () -> {
                hardware.getServos().get("shooterServo").setPosition(0.32);

            }, () -> {
                hardware.getServos().get("shooterServo").setPosition(0.1);

            });
            dpadUp = new ButtonToggle(1, "dpad_up", () -> {
                currentShooterServoLevel++;
                if (currentShooterServoLevel > 3) {
                    currentShooterServoLevel = 0;
                }
                hardware.getServos().get("liftServo").setPosition(liftPositions.get(currentShooterServoLevel));
            }, () -> {
                currentShooterServoLevel++;
                if (currentShooterServoLevel > 3) {
                    currentShooterServoLevel = 0;
                }
                hardware.getServos().get("liftServo").setPosition(liftPositions.get(currentShooterServoLevel));
            });

            dpadDown = new ButtonToggle(1, "dpad_down", () -> {
                currentShooterServoLevel--;
                if (currentShooterServoLevel < 0) {
                    currentShooterServoLevel = 0;
                }
                hardware.getServos().get("liftServo").setPosition(liftPositions.get(currentShooterServoLevel));
            }, () -> {
                currentShooterServoLevel--;
                if (currentShooterServoLevel < 0) {
                    currentShooterServoLevel = 0;
                }
                hardware.getServos().get("liftServo").setPosition(liftPositions.get(currentShooterServoLevel));
            });

            shootToggle.init();
            shooterServoToggle.init();
            dpadUp.init();
            dpadDown.init();
        }


        liftPositions = new HashMap<>();
        liftPositions.put(0, 0.78);
        liftPositions.put(1, 0.65);
        liftPositions.put(2, 0.59);
        liftPositions.put(3, 0.5);
        hardware.getServos().get("shooterServo").setPosition(0.1);
        hardware.getServos().get("liftServo").setPosition(0.78);


    }

    @Override
    public void loop() {
        if (isTeleop) {
            shootToggle.loop();
            shooterServoToggle.loop();
            dpadUp.loop();
            dpadDown.loop();
        }
    }

    public void handleLift() {
        currentShooterServoLevel++;
        if (currentShooterServoLevel > 3) {
            currentShooterServoLevel = 0;
        }
        hardware.getServos().get("liftServo").setPosition(liftPositions.get(currentShooterServoLevel));
        if (currentShooterServoLevel == 0) {
            timerService.registerUniqueTimerEvent(600, "Wobble", () -> {
            });
        } else if (currentShooterServoLevel == 1) {
            timerService.registerUniqueTimerEvent(600, "Wobble", () -> {
                hardware.getServos().get("shooterServo").setPosition(0.32);
                timerService.registerUniqueTimerEvent(150, "Wobble", () -> {
                    hardware.getServos().get("shooterServo").setPosition(0.1);
                });
            });
        } else {
            timerService.registerUniqueTimerEvent(250, "Wobble", () -> {
                hardware.getServos().get("shooterServo").setPosition(0.32);
                timerService.registerUniqueTimerEvent(200, "Wobble", () -> {
                    hardware.getServos().get("shooterServo").setPosition(0.1);
                });
            });
        }
    }


    public int getCurrentShooterServoLevel() {
        return currentShooterServoLevel;
    }

    public void setCurrentShooterServoLevel(int level) {
        currentShooterServoLevel = level;
    }
}
