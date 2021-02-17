package org.igutech.teleop;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import org.igutech.config.Hardware;
import org.igutech.teleop.Modules.Shooter;
import org.igutech.teleop.Modules.TimerService;

import java.util.HashMap;

@TeleOp
public class ServoTest extends LinearOpMode {
    TimerService timerService = new TimerService();
    int currentShooterServoLevel = 0;
    Hardware hardware;
    Shooter shooter;
    private HashMap<Integer, Double> liftPositions;
    boolean isShooterEnabled = false;

    @Override
    public void runOpMode() throws InterruptedException {
        hardware = new Hardware(hardwareMap);
        shooter = new Shooter(hardware,false);
        shooter.init();
        liftPositions = new HashMap<>();
        liftPositions.put(0, 0.78);
        liftPositions.put(1, 0.65);
        liftPositions.put(2, 0.59);
        liftPositions.put(3, 0.5);
        waitForStart();
        timerService.start();

        isShooterEnabled=true;
        handleLift(1);
        while (!isStopRequested() && opModeIsActive()) {
            timerService.loop();
            shooter.loop();
        }
    }

    public void handleLift(int shooterLevel) {

        shooter.setShooterStatus(true);
        currentShooterServoLevel = shooterLevel;
        System.out.println("Running Indexer");
        hardware.getServos().get("liftServo").setPosition(liftPositions.get(currentShooterServoLevel));
        timerService.registerUniqueTimerEvent(2000, "Index", () -> {
            hardware.getServos().get("shooterServo").setPosition(0.32);
            timerService.registerUniqueTimerEvent(300, "Index", () -> {
                hardware.getServos().get("shooterServo").setPosition(0.1);
                currentShooterServoLevel++;
                if (currentShooterServoLevel > 3) {
                    isShooterEnabled = false;
                }
                if (isShooterEnabled) {
                    timerService.registerUniqueTimerEvent(75,"Index",()->{
                        handleLift(currentShooterServoLevel);
                    });

                } else {
                    currentShooterServoLevel = 0;
                    hardware.getServos().get("liftServo").setPosition(liftPositions.get(currentShooterServoLevel));
                    timerService.registerUniqueTimerEvent(500, "Index", () -> {
                        shooter.setShooterStatus(false);
                    });
                }
            });
        });
    }
}
