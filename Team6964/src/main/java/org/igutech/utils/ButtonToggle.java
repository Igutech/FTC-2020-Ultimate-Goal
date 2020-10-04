package org.igutech.utils;

import org.igutech.teleop.Modules.GamepadService;
import org.igutech.teleop.Teleop;
import org.igutech.utils.events.Callback;

public class ButtonToggle {
    private int gamepadNumber;
    private String button;
    private GamepadService gamepadService;
    private boolean toggle = false;
    private boolean previousButtonPosition = false;
    private boolean currentButtonPosition = false;
    private boolean wasJustPressed = false;
    private Callback action1;
    private Callback action2;

    public ButtonToggle(int gamepadNumber, String button, Callback action1, Callback action2) {
        this.gamepadNumber = gamepadNumber;
        this.button = button;
        this.action1 = action1;
        this.action2 = action2;
    }

    public void init() {
        gamepadService = (GamepadService) Teleop.getInstance().getService("GamepadService");
    }

    public void loop() {
        currentButtonPosition = gamepadService.getDigital(gamepadNumber, button);

        if (currentButtonPosition && !previousButtonPosition) {
            wasJustPressed = true;
            toggle = !toggle;
            if (toggle) {
                action1.call();
            } else {
                action2.call();
            }
        } else {
            wasJustPressed = false;
        }

        previousButtonPosition = currentButtonPosition;

    }

    public boolean getState() {
        return toggle;
    }

    public boolean isDown() {
        return currentButtonPosition;
    }

    public boolean wasJustPressed() {
        return wasJustPressed;
    }
}
