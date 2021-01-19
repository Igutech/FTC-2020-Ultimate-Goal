package org.igutech.auto;

import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.acmerobotics.roadrunner.trajectory.Trajectory;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.igutech.auto.modules.Shooter;
import org.igutech.auto.paths.RedA;
import org.igutech.auto.roadrunner.SampleMecanumDrive;
import org.igutech.auto.util.AutoUtilManager;
import org.igutech.utils.control.PIDFController;

import java.util.ArrayList;
import java.util.HashMap;


@Autonomous
public class FullRedAuto extends LinearOpMode {
    private static State currentState = State.PREPARE_TO_SHOOT;

    @Override
    public void runOpMode() throws InterruptedException {

        Shooter shooter = new Shooter(hardwareMap);

        SampleMecanumDrive drive = new SampleMecanumDrive(hardwareMap);
        Pose2d startPose = new Pose2d(-60, -48, Math.toRadians(0));
        drive.setPoseEstimate(startPose);
        HashMap<String, Trajectory> trajectories = RedA.createTrajectory(drive, startPose);
        AutoUtilManager autoUtilManager = new AutoUtilManager(hardwareMap, "RedA");
        telemetry.addData("Status: ", "Ready");
        telemetry.update();
        waitForStart();

        if (isStopRequested()) return;


        while (!isStopRequested() && opModeIsActive()) {
            switch (currentState) {
                case PREPARE_TO_SHOOT:
                    drive.followTrajectoryAsync(trajectories.get("PrepareToShoot"));
                    break;
                case SHOOTING:
                    shooter.setStatus(true);
                    ElapsedTime elapsedTime = new ElapsedTime(ElapsedTime.Resolution.MILLISECONDS);
                    if(elapsedTime.time()>3000){
                        shooter.setStatus(false);
                        currentState=State.INTAKE_RING_STACK;
                    }

            }

            drive.update();
            autoUtilManager.loop();
        }

    }



    public static void setState(State state) {
        currentState = state;
    }

    public static State getState() {
        return currentState;
    }


}
