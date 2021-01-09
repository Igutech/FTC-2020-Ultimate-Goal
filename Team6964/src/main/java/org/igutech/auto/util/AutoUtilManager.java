package org.igutech.auto.util;

import com.qualcomm.robotcore.hardware.HardwareMap;

import org.igutech.config.Hardware;

public class AutoUtilManager {

    private Hardware hardware;

    public AutoUtilManager(HardwareMap hardwareMap, String name) {
        this.hardware = new Hardware(hardwareMap);
    }

    public Hardware getHardware() {
        return hardware;
    }

}
