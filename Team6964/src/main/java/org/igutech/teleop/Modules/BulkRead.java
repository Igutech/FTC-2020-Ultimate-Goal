package org.igutech.teleop.Modules;

import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.robotcore.hardware.DcMotorEx;

import org.igutech.config.Hardware;
import org.igutech.teleop.Service;
import org.igutech.teleop.Teleop;

import java.util.List;

public class BulkRead extends Service {

    List<LynxModule> allHubs;
    public BulkRead() {
        super("BulkRead");
    }

    @Override
    public void init() {

        allHubs = Teleop.getInstance().hardwareMap.getAll(LynxModule.class);
        for (LynxModule module : allHubs) {
            module.setBulkCachingMode(LynxModule.BulkCachingMode.MANUAL);
        }
    }

    @Override
    public void loop() {
        for (LynxModule module : allHubs) {
            module.clearBulkCache();
        }

    }


}