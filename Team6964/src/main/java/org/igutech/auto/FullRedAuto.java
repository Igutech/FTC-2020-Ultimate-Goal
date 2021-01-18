package org.igutech.auto;

import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.acmerobotics.roadrunner.trajectory.Trajectory;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.igutech.auto.paths.RedA;
import org.igutech.auto.roadrunner.SampleMecanumDrive;

import java.util.ArrayList;


@Autonomous
public class FullRedAuto extends LinearOpMode {

    @Override
    public void runOpMode() throws InterruptedException {
        SampleMecanumDrive drive = new SampleMecanumDrive(hardwareMap);
        Pose2d startPose = new Pose2d(-60, -48, Math.toRadians(0));
        drive.setPoseEstimate(startPose);
        ArrayList<Trajectory> trajectories = RedA.createTrajectory(drive, startPose);

        waitForStart();

        if (isStopRequested()) return;

        for (Trajectory trajectory : trajectories) {
            drive.followTrajectory(trajectory);
        }

    }
}
