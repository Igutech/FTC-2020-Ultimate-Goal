package org.igutech.teleop.Modules;

import org.igutech.config.Hardware;
import org.igutech.teleop.Module;
import org.igutech.teleop.Teleop;
import org.igutech.utils.ButtonToggle;

public class Index extends Module {
    private GamepadService gamepadService;
    private TimerService timerService;
    private ButtonToggle shootToggle;
    private ButtonToggle shooterServoToggle;
    private boolean isIndexUp = false;
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

            shootToggle = new ButtonToggle(1, "left_bumper", this::shootRings, this::shootRings);

            shooterServoToggle = new ButtonToggle(1, "y", () -> {
                setIndexServoStatus(true);
            }, () -> {
                setIndexServoStatus(false);
            });


            shootToggle.init();
            shooterServoToggle.init();

        }


        hardware.getServos().get("shooterServo1").setPosition(0.21);
        hardware.getServos().get("shooterServo2").setPosition(0.46);
        hardware.getServos().get("liftServo").setPosition(0.86);


    }

    @Override
    public void loop() {
        if (isTeleop) {
            shootToggle.loop();
            shooterServoToggle.loop();
        }
    }

    public boolean getIndexStatus() {
        return isIndexUp;
    }


    public void setIndexStatus(boolean indexStatus) {
        if (indexStatus) {
            hardware.getServos().get("liftServo").setPosition(0.54);
        } else {
            hardware.getServos().get("shooterServo1").setPosition(0.21);
            hardware.getServos().get("shooterServo2").setPosition(0.46);
            hardware.getServos().get("liftServo").setPosition(0.86);
        }
        isIndexUp = indexStatus;
    }

    public void setIndexServoStatus(boolean servoStatus) {
        if (servoStatus) {
            hardware.getServos().get("shooterServo1").setPosition(0.43);
            hardware.getServos().get("shooterServo2").setPosition(0.23);
        } else {
            hardware.getServos().get("shooterServo1").setPosition(0.21);
            hardware.getServos().get("shooterServo2").setPosition(0.46);
        }
    }

    private void shootRings() {
        int time = 0;
        for (int i = 0; i < 3; i++) {
            timerService.registerSingleTimerEvent(time, () -> setIndexServoStatus(true));
            time += 500;
            timerService.registerSingleTimerEvent(time, () -> setIndexServoStatus(false));
            time += 500;
        }
        timerService.registerSingleTimerEvent(time, () -> hardware.getServos().get("liftServo").setPosition(0.86));

    }
}
