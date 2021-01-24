package org.igutech.auto.util;

import com.qualcomm.robotcore.hardware.HardwareMap;

import org.igutech.config.Hardware;
import java.util.ArrayList;
import java.util.Collections;

public class AutoUtilManager {

    private Hardware hardware;

    public AutoUtilManager(HardwareMap hardwareMap) {
        this.hardware = new Hardware(hardwareMap);
    }

    public Hardware getHardware() {
        return hardware;
    }



}
