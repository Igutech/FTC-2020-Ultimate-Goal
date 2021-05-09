package org.igutech.teleop.Modules;

import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.acmerobotics.roadrunner.trajectory.Trajectory;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.apache.commons.math3.util.FastMath;
import org.igutech.auto.roadrunner.SampleMecanumDrive;
import org.igutech.teleop.Module;
import org.igutech.teleop.Teleop;
import org.igutech.utils.ButtonToggle;
import org.igutech.utils.FTCMath;
import org.igutech.utils.PoseStorage;

import java.util.logging.Logger;

@Config
public class RRBasedDriveTrain extends Module {
    private ButtonToggle gotoPointToggle;
    private ButtonToggle resetOdoToggle;


    private double VX_WEIGHT = 1;
    private double VY_WEIGHT = 1;
    private double OMEGA_WEIGHT = 1;
    private SampleMecanumDrive drive;
    private Pose2d baseVel;
    private Pose2d vel;
    private GamepadService gamepadService;
    private HardwareMap hwMap;
    private DriveTrainState state;
    public static double x = -10;
    public static double y = -33;

    public RRBasedDriveTrain(HardwareMap hwMap) {
        super(1000, "RRBasedDriveTrain");
        this.hwMap = hwMap;
    }

    @Override
    public void init() {
        drive = new SampleMecanumDrive(hwMap);
        drive.setPoseEstimate(PoseStorage.currentPose);
        gamepadService = (GamepadService) Teleop.getInstance().getService("GamepadService");

        gotoPointToggle = new ButtonToggle(1, "dpad_left", () -> {
        }, () -> {
        });
        resetOdoToggle = new ButtonToggle(1, "dpad_down", () ->
                drive.setPoseEstimate(new Pose2d(-62.5, -63.5, 0)), () -> drive.setPoseEstimate(new Pose2d(-62.5, -63.5, 0)));
        gotoPointToggle.init();
        resetOdoToggle.init();

        state = DriveTrainState.DRIVER_CONTROL;

    }

    @Override
    public void loop() {

        double slowMo = gamepadService.getAnalog(1, "right_trigger");
        double vdMult = FTCMath.lerp(1, 0.4, FastMath.abs(slowMo));
        double vThetaMult = FTCMath.lerp(.8, 0.15, FastMath.abs(slowMo));

        baseVel = new Pose2d(
                -gamepadService.getAnalog(1, "right_stick_y") * vdMult,
                -gamepadService.getAnalog(1, "right_stick_x") * vdMult,
                -gamepadService.getAnalog(1, "left_stick_x") * vThetaMult
        );


        if (baseVel.getX() != 0 || baseVel.getY() != 0 || baseVel.getHeading() != 0) {
            state = DriveTrainState.DRIVER_CONTROL;
        } else if (gotoPointToggle.getState() && state != DriveTrainState.FOLLOWING) {
            state = DriveTrainState.AUTOMATIC_CONTROL;
        }

        if (state == DriveTrainState.DRIVER_CONTROL) {
            drive.cancelFollowing();
            gotoPointToggle.setState(false);
            vel = normalize(baseVel);
            drive.setDrivePower(vel);
        } else if (state != DriveTrainState.FOLLOWING) {
            Trajectory traj1 = drive.trajectoryBuilder(drive.getPoseEstimate())
                    .lineToLinearHeading(new Pose2d(x, y, 0.0))
                    .build();
            drive.followTrajectoryAsync(traj1);
            state = DriveTrainState.FOLLOWING;
        }

        drive.update();

        Teleop.getInstance().telemetry.addData("Pose", drive.getPoseEstimate());
        gotoPointToggle.loop();
        resetOdoToggle.loop();
    }

    public Pose2d normalize(Pose2d baseVel) {
        Pose2d temp;
        if (Math.abs(baseVel.getX()) + Math.abs(baseVel.getY()) + Math.abs(baseVel.getHeading()) > 1) {
            // re-normalize the powers according to the weights
            double denom = VX_WEIGHT * Math.abs(baseVel.getX())
                    + VY_WEIGHT * Math.abs(baseVel.getY())
                    + OMEGA_WEIGHT * Math.abs(baseVel.getHeading());
            temp = new Pose2d(
                    VX_WEIGHT * baseVel.getX(),
                    VY_WEIGHT * baseVel.getY(),
                    OMEGA_WEIGHT * baseVel.getHeading()
            ).div(denom);
        } else {
            temp = baseVel;
        }
        return temp;
    }

    private enum DriveTrainState {
        DRIVER_CONTROL,
        AUTOMATIC_CONTROL,
        FOLLOWING,
        OFF
    }

}
