package org.igutech.teleop.Modules;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.robotcore.hardware.DcMotorEx;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.igutech.auto.util.LoggingUtil;
import org.igutech.teleop.Module;
import org.igutech.teleop.Teleop;
import org.igutech.utils.ButtonToggle;
import org.igutech.utils.control.PIDFController;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;


@Config
public class Shooter extends Module {
    FtcDashboard dashboard = FtcDashboard.getInstance();
    Telemetry dashboardTelemetry = dashboard.getTelemetry();
    private GamepadService gamepadService;
    private BulkRead bulkRead;

    public static double pShooterLeft = 0.008;
    public static double iShooterLeft = 0.00;
    public static double dShooterLeft = 0.00;
    public static double kF = 0;
    public static double motorVelo=1200;
    private PIDFController PIDFController;
    private boolean veloControlActive = false;
    private ButtonToggle veloToggle;
    PrintWriter pw;
    ArrayList<Long> time = new ArrayList<>();
    ArrayList<Double> velo = new ArrayList<>();
    ArrayList<Double> power = new ArrayList<>();
    ArrayList<Double> error = new ArrayList<>();
    DcMotorEx leftMotor = (DcMotorEx) Teleop.getInstance().getHardware().getMotors().get("shooterLeft");
    DcMotorEx rightMotor = (DcMotorEx) Teleop.getInstance().getHardware().getMotors().get("shooterRight");
    public Shooter() {
        super(500, "Shooter");
    }
    long startTime;
    @Override
    public void init() {
        gamepadService = (GamepadService) Teleop.getInstance().getService("GamepadService");
        bulkRead = (BulkRead) Teleop.getInstance().getService("BulkRead");
        PIDFController = new PIDFController(pShooterLeft, iShooterLeft, dShooterLeft,kF);
        veloToggle = new ButtonToggle(2, "x", () -> {
        }, () -> {
        });
        veloToggle.init();
        try {
            pw = new PrintWriter(LoggingUtil.getLogFile("shooter.csv"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        startTime = System.currentTimeMillis();
        pw.println("time,velocity,power,error,setpoint");
    }

    @Override
    public void loop() {

        veloToggle.loop();
        veloControlActive = veloToggle.getState();
        double manualPower = gamepadService.getAnalog(2, "right_stick_y");
        if (Math.abs(manualPower) != 0.0) {
            Teleop.getInstance().getHardware().getMotors().get("shooterLeft").setPower(manualPower);
            Teleop.getInstance().getHardware().getMotors().get("shooterRight").setPower(manualPower);
        } else if (veloControlActive) {
            time.add(System.currentTimeMillis()-startTime);
            velo.add(bulkRead.getShooterLeftVelo());

            PIDFController.setkP(pShooterLeft);
            PIDFController.updateSetpoint(motorVelo);
            double autoPower = PIDFController.update(bulkRead.getShooterLeftVelo());
            power.add(autoPower);
            error.add(motorVelo-bulkRead.getShooterLeftVelo());
            dashboardTelemetry.addData("autoPower",autoPower);
            Teleop.getInstance().getHardware().getMotors().get("shooterLeft").setPower(-autoPower);
            Teleop.getInstance().getHardware().getMotors().get("shooterRight").setPower(-autoPower);
        } else {
            Teleop.getInstance().getHardware().getMotors().get("shooterLeft").setPower(0.0);
            Teleop.getInstance().getHardware().getMotors().get("shooterRight").setPower(0.0);
        }


        dashboardTelemetry.addData("manual", manualPower);
        dashboardTelemetry.addData("Target Velo", motorVelo);
        dashboardTelemetry.addData("MotorLeft Velo", leftMotor.getVelocity());
        dashboardTelemetry.addData("MotorRight Velo",rightMotor.getVelocity());
        dashboardTelemetry.addData("PID Active ", veloControlActive);
        dashboardTelemetry.update();

    }

    @Override
    public void stop() {
        for(int i=0;i<time.size();i++){
            pw.println((time.get(i))+","+velo.get(i)+","+power.get(i)+","+error.get(i)+","+"1200");
        }
        pw.close();
    }
}
