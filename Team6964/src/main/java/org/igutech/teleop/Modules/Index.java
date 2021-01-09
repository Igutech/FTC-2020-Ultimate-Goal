package org.igutech.teleop.Modules;

import org.igutech.auto.util.LoggingUtil;
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

    private PrintWriter printWriter;
    public static int currentShooterServoLevel = 0;

    public Index() {
        super(300, "Index");
    }

    @Override
    public void init() {
        try {
            printWriter = new PrintWriter(LoggingUtil.getLogFile("shooterInfo.csv"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        printWriter.println("time,frontShooterTarget,backShooterTarget,frontShooterVelo,backShooterVelo ");
        Teleop.getInstance().getHardware().getServos().get("shooterServo").setPosition(0.1);
        gamepadService = (GamepadService) Teleop.getInstance().getService("GamepadService");
        timerService = (TimerService) Teleop.getInstance().getService("TimerService");
        liftPositions = new HashMap<>();
        liftPositions.put(0, 0.78);
        liftPositions.put(1, 0.65);
        liftPositions.put(2, 0.59);
        liftPositions.put(3, 0.5);
        shootToggle = new ButtonToggle(1, "left_bumper", () -> {
            handleLift();
        }, () -> {
            handleLift();
        });

        shooterServoToggle = new ButtonToggle(1, "y", () -> {
            Teleop.getInstance().getHardware().getServos().get("shooterServo").setPosition(0.32);

        }, () -> {
            Teleop.getInstance().getHardware().getServos().get("shooterServo").setPosition(0.1);

        });
        dpadUp = new ButtonToggle(1, "dpad_up", () -> {
            currentShooterServoLevel++;
            if (currentShooterServoLevel > 3) {
                currentShooterServoLevel = 0;
            }
            Teleop.getInstance().getHardware().getServos().get("liftServo").setPosition(liftPositions.get(currentShooterServoLevel));
        }, () -> {
            currentShooterServoLevel++;
            if (currentShooterServoLevel > 3) {
                currentShooterServoLevel = 0;
            }
            Teleop.getInstance().getHardware().getServos().get("liftServo").setPosition(liftPositions.get(currentShooterServoLevel));
        });

        dpadDown = new ButtonToggle(1, "dpad_down", () -> {
            currentShooterServoLevel--;
            if (currentShooterServoLevel < 0) {
                currentShooterServoLevel = 0;
            }
            Teleop.getInstance().getHardware().getServos().get("liftServo").setPosition(liftPositions.get(currentShooterServoLevel));
        }, () -> {
            currentShooterServoLevel--;
            if (currentShooterServoLevel < 0) {
                currentShooterServoLevel = 0;
            }
            Teleop.getInstance().getHardware().getServos().get("liftServo").setPosition(liftPositions.get(currentShooterServoLevel));
        });

        shootToggle.init();
        shooterServoToggle.init();
        dpadUp.init();
        dpadDown.init();

        Teleop.getInstance().getHardware().getServos().get("liftServo").setPosition(0.78);


    }

    @Override
    public void loop() {
        shootToggle.loop();
        shooterServoToggle.loop();
        dpadUp.loop();
        dpadDown.loop();
    }

    private void handleLift() {
        currentShooterServoLevel++;
        if (currentShooterServoLevel > 3) {
            currentShooterServoLevel = 0;
        }
        Teleop.getInstance().getHardware().getServos().get("liftServo").setPosition(liftPositions.get(currentShooterServoLevel));
        if (currentShooterServoLevel == 0) {
            timerService.registerSingleTimerEvent(600, () -> {
            });
        } else if (currentShooterServoLevel == 1) {
            timerService.registerSingleTimerEvent(600, () -> {
                Teleop.getInstance().getHardware().getServos().get("shooterServo").setPosition(0.32);
                timerService.registerSingleTimerEvent(150, () -> {
                    Teleop.getInstance().getHardware().getServos().get("shooterServo").setPosition(0.1);
                });
            });
        } else {
            timerService.registerSingleTimerEvent(250, () -> {
                Teleop.getInstance().getHardware().getServos().get("shooterServo").setPosition(0.32);
                timerService.registerSingleTimerEvent(200, () -> {
                    Teleop.getInstance().getHardware().getServos().get("shooterServo").setPosition(0.1);
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
