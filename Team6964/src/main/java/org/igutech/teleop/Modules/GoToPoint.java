package org.igutech.teleop.Modules;



import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.roadrunner.geometry.Pose2d;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.igutech.teleop.Module;
import org.igutech.teleop.Teleop;
import org.igutech.utils.ButtonToggle;
import org.igutech.utils.control.PIDFController;

import java.util.Arrays;
@Config
public class GoToPoint extends Module {
    public static double kp = 0.03;
    public static double ki = 0;
    public static double kd = 0.000;
    //public static double kd = 0.0025;

    public static double kpr = 0.6;
    public static double kir = 0;
    public static double kdr = 0.00;
    //public static double kdr = 0.02;
    public static int targetX=0;
    public static int targetY=0;
    public static double targetTheta=Math.PI    ;
    private PIDFController xController;
    private PIDFController yController;
    private PIDFController thetaController;
    FtcDashboard dashboard = FtcDashboard.getInstance();
    Telemetry dashboardTelemetry = dashboard.getTelemetry();
    private ButtonToggle status;
    private ThreeWheelOdometry threeWheelOdometry;
    public GoToPoint() {
        super(1300, "GoToPoint");
    }

    @Override
    public void init() {
        threeWheelOdometry = (ThreeWheelOdometry) Teleop.getInstance().getModuleByName("ThreeWheelOdometry");
        xController = new PIDFController(kp, ki, kd, 0);
        yController = new PIDFController(kp, ki, kd, 0);
        thetaController = new PIDFController(kpr, kir, kdr, 0);
        status = new ButtonToggle(1, "y", () -> {
        }, () -> {
        });
        status.init();

    }

    @Override
    public void loop() {

        status.loop();
       if (status.getState()){
      ;
            goToPoint(new Pose2d(targetX, targetY, targetTheta), new Pose2d(threeWheelOdometry.getPose().getX(), threeWheelOdometry.getPose().getY(), threeWheelOdometry.getPose().getHeading()));
        }
        xController.setkP(kp);
        yController.setkP(kp);
        thetaController.setkP(kpr);
        dashboardTelemetry.addData("target x",targetX);
        dashboardTelemetry.addData("target y",targetY);
        dashboardTelemetry.addData("target heading",targetTheta);
        dashboardTelemetry.addData("x",threeWheelOdometry.getPose().getX());
        dashboardTelemetry.addData("y",threeWheelOdometry.getPose().getY());
        dashboardTelemetry.addData("heading",threeWheelOdometry.getPose().getHeading());

        dashboardTelemetry.update();

    }

    public void setPowerCentric(double x, double y, double rot, double heading) {

        x = x * Math.cos(heading) - y * Math.sin(heading);
        y = x * Math.sin(heading) + y * Math.cos(heading);
        setPower(x, y, rot);

    }


    public void setPower(double x, double y, double rot) {
        double FrontLeftVal = y - x + rot;
        double FrontRightVal = y + x - rot;
        double BackLeftVal = y + x + rot;
        double BackRightVal = y - x - rot;

        double[] power = {FrontLeftVal, FrontRightVal, BackLeftVal, BackRightVal};
        Arrays.sort(power);

        if (power[3] > 1) {
            FrontLeftVal /= power[3];
            FrontRightVal /= power[3];
            BackLeftVal /= power[3];
            BackRightVal /= power[3];
        }


        Teleop.getInstance().getHardware().getMotors().get("frontleft").setPower(FrontLeftVal);
        Teleop.getInstance().getHardware().getMotors().get("frontright").setPower(-FrontRightVal);
        Teleop.getInstance().getHardware().getMotors().get("backleft").setPower(BackLeftVal);
        Teleop.getInstance().getHardware().getMotors().get("backright").setPower(-BackRightVal);

        dashboardTelemetry.addData("frontleft",FrontLeftVal);
        dashboardTelemetry.addData("frontright",FrontRightVal);
        dashboardTelemetry.addData("backright",BackRightVal);
        dashboardTelemetry.addData("backleft",BackLeftVal);

    }

    public void goToPoint(Pose2d target, Pose2d current) {

        double heading = current.getHeading();  //0 radian
        double target_heading = target.getHeading(); //PI
        xController.updateSetpoint(target.getX());
        yController.updateSetpoint(target.getY());
        thetaController.updateSetpoint(target_heading);
        if (current.getHeading() <= Math.PI) {
            heading = current.getHeading();
        } else {
            heading = -((2 * Math.PI) - current.getHeading());
        }

        if (Math.abs(target.getHeading() - heading) >= Math.toRadians(180.0)) {
            target_heading = -((2 * Math.PI) - target.getHeading());
        }
        double _x_power = xController.update(current.getX());
        double _y_power = yController.update(current.getY());
        double _rot_power = thetaController.update(heading);
        //rot power = 1.884
        // if(Math.abs(currentV.distanceToVector(new Vector3(target.x,target.y))) >3){
        double[] power = {_x_power, _y_power, _rot_power};
        setPowerCentric(0,0, -_rot_power, current.getHeading());




    }
}

