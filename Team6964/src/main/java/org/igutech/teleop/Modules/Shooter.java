package org.igutech.teleop.Modules;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.robotcore.hardware.DcMotorEx;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.igutech.config.Hardware;
import org.igutech.teleop.Module;
import org.igutech.teleop.Teleop;
import org.igutech.utils.ButtonToggle;
import org.igutech.utils.control.PIDFController;

//back motor=0.5 power and front =0.6
//powershot back=-6 front = 0.55
@Config
public class Shooter extends Module {
    FtcDashboard dashboard = FtcDashboard.getInstance();
    Telemetry dashboardTelemetry = dashboard.getTelemetry();
    private GamepadService gamepadService;
    private Hardware hardware;
    public static double frontShooterkP = 0.007;
    public static double frontShooterkI = 0.001;
    public static double frontShooterkD = 0.0;
    public static double frontShooterkF = 0.;

    public static double backShooterkP = 0.0032;
    public static double backShooterkI = 0.00145;
    public static double backShooterkD = 0.00;
    public static double backShooterkF = 0.0;

    public static double frontShooterTargetVelo = -1400;
    //public static double backShooterTargetVelo = -1080;
    public static double backShooterTargetVelo = -1030;
    public static double frontShooterPowershotVelo = -1200;
    public static double backShooterPowershotVelo = -1100;
    private PIDFController frontShooterController;
    private PIDFController backShooterController;
    private boolean veloControlActive = false;
    private ButtonToggle highGoalToggle;
    private ButtonToggle powershotToggle;

    private boolean wasPidRunning;
    private DcMotorEx frontShooterMotor;
    private DcMotorEx backShooterMotor;
    private boolean inTeleop;
    private boolean enableShooter = false;
    private ShooterState shooterState = ShooterState.OFF;

    public Shooter(Hardware hardware, boolean inTeleop) {
        super(500, "Shooter");
        this.hardware = hardware;
        this.inTeleop = inTeleop;
    }

    @Override
    public void init() {
        frontShooterController = new PIDFController(frontShooterkP, frontShooterkI, frontShooterkD, frontShooterkF);
        backShooterController = new PIDFController(backShooterkP, backShooterkI, backShooterkD, backShooterkF);
        if (inTeleop) {
            gamepadService = (GamepadService) Teleop.getInstance().getService("GamepadService");

            highGoalToggle = new ButtonToggle(1, "right_bumper", () -> {
                frontShooterController.init();
                backShooterController.init();
            }, () -> {
                frontShooterController.init();
                backShooterController.init();
            });
            highGoalToggle.init();
            powershotToggle = new ButtonToggle(1, "b", () -> {
                frontShooterController.init();
                backShooterController.init();
            }, () -> {
                frontShooterController.init();
                backShooterController.init();
            });
            powershotToggle.init();
        }
        frontShooterMotor = ((DcMotorEx) hardware.getMotors().get("frontshooter"));
        backShooterMotor = ((DcMotorEx) hardware.getMotors().get("backshooter"));

    }

    @Override
    public void loop() {

        double manualPower = 0;
        if (inTeleop) {
            highGoalToggle.loop();
            powershotToggle.loop();
            manualPower = gamepadService.getAnalog(2, "right_trigger");
            if (Math.abs(manualPower) != 0.0) {
                wasPidRunning = false;
                hardware.getMotors().get("frontshooter").setPower(-manualPower);
                hardware.getMotors().get("backshooter").setPower(-manualPower);
            } else if (highGoalToggle.getState()) {
                shooterState = ShooterState.HIGH_GOAL;
            } else if (powershotToggle.getState()) {
                shooterState = ShooterState.POWERSHOT;
            } else {
                shooterState = ShooterState.OFF;
            }
        } else {
            if (enableShooter) {
                shooterState = ShooterState.HIGH_GOAL;
            } else {
                shooterState = ShooterState.OFF;
            }
        }

        if (shooterState == ShooterState.MANUAL) {
            wasPidRunning = false;
            hardware.getMotors().get("frontshooter").setPower(-manualPower);
            hardware.getMotors().get("backshooter").setPower(-manualPower);
        } else if (shooterState == ShooterState.HIGH_GOAL) {
            if(inTeleop){
                if (powershotToggle.getState()) {
                    powershotToggle.setState(false);
                }
            }
            if (!wasPidRunning) {
                frontShooterController.init();
                backShooterController.init();
            }
            frontShooterController.updateSetpoint(frontShooterTargetVelo);
            backShooterController.updateSetpoint(backShooterTargetVelo);
            double frontShooterPower = frontShooterController.update(frontShooterMotor.getVelocity());
            double backShooterPower = backShooterController.update(backShooterMotor.getVelocity());

            hardware.getMotors().get("frontshooter").setPower(frontShooterPower);
            hardware.getMotors().get("backshooter").setPower(backShooterPower);

            dashboardTelemetry.addData("frontShooterPower", frontShooterPower);
            dashboardTelemetry.addData("backShooterPower", backShooterPower);
        } else if (shooterState == ShooterState.POWERSHOT) {
            if (!wasPidRunning) {
                frontShooterController.init();
                backShooterController.init();
            }
            frontShooterController.updateSetpoint(frontShooterPowershotVelo);
            backShooterController.updateSetpoint(backShooterPowershotVelo);
            double frontShooterPower = frontShooterController.update(frontShooterMotor.getVelocity());
            double backShooterPower = backShooterController.update(backShooterMotor.getVelocity());

            hardware.getMotors().get("frontshooter").setPower(frontShooterPower);
            hardware.getMotors().get("backshooter").setPower(backShooterPower);
            dashboardTelemetry.addData("frontShooterPower", frontShooterPower);
        } else {
            wasPidRunning = false;
            hardware.getMotors().get("frontshooter").setPower(0.0);
            hardware.getMotors().get("backshooter").setPower(0.0);
        }

        dashboardTelemetry.addData("manualPower", manualPower);
        dashboardTelemetry.addData("Target frontShooter Velo", frontShooterTargetVelo);
        dashboardTelemetry.addData("Target backShooter Velo", backShooterTargetVelo);
        dashboardTelemetry.addData("Front Shooter Velo", frontShooterMotor.getVelocity());
        dashboardTelemetry.addData("Back Shooter Velo", backShooterMotor.getVelocity());
        dashboardTelemetry.addData("PID Active ", veloControlActive);
        dashboardTelemetry.addData("Front P ", frontShooterController.getkP());
        dashboardTelemetry.addData("Back P ", backShooterController.getkP());
        if(inTeleop){
            dashboardTelemetry.update();
        }
        if(inTeleop){
            if (highGoalToggle.getState() || powershotToggle.getState()) {
                wasPidRunning = true;
            }
        }else{
            if(shooterState==ShooterState.HIGH_GOAL){
                wasPidRunning=true;
            }
        }

    }

    public void setEnableShooter(boolean status) {
        enableShooter = status;
    }

    enum ShooterState {
        MANUAL,
        HIGH_GOAL,
        POWERSHOT,
        OFF;
    }

}
