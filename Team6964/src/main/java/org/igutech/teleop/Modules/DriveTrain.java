package org.igutech.teleop.Modules;

import org.apache.commons.math3.util.FastMath;
import org.igutech.teleop.Module;
import org.igutech.teleop.Teleop;
import org.igutech.utils.FTCMath;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DriveTrain extends Module {

    private GamepadService gamepadService;

    public DriveTrain() {
        super(1000, "Drivetrain");
    }

    @Override
    public void init() {
        gamepadService = (GamepadService) Teleop.getInstance().getService("GamepadService");

    }

    @Override
    public void start() {

    }

    @Override
    public void loop() {

        double vD = FastMath.hypot(gamepadService.getAnalog(1, "right_stick_x"),
                -gamepadService.getAnalog(1, "right_stick_y"));
        double thetaD = Math.atan2(-gamepadService.getAnalog(1, "right_stick_x"),
                gamepadService.getAnalog(1, "right_stick_y")) + FastMath.PI / 4;

        double vTheta = -gamepadService.getAnalog(1, "left_stick_x");


        double slowMo = gamepadService.getAnalog(1, "right_trigger");
        double vdMult = FTCMath.lerp(1, 0.4, FastMath.abs(slowMo));
        double vThetaMult = FTCMath.lerp(.8, 0.15, FastMath.abs(slowMo));
        vD *= vdMult;
        vTheta *= vThetaMult;

//        Teleop.getInstance().telemetry.addData("slowMo", slowMo);
//        Teleop.getInstance().telemetry.addData("vdMult", vdMult);
//        Teleop.getInstance().telemetry.addData("vThetaMult", vThetaMult);

        double frontLeft = vD * FastMath.sin(-thetaD) - vTheta;
        double frontRight = vD * FastMath.cos(-thetaD) - vTheta;
        double backLeft = vD * FastMath.cos(-thetaD) + vTheta;
        double backRight = vD * FastMath.sin(-thetaD) + vTheta;


        List<Double> powers = Arrays.asList(frontLeft, frontRight, backLeft, backRight);
        double minPower = Collections.min(powers);
        double maxPower = Collections.max(powers);
        double maxMag = FastMath.max(FastMath.abs(minPower), FastMath.abs(maxPower));

        if (maxMag > 1.0) {
            for (int i = 0; i < powers.size(); i++)
                powers.set(i, powers.get(i) / maxMag);
        }

        Teleop.getInstance().getHardware().getMotors().get("frontleft").setPower(powers.get(0));
        Teleop.getInstance().getHardware().getMotors().get("frontright").setPower(powers.get(1));
        Teleop.getInstance().getHardware().getMotors().get("backleft").setPower(-powers.get(2));
        Teleop.getInstance().getHardware().getMotors().get("backright").setPower(-powers.get(3));


        Teleop.getInstance().telemetry.addData("FrontLeft ", powers.get(0));
        Teleop.getInstance().telemetry.addData("FrontRight", powers.get(1));
        Teleop.getInstance().telemetry.addData("BackLeft", -powers.get(2));
        Teleop.getInstance().telemetry.addData("BackRight", -powers.get(3));

    }
}
