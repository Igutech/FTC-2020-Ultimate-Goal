package org.igutech.auto.util;

import com.qualcomm.robotcore.hardware.HardwareMap;

import org.igutech.auto.modules.Shooter;
import org.igutech.config.Hardware;
import org.igutech.teleop.Module;
import org.igutech.teleop.Modules.DriveTrain;
import org.igutech.teleop.Modules.Index;
import org.igutech.teleop.Modules.Intake;
import org.igutech.teleop.Modules.ThreeWheelOdometry;
import org.igutech.teleop.Teleop;

import java.util.ArrayList;
import java.util.Collections;

public class AutoUtilManager {

    private Hardware hardware;
    private static AutoUtilManager instance;

    public static AutoUtilManager getInstance() {
        return instance;
    }

    private ArrayList<Module> modules;

    public AutoUtilManager(HardwareMap hardwareMap, String name) {
        this.hardware = new Hardware(hardwareMap);
    }


    public void init() {
        instance = this;
        modules = new ArrayList<>();
        Collections.sort(modules);
        for (Module module : modules) {
            module.init();
        }
    }

    public void start() {
        for (Module module : modules) {
            module.start();
        }
    }

    public void loop() {
        for (Module module : modules) {
            module.loop();
        }
    }


    public Hardware getHardware() {
        return hardware;
    }

    public Module getModuleByName(String name) {
        for (Module m : modules) {
            if (m.getName().equalsIgnoreCase(name)) return m;
        }
        return null;
    }

}
