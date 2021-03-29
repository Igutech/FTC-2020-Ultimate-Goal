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

            shootToggle = new ButtonToggle(1, "left_bumper", () -> {
                handleLift();
            }, () -> {
                handleLift();
            });

            shooterServoToggle = new ButtonToggle(1, "y", () -> {
                hardware.getServos().get("shooterServo1").setPosition(0.43);
                hardware.getServos().get("shooterServo2").setPosition(0.23);

            }, () -> {
                hardware.getServos().get("shooterServo1").setPosition(0.21);
                hardware.getServos().get("shooterServo2").setPosition(0.46);

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

    public void handleLift() {

        if (shootToggle.getState()) {
            hardware.getServos().get("liftServo").setPosition(0.54);
            isIndexUp = true;
        } else {
            hardware.getServos().get("liftServo").setPosition(0.86);
            isIndexUp = false;
        }

    }


    public boolean getIndexStatus() {
        return isIndexUp;
    }


    public void setIndexStatus(boolean shooterIsUp) {
        this.isIndexUp = shooterIsUp;
    }
}
